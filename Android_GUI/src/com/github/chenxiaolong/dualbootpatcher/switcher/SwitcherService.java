/*
 * Copyright (C) 2014  Andrew Gunnerson <andrewgunnerson@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.chenxiaolong.dualbootpatcher.switcher;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.github.chenxiaolong.dualbootpatcher.CommandUtils;
import com.github.chenxiaolong.dualbootpatcher.CommandUtils.CommandResult;
import com.github.chenxiaolong.dualbootpatcher.CommandUtils.RootCommandListener;
import com.github.chenxiaolong.dualbootpatcher.CommandUtils.RootCommandParams;
import com.github.chenxiaolong.dualbootpatcher.CommandUtils.RootCommandRunner;
import com.github.chenxiaolong.dualbootpatcher.FileUtils;
import com.github.chenxiaolong.dualbootpatcher.MainActivity;
import com.github.chenxiaolong.dualbootpatcher.R;
import com.github.chenxiaolong.dualbootpatcher.switcher.SwitcherUtils.VerificationResult;
import com.github.chenxiaolong.dualbootpatcher.switcher.ZipFlashingFragment.PendingAction;
import com.github.chenxiaolong.multibootpatcher.AnsiStuff;
import com.github.chenxiaolong.multibootpatcher.AnsiStuff.Attribute;
import com.github.chenxiaolong.multibootpatcher.AnsiStuff.Color;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.EnumSet;

public class SwitcherService extends IntentService {
    private static final String TAG = SwitcherService.class.getSimpleName();
    public static final String BROADCAST_INTENT =
            "com.chenxiaolong.github.dualbootpatcher.BROADCAST_SWITCHER_STATE";

    public static final String ACTION = "action";
    public static final String ACTION_CHOOSE_ROM = "choose_rom";
    public static final String ACTION_SET_KERNEL = "set_kernel";
    public static final String ACTION_VERIFY_ZIP = "verify_zip";
    public static final String ACTION_FLASH_ZIPS = "flash_zips";

    public static final String PARAM_KERNEL_ID = "kernel_id";
    public static final String PARAM_ZIP_FILE = "zip_file";
    public static final String PARAM_PENDING_ACTIONS = "pending_actions";

    public static final String STATE = "state";
    public static final String STATE_CHOSE_ROM = "chose_rom";
    public static final String STATE_SET_KERNEL = "set_kernel";
    public static final String STATE_VERIFIED_ZIP = "verified_zip";
    public static final String STATE_FLASHED_ZIPS = "flashed_zips";
    public static final String STATE_COMMAND_OUTPUT_DATA = "command_output_line";

    public static final String RESULT_FAILED = "failed";
    public static final String RESULT_KERNEL_ID = "kernel_id";
    public static final String RESULT_VERIFY_ZIP = "verify_zip";
    public static final String RESULT_TOTAL_ACTIONS = "total_actions";
    public static final String RESULT_FAILED_ACTIONS = "failed_actions";
    public static final String RESULT_OUTPUT_DATA = "output_line";

    private static final String UPDATE_BINARY = "META-INF/com/google/android/update-binary";

    public SwitcherService() {
        super(TAG);
    }

    private void onChoseRom(boolean failed, String kernelId) {
        Intent i = new Intent(BROADCAST_INTENT);
        i.putExtra(STATE, STATE_CHOSE_ROM);
        i.putExtra(RESULT_FAILED, failed);
        i.putExtra(RESULT_KERNEL_ID, kernelId);
        sendBroadcast(i);
    }

    private void onSetKernel(boolean failed, String kernelId) {
        Intent i = new Intent(BROADCAST_INTENT);
        i.putExtra(STATE, STATE_SET_KERNEL);
        i.putExtra(RESULT_FAILED, failed);
        i.putExtra(RESULT_KERNEL_ID, kernelId);
        sendBroadcast(i);
    }

    private void onVerifiedZip(VerificationResult result) {
        Intent i = new Intent(BROADCAST_INTENT);
        i.putExtra(STATE, STATE_VERIFIED_ZIP);
        i.putExtra(RESULT_VERIFY_ZIP, result);
        sendBroadcast(i);
    }

    private void onFlashedZips(int totalActions, int failedActions) {
        Intent i = new Intent(BROADCAST_INTENT);
        i.putExtra(STATE, STATE_FLASHED_ZIPS);
        i.putExtra(RESULT_TOTAL_ACTIONS, totalActions);
        i.putExtra(RESULT_FAILED_ACTIONS, failedActions);
        sendBroadcast(i);
    }

    private void onNewOutputData(String line) {
        Intent i = new Intent(BROADCAST_INTENT);
        i.putExtra(STATE, STATE_COMMAND_OUTPUT_DATA);
        i.putExtra(RESULT_OUTPUT_DATA, line);
        sendBroadcast(i);
    }

    private void setupNotification(String action) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.setAction(Intent.ACTION_MAIN);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setOngoing(true);

        if (ACTION_CHOOSE_ROM.equals(action)) {
            builder.setContentTitle(getString(R.string.switching_rom));
        } else if (ACTION_SET_KERNEL.equals(action)) {
            builder.setContentTitle(getString(R.string.setting_kernel));
        }

        builder.setContentIntent(pendingIntent);
        builder.setProgress(0, 0, true);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(1, builder.build());
    }

    private void removeNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(1);
    }

    private void chooseRom(Bundle data) {
        setupNotification(ACTION_CHOOSE_ROM);

        String kernelId = data.getString(PARAM_KERNEL_ID);
        boolean failed = !SwitcherUtils.chooseRom(this, kernelId);

        onChoseRom(failed, kernelId);

        removeNotification();
    }

    private void setKernel(Bundle data) {
        setupNotification(ACTION_SET_KERNEL);

        String kernelId = data.getString(PARAM_KERNEL_ID);
        boolean failed = !SwitcherUtils.setKernel(this, kernelId);

        onSetKernel(failed, kernelId);

        removeNotification();
    }

    private void verifyZip(Bundle data) {
        String zipFile = data.getString(PARAM_ZIP_FILE);
        VerificationResult result = SwitcherUtils.verifyZipMbtoolVersion(zipFile);
        onVerifiedZip(result);
    }

    private int runRootCommand(String command) {
        printBoldText(Color.YELLOW, "Running command: " + command + "\n");

        RootCommandParams params = new RootCommandParams();
        params.command = command;
        params.listener = new RootCommandListener() {
            @Override
            public void onNewOutputLine(String line) {
                SwitcherService.this.onNewOutputData(line + "\n");
            }

            @Override
            public void onCommandCompletion(CommandResult result) {
            }
        };

        RootCommandRunner cmd = new RootCommandRunner(params);
        cmd.start();
        CommandUtils.waitForRootCommand(cmd);
        CommandResult result = cmd.getResult();

        if (result == null) {
            return -1;
        } else {
            return result.exitCode;
        }
    }

    private boolean remountFs(String mountpoint, boolean rw) {
        printBoldText(Color.YELLOW, "Mounting " + mountpoint + " as " +
                (rw ? "writable" : "read-only") + "\n");

        if (rw) {
            return runRootCommand("mount -o remount,rw " + mountpoint) == 0;
        } else {
            return runRootCommand("mount -o remount,ro " + mountpoint) == 0;
        }
    }

    private void printSeparator() {
        printBoldText(Color.WHITE, StringUtils.repeat('-', 16) + "\n");
    }

    private void printBoldText(Color color, String text) {
        onNewOutputData(AnsiStuff.format(text,
                EnumSet.of(color), null, EnumSet.of(Attribute.BOLD)));
    }

    private void flashZips(Bundle data) {
        Parcelable[] parcelables = data.getParcelableArray(PARAM_PENDING_ACTIONS);
        PendingAction[] actions = new PendingAction[parcelables.length];
        System.arraycopy(parcelables, 0, actions, 0, parcelables.length);

        int succeeded = 0;

        try {
            if (!remountFs("/", true)) {
                printBoldText(Color.RED, "Failed to remount / as rw");
                return;
            }
            if (!remountFs("/system", true)) {
                printBoldText(Color.RED, "Failed to remount /system as rw");
                return;
            }

            for (PendingAction pa : actions) {
                if (pa.type != PendingAction.Type.INSTALL_ZIP) {
                    throw new IllegalStateException("Only INSTALL_ZIP is supported right now");
                }

                printSeparator();

                printBoldText(Color.MAGENTA, "Processing action: Flash zip\n");
                printBoldText(Color.MAGENTA, "- ZIP file: " + pa.zipFile + "\n");
                printBoldText(Color.MAGENTA, "- Destination: " + pa.romId + "\n");

                // Extract mbtool from the zip file
                File zipInstaller = new File(getCacheDir() + File.separator + "rom-installer");
                zipInstaller.delete();

                printBoldText(Color.YELLOW, "Extracting mbtool ROM installer from the zip file\n");
                if (!FileUtils.zipExtractFile(pa.zipFile, UPDATE_BINARY, zipInstaller.getPath())) {
                    printBoldText(Color.RED, "Failed to extract update-binary\n");
                    return;
                }

                // Copy to /
                if (runRootCommand("rm -f /rom-installer") != 0) {
                    printBoldText(Color.RED, "Failed to remove old /rom-installer");
                    return;
                }
                if (runRootCommand("cp " + zipInstaller.getPath() + " /rom-installer") != 0) {
                    printBoldText(Color.RED, "Failed to copy new /rom-installer");
                    return;
                }
                if (runRootCommand("chmod 755 /rom-installer") != 0) {
                    printBoldText(Color.RED, "Failed to chmod /rom-installer");
                    return;
                }

                int ret = runRootCommand("/rom-installer --romid " + pa.romId + " " + pa.zipFile);

                zipInstaller.delete();

                if (ret < 0) {
                    printBoldText(Color.RED, "\nFailed to run command\n");
                    return;
                } else {
                    printBoldText(ret == 0 ? Color.GREEN : Color.RED,
                            "\nCommand returned: " + ret + "\n");

                    if (ret == 0) {
                        succeeded++;
                    } else {
                        return;
                    }
                }
            }

            printSeparator();

            if (!remountFs("/", false)) {
                printBoldText(Color.RED, "Failed to remount / as ro");
            }
            if (!remountFs("/system", false)) {
                printBoldText(Color.RED, "Failed to remount /system as ro");
            }
        } finally {
            printSeparator();

            String frac = succeeded + "/" + actions.length;

            printBoldText(Color.CYAN, "Successfully completed " + frac + " actions\n");

            onFlashedZips(actions.length, actions.length - succeeded);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getStringExtra(ACTION);

        if (ACTION_CHOOSE_ROM.equals(action)) {
            chooseRom(intent.getExtras());
        } else if (ACTION_SET_KERNEL.equals(action)) {
            setKernel(intent.getExtras());
        } else if (ACTION_VERIFY_ZIP.equals(action)) {
            verifyZip(intent.getExtras());
        } else if (ACTION_FLASH_ZIPS.equals(action)) {
            flashZips(intent.getExtras());
        }
    }
}
