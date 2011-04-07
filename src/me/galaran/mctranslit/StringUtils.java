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

public class StringUtils {
    public static String buildString(String[] strArr, char delimiter) {
        StringBuilder sb = new StringBuilder();
        for (String curStr : strArr) {
            sb.append(curStr);
            sb.append(delimiter);
        }
        return sb.toString().trim();
    }
    
    public static String byteArrayAsString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte cur : bytes) {
            sb.append((int)cur).append(' ');
        }
        return sb.toString();
    }
}
