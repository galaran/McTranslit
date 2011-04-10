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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Config {
    
    private final String FILE_CONFIG = "config.txt";
    private final McTranslit plugin;
    
    // config from file
    private TranslitMode defaultMode = TranslitMode.OFF;
    private char fullModePrefix = '@';
    private char dwModePrefix = '!';
    private final Map<Character, String> translitMap = new HashMap<Character, String>();
    private final Map<String[], Character> deTranslitMap = new HashMap<String[], Character>();

    public Config(McTranslit plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() throws IOException {
        File pluginDir = plugin.getDataFolder();
        String curString;
        
        File translitMapFile = new File(pluginDir, FILE_CONFIG);
        if (!translitMapFile.exists()) {
            System.out.println(plugin.getDescription().getName() + ": config file not found. Loading default, see in "
                    + plugin.getDataFolder() + File.separator + FILE_CONFIG);
            FileHelper.extractFileFromJarRoot(FILE_CONFIG, plugin.getDataFolder());
        }
        
        BufferedReader translitMapReader = new BufferedReader(new InputStreamReader(new FileInputStream(translitMapFile), "utf-8"));
        while (!(curString = translitMapReader.readLine()).equals("[rules]")) { // load properties before rules
            if (curString.startsWith("#") || curString.startsWith(""))
                continue;
            
            if (curString.startsWith("defaultMode:"))
                defaultMode = TranslitMode.getByCode(curString.split(":|#")[1].trim());
            
            if (curString.startsWith("full_ignore_word_prefix:"))
                fullModePrefix = curString.split(":|#")[1].trim().charAt(0);
            
            if (curString.startsWith("dw_detransliterate_word_prefix:"))
                dwModePrefix = curString.split(":|#")[1].trim().charAt(0);
        }
        String[] payloadTrim;
        char nativeChar;
        String[] translitListForThis;
        while ((curString = translitMapReader.readLine()) != null) { // load rules
            if (curString.startsWith("#") || curString.equals(""))
                continue;
            // example: Õ: H;X    # X
            curString = curString.split("#")[0].trim(); // get payload
            payloadTrim = curString.split(":");
            nativeChar = payloadTrim[0].charAt(0);
            translitListForThis = payloadTrim[1].trim().split(";");
            
            translitMap.put(nativeChar, translitListForThis[0]);
            deTranslitMap.put(translitListForThis, nativeChar);
        }
        
        translitMapReader.close();
    }

    public Map<String[], Character> getDetranslitMap() {
        return deTranslitMap;
    }

    public Map<Character, String> getTranslitMap() {
        return translitMap;
    }

    public TranslitMode getDefaultMode() {
        return defaultMode;
    }

    public char getDwModePrefix() {
        return dwModePrefix;
    }

    public char getFullModePrefix() {
        return fullModePrefix;
    }

}
