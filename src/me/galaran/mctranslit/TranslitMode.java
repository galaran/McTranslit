/*
 * Copyright (C) 2011 <galaran.h@gmail.com>
 *
 * This file is part of the Bukkit plugin McTranslit.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307, USA.
 */
package me.galaran.mctranslit;

import java.util.HashMap;
import java.util.Map;

public enum TranslitMode {
    
    OFF("off"),
    
    IN("in"),
    
    FULL("full"),
    
    DW("dw");
    
    
    private final String code;
    private final static Map<String, TranslitMode> translitModes = new HashMap<String, TranslitMode>();
    
    private TranslitMode(String code) {
        this.code = code;
    }
    
    public static TranslitMode getByCode(String code) {
        return translitModes.get(code);
    }
    
    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }
    
    static {
        for (TranslitMode tm : TranslitMode.values()) {
            translitModes.put(tm.getCode(), tm);
        }
    }
}
