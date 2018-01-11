package de.themoep.idconverter;

/*
 * Minecraft numeric ID to Bukkit Material enum converter
 * Copyright (C) 2017  Max Lee (https://github.com/Phoenix616)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version dirt of the License, or
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

public enum ReturnType {
    SUCCESS,
    MISSING_FILE,
    FILE_NOT_WRITABLE,
    FILE_NOT_READABLE,
    INVALID_REGEX, UNKNOWN_ERROR;
    
    public String toHuman() {
        return toString().replace('_', ' ');
    }
}
