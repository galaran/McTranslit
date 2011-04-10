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

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private PluginManager pm;
    private McTranslitPlayerListener playerListener;
    private Config cfg;
    private PlayerDBManager db;
    private Translitter translitter;

    @Override
    public void onEnable() {
        pm = getServer().getPluginManager();
        PluginDescriptionFile pdfFile = getDescription();
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
        
        cfg = new Config(this);
        db = new PlayerDBManager(this, cfg);
        
        try {
            cfg.loadConfig();
        } catch (IOException ex) {
            System.out.println(pdfFile.getName() + ": exception while loading configuration");
            ex.printStackTrace();
            return;
        }
        
        try {
            db.initDb();
        } catch (IOException ex) {
            System.out.println(pdfFile.getName() + ": exception while loading player db");
            ex.printStackTrace();
            return;
        }
        
        System.out.println(pdfFile.getName() + ": loaded " + db.getSize() + " players and " +
                cfg.getTranslitMap().size() + " translit rules");

        translitter = new Translitter(cfg);
        playerListener = new McTranslitPlayerListener(this, translitter, db);
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Lowest, this);
        
        // save Db every 3 min
        ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
        ex.scheduleWithFixedDelay(db, 1, 3, TimeUnit.MINUTES);
    }


    @Override
    public void onDisable() {
        db.saveDb(); // forced
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can be executed only by players");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (command.getName().equalsIgnoreCase("translit")) {
            if (args.length < 1) {
                printUsage(sender, command);
                return true;
            }
            
            if (args[0].equalsIgnoreCase("off")) {
                db.setMode(player, TranslitMode.OFF);
                player.sendMessage(ChatColor.AQUA + "Now you will receive messages \"as is\"");
                return true;
            } else if (args[0].equalsIgnoreCase("in")) {
                db.setMode(player, TranslitMode.IN);
                player.sendMessage(ChatColor.AQUA + "Now you will receive transliterated messages");
                return true;
            } else if (args[0].equalsIgnoreCase("full")) {
                db.setMode(player, TranslitMode.FULL);
                player.sendMessage(ChatColor.AQUA + "Now you will receive transliterated messages");
                player.sendMessage(ChatColor.AQUA + "and send detransliterated to players, who use language pack");
                player.sendMessage(ChatColor.AQUA + "Write '" + cfg.getFullModePrefix() + "' before word, if you want");
                player.sendMessage(ChatColor.AQUA + "to send it \"as is\"");
                return true;
            } else if (args[0].equalsIgnoreCase("dw")) {
                db.setMode(player, TranslitMode.DW);
                player.sendMessage(ChatColor.AQUA + "Use '" + cfg.getDwModePrefix() + "' ?? how to describe this ?");
                return true;
            } else {
                printUsage(sender, command);
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("translittest")) {
            if (args.length == 0) {
                player.sendMessage(ChatColor.DARK_AQUA + "Invalid parameters. Usage: " + command.getUsage());
                return true;
            }
            
            String transliterated = translitter.transliterate(StringUtils.buildString(args, ' '));
            player.sendMessage(ChatColor.AQUA + "> " + transliterated);
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("detranslittest")) {
            if (args.length == 0) {
                player.sendMessage(ChatColor.DARK_AQUA + "Invalid parameters. Usage: " + command.getUsage());
                return true;
            }
            
            String detransliterated = translitter.detransliterate(StringUtils.buildString(args, ' '));
            player.sendMessage(ChatColor.AQUA + "> " + detransliterated);
            return true;
        }
        
        return false;
    }
    
    private void printUsage(CommandSender sender, Command command) {
        sender.sendMessage(ChatColor.DARK_AQUA + "Usage: " + command.getUsage());
    }
        
}

