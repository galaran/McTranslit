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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author galaran
 */
public class McTranslit extends JavaPlugin {
    
    public final String FILE_PLAYERS = "players.txt";
    public final String FILE_TRANSLIT_MAP = "translit.txt";

    private PluginManager pm;
    private McTranslitPlayerListener playerListener = new McTranslitPlayerListener(this);
    
    private Map<Character, String> translitMap = new HashMap<Character, String>();
    private Set<String> translitPlayers = Collections.synchronizedSet(new HashSet<String>());

    @Override
    public void onEnable() {
        pm = getServer().getPluginManager();
        PluginDescriptionFile pdfFile = getDescription();
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
        
        try {
            loadSettings();
        } catch (IOException ex) {
            System.out.println(pdfFile.getName() + ": failed to load config");
            ex.printStackTrace();
            return;
        }
        System.out.println(pdfFile.getName() + ": loaded " + translitPlayers.size() + " players and " +
                translitMap.size() + " translit rules");

        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Low, this);
    }


    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can be executed only by players");
            return true;
        }
        
        Player pl = (Player) sender;
        
        if (command.getName().equalsIgnoreCase("translit")) {
            if (args.length == 0) {
                pl.sendMessage(ChatColor.DARK_AQUA + "Invalid parameters. Usage: " + command.getUsage());
                return true;
            }
            
            if (args[0].equalsIgnoreCase("on")) {
                // add this player to translit set
                translitPlayers.add(pl.getName());
                saveSettings();
                pl.sendMessage(ChatColor.AQUA + "Now you will receive transliterated messages");
            } else if (args[0].equalsIgnoreCase("off")) {
                // remove from translit set
                translitPlayers.remove(pl.getName());
                saveSettings();
                pl.sendMessage(ChatColor.AQUA + "Now you will receive original messages");
            } else {
                pl.sendMessage(ChatColor.DARK_AQUA + "Invalid parameters. Usage: " + command.getUsage());
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("translittest") && pl.isOp()) {
            if (args.length == 0) {
                pl.sendMessage(ChatColor.DARK_AQUA + "Invalid parameters. Usage: " + command.getUsage());
                return true;
            }
            
            String transliterated = Translitter.transliterate(StringUtils.buildString(args, ' '), translitMap);
            pl.sendMessage(ChatColor.AQUA + "> " + transliterated);
            return true;
        }
        
        return false;
    }

    private void loadSettings() throws IOException {
        File pluginDir = getDataFolder();
        String curString;
        
        File playersFile = new File(pluginDir, FILE_PLAYERS);
        BufferedReader playerReader = new BufferedReader(new InputStreamReader(new FileInputStream(playersFile)));
        while ((curString = playerReader.readLine()) != null) {
            translitPlayers.add(curString);
        }
        playerReader.close();
        
        File translitMapFile = new File(pluginDir, FILE_TRANSLIT_MAP);
        BufferedReader translitMapReader = new BufferedReader(new InputStreamReader(new FileInputStream(translitMapFile), "utf-8"));
        
        while ((curString = translitMapReader.readLine()) != null) {
            int commentBeginsAt = curString.indexOf("#");
            translitMap.put(curString.charAt(0), curString.substring(2, commentBeginsAt).trim());
        }
        translitMapReader.close();
    }
    
    private void saveSettings() {
        try {
            File playersFile = new File(getDataFolder(), FILE_PLAYERS);
            playersFile.delete();
            BufferedWriter bw = new BufferedWriter(new FileWriter(playersFile));
            for (String player : translitPlayers) {
                bw.write(player);
                bw.newLine();
            }
            bw.close();
        } catch (IOException ex) {
            System.out.println(getDescription().getName() + ": error saving player list");
            ex.printStackTrace();
        }
    }
    
    public Set<String> getTranslitPlayers() {
        return translitPlayers;
    }

    public Map<Character, String> getTranslitMap() {
        return translitMap;
    }
    
}

