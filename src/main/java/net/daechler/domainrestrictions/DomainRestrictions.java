package net.daechler.domainrestrictions;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class DomainRestrictions extends Plugin implements Listener {

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, this);
    }

    @Override
    public void onDisable() {
        getProxy().getPluginManager().unregisterListener(this);
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        PendingConnection connection = event.getConnection();
        String hostname = connection.getVirtualHost().getHostName();
        getLogger().info("Benutzer verbindet sich über: " + hostname);
        if (hostname == null || (!hostname.equalsIgnoreCase("playsmp.xyz") && !hostname.equalsIgnoreCase("localhost") && !hostname.equals("127.0.0.1") && !hostname.equals("127.0.1.1"))) {
            event.setCancelled(true);
            event.setCancelReason(ChatColor.RED + "Du kannst dich nur über die Domain playsmp.xyz verbinden.");
        }
    }
}
