/*
 * Copyright (C) 2017  Andrew Gunnerson <andrewgunnerson@gmail.com>
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

#ifdef __cplusplus
#  include <cstdbool>
#  include <cstdint>
#else
#  include <stdbool.h>
#  include <stdint.h>
#endif

#include "mbcommon/common.h"
#include "mbdevice/capi/flags.h"


MB_BEGIN_C_DECLS

struct CDevice;
typedef struct CDevice CDevice;

MB_EXPORT CDevice * mb_device_new();

MB_EXPORT void mb_device_free(CDevice *device);

#define GETTER(TYPE, NAME) \
    MB_EXPORT TYPE mb_device_ ## NAME (const CDevice *device)
#define SETTER(TYPE, NAME) \
    MB_EXPORT void mb_device_set_ ## NAME (CDevice *device, TYPE value)

GETTER(char *, id);
SETTER(const char *, id);

GETTER(char * const *, codenames);
SETTER(char const * const *, codenames);

GETTER(char *, name);
SETTER(const char *, name);

GETTER(char *, architecture);
SETTER(const char *, architecture);

GETTER(uint32_t, flags);
SETTER(uint32_t, flags);

GETTER(char * const *, block_dev_base_dirs);
SETTER(char const * const *, block_dev_base_dirs);

GETTER(char * const *, system_block_devs);
SETTER(char const * const *, system_block_devs);

GETTER(char * const *, cache_block_devs);
SETTER(char const * const *, cache_block_devs);

GETTER(char * const *, data_block_devs);
SETTER(char const * const *, data_block_devs);

GETTER(char * const *, boot_block_devs);
SETTER(char const * const *, boot_block_devs);

GETTER(char * const *, recovery_block_devs);
SETTER(char const * const *, recovery_block_devs);

GETTER(char * const *, extra_block_devs);
SETTER(char const * const *, extra_block_devs);

/* Boot UI */

GETTER(bool, tw_supported);
SETTER(bool, tw_supported);

GETTER(uint16_t, tw_pixel_format);
SETTER(uint16_t, tw_pixel_format);

MB_EXPORT uint32_t mb_device_validate(const CDevice *device);

MB_EXPORT bool mb_device_equals(const CDevice *a, const CDevice *b);

#undef GETTER
#undef SETTER

MB_END_C_DECLS
