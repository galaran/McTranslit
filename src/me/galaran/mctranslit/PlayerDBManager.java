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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class PlayerDBManager implements Runnable {
    
    private final String FILE_PLAYERS_DB = "players.txt";
    private final McTranslit plugin;
    private final Config cfg;
    
    private Map<String, TranslitMode> translitDb = new HashMap<String, TranslitMode>();
    private boolean dbChanged = false;

    public PlayerDBManager(McTranslit plugin, Config cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
    }

    public void initDb() throws IOException {
        loadDb();
    }
    
    public int getSize() {
        return translitDb.size();
    }

    @Override
    public void run() {
        if (dbChanged) {
            dbChanged = false;
            saveDb();
        }
    }
    
    public void saveDb() {
        Map<String, TranslitMode> copy;
        synchronized (PlayerDBManager.class) {
            copy = new HashMap<String, TranslitMode>(translitDb);
        }
        try {
            File playersFile = new File(plugin.getDataFolder(), FILE_PLAYERS_DB);
            playersFile.delete();
            BufferedWriter bw = new BufferedWriter(new FileWriter(playersFile));
            for (Map.Entry<String, TranslitMode> entry : copy.entrySet()) {
                bw.write(entry.getKey() + ':' + entry.getValue()); // nickname:mode
                bw.newLine();
            }
            bw.close();
        } catch (IOException ex) {
            System.out.println(plugin.getDescription().getName() + ": error while saving player db");
            ex.printStackTrace();
        }
    }
    
    private synchronized void loadDb() throws IOException {
        File playersFile = new File(plugin.getDataFolder(), FILE_PLAYERS_DB);
        if (!playersFile.exists()) // empty db
            return;
        BufferedReader playerReader = new BufferedReader(new FileReader(playersFile));
        
        String curString;
        while ((curString = playerReader.readLine()) != null) {
            translitDb.put(curString.split(":")[0], TranslitMode.getByCode(curString.split(":")[1]));
        }
        playerReader.close();
    }
    
    public synchronized TranslitMode getMode(Player player) {
        String playerName = player.getName();
        TranslitMode tm = translitDb.get(playerName);
        if (tm != null) {
            return tm;
        }
        
        // new player, apply default mode
        translitDb.put(playerName, cfg.getDefaultMode());
        dbChanged = true;
        return cfg.getDefaultMode();
    }
    
    public synchronized void setMode(Player player, TranslitMode newMode) {
        if (translitDb.put(player.getName(), newMode) != newMode) // if old mode != new mode
            dbChanged = true;
    }
}
