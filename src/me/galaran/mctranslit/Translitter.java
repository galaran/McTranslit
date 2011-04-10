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

import java.util.Map;

public class Translitter {
    
    private final Config cfg;

    public Translitter(Config cfg) {
        this.cfg = cfg;
    }

    public String removeFullModePrefixs(String str) {
        return StringUtils.removePrefixes(str, cfg.getFullModePrefix());
    }
    
    public String transliterate(String str) {
        StringBuilder sb = new StringBuilder();
        for (char ch : str.toCharArray()) {
            if (cfg.getTranslitMap().containsKey(ch)) {
                sb.append(cfg.getTranslitMap().get(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public String detransliterate(String str) {
        String[] words = str.split(" ");
        for (int i = 0; i < words.length; i++) {
            if (words[i].startsWith(String.valueOf(cfg.getFullModePrefix()))) {
                words[i] = words[i].substring(1);  // cut prefix
                continue;
            }
            else
                words[i] = detransliterateSingleWord(words[i], cfg.getDetranslitMap());
        }
        return StringUtils.buildString(words, ' ');
    }
    
    private String detransliterateSingleWord(String word, Map<String[], Character> deTranslitMap) {
        String maxMatchValue = null;
        char maxMatchConvertTo = 0;
        int maxMatch;
        StringBuilder result = new StringBuilder();
        
        for (int pos = 0; pos < word.length(); ) {
            // if its punctuatian mark or number
            if (!Character.isLetter(word.charAt(pos))) {
                result.append(word.charAt(pos++));
                continue;
            }
                
            // iterate over all Variants and find longest match
            maxMatch = 0;
            for (String[] curVariants : deTranslitMap.keySet()) {
                for (String curVariant : curVariants) {
                    if (pos + curVariant.length() > word.length()) // this variant is too long :/
                        continue;
                    if (word.substring(pos, pos + curVariant.length()).equals(curVariant)) { // match!
                        if (curVariant.length() > maxMatch) {
                            maxMatch = curVariant.length();
                            maxMatchValue = curVariant;
                            maxMatchConvertTo = deTranslitMap.get(curVariants);
                        }
                    }
                }
            }
            if (maxMatch == 0) // oops, something wrong, skip this char
                pos++;
            else {
                result.append(maxMatchConvertTo);
                pos += maxMatchValue.length();
            }
        }
        return result.toString();
    }

    public String transliterateMarkedWords(String message) {
        String[] words = message.split(" ");
        for (int i = 0; i < words.length; i++) {
            if (words[i].startsWith(String.valueOf(cfg.getDwModePrefix())))
                words[i] = transliterate(words[i].substring(1));
        }
        
        return StringUtils.buildString(words, ' ');
    }

    public String transliterateDwMessage(String message) {
        return transliterate(StringUtils.removePrefixes(message, cfg.getDwModePrefix()));
    }
    
    
}
