/*
 * Copyright (C) 2014-2017  Andrew Gunnerson <andrewgunnerson@gmail.com>
 *
 * This file is part of DualBootPatcher
 *
 * DualBootPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DualBootPatcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DualBootPatcher.  If not, see <http://www.gnu.org/licenses/>.
 */

#pragma once

#include <string>
#include <vector>

#include "mbcommon/common.h"

#include "mbdevice/device_p.h"
#include "mbdevice/flags.h"


namespace mb::device
{

class MB_EXPORT Device
{
public:
    Device();
    ~Device();

    MB_DEFAULT_COPY_CONSTRUCT_AND_ASSIGN(Device)
    MB_DEFAULT_MOVE_CONSTRUCT_AND_ASSIGN(Device)

    std::string id() const;
    void set_id(std::string id);

    std::vector<std::string> codenames() const;
    void set_codenames(std::vector<std::string> codenames);

    std::string name() const;
    void set_name(std::string name);

    std::string architecture() const;
    void set_architecture(std::string architecture);

    DeviceFlags flags() const;
    void set_flags(DeviceFlags flags);

    std::vector<std::string> block_dev_base_dirs() const;
    void set_block_dev_base_dirs(std::vector<std::string> base_dirs);

    std::vector<std::string> system_block_devs() const;
    void set_system_block_devs(std::vector<std::string> block_devs);

    std::vector<std::string> cache_block_devs() const;
    void set_cache_block_devs(std::vector<std::string> block_devs);

    std::vector<std::string> data_block_devs() const;
    void set_data_block_devs(std::vector<std::string> block_devs);

    std::vector<std::string> boot_block_devs() const;
    void set_boot_block_devs(std::vector<std::string> block_devs);

    std::vector<std::string> recovery_block_devs() const;
    void set_recovery_block_devs(std::vector<std::string> block_devs);

    std::vector<std::string> extra_block_devs() const;
    void set_extra_block_devs(std::vector<std::string> block_devs);

    bool tw_supported() const;
    void set_tw_supported(bool supported);

    TwPixelFormat tw_pixel_format() const;
    void set_tw_pixel_format(TwPixelFormat format);

    ValidateFlags validate() const;

    bool operator==(const Device &other) const;

private:
    detail::BaseOptions m_base;
    detail::TwOptions m_tw;
};

}
