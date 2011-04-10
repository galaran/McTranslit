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

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

class McTranslitPlayerListener extends PlayerListener {

    private final McTranslit plugin;
    private final Translitter translitter;
    private final PlayerDBManager db;

    public McTranslitPlayerListener(McTranslit plugin, Translitter translitter, PlayerDBManager db) {
        this.plugin = plugin;
        this.translitter = translitter;
        this.db = db;
    }
    
    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.getMessage().startsWith("/")) // ignore commands
            return;
        
        event.setCancelled(true);
        
        Player sender = event.getPlayer();
        String messageForLanguagePackUsers = null;
        String messageForNativeClientUsers = null;
        
        // preprocess
        switch (db.getMode(sender)) {
            case OFF:  messageForLanguagePackUsers = event.getMessage();
                       messageForNativeClientUsers = translitter.transliterate(event.getMessage());
                       // if language pack user write full translit message
                       if (messageForNativeClientUsers.equals(messageForLanguagePackUsers)) { 
                           event.setCancelled(false);                                          
                           return;
                       }
                       break;
                
            case IN:   event.setCancelled(false);
                       return;
                
            case FULL: messageForLanguagePackUsers = translitter.detransliterate(event.getMessage());
                       messageForNativeClientUsers = translitter.removeFullModePrefixs(event.getMessage());
                       break;
                
            case DW:   messageForLanguagePackUsers = translitter.transliterateMarkedWords(event.getMessage());
                       messageForNativeClientUsers = translitter.transliterateDwMessage(event.getMessage());
                       // if DW player write full-translit message without DW-prefixs
                       if (messageForNativeClientUsers.equals(event.getMessage())) { 
                           event.setCancelled(false);                                          
                           return;
                       }
                       break;
        }
        printMessageToConsole(messageForNativeClientUsers, sender);
        for (Player reciever : plugin.getServer().getOnlinePlayers()) {
            TranslitMode recieverMode = db.getMode(reciever);
            if (recieverMode == TranslitMode.OFF || recieverMode == TranslitMode.DW) { // reciever use language pack
                sendMessageToPlayer(messageForLanguagePackUsers, sender, reciever);
            } else {
                sendMessageToPlayer(messageForNativeClientUsers, sender, reciever);
            }
        }
    }
    
    private void sendMessageToPlayer(String message, Player sender, Player reciever) {
        reciever.sendMessage('<' + sender.getDisplayName() + "> " + message);
    }

    private void printMessageToConsole(String message, Player sender) {
        System.out.println("[TRANSLIT] <" + sender.getName() + "> " + message);
    }
}
