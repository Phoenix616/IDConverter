package de.themoep.idconverter;

/*
 * Minecraft ID converter
 * Copyright (C) 2017  Max Lee (https://github.com/Phoenix616)
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

import java.util.Optional;

public class ReturnState {
    private ReturnType type;
    private Optional<String> message;
    
    public ReturnState() {
        this(ReturnType.UNKNOWN_ERROR);
    }
    
    public ReturnState(ReturnType type) {
        this.type = type;
        this.message = Optional.empty();
    }
    
    public ReturnState(ReturnType type, String message) {
        this.type = type;
        this.message = Optional.of(message);
    }
    
    public ReturnType getType() {
        return type;
    }
    
    public void setType(ReturnType type) {
        this.type = type;
    }
    
    public Optional<String> getMessage() {
        return message;
    }
    
    public void setMessage(Optional<String> message) {
        this.message = message;
    }
}
