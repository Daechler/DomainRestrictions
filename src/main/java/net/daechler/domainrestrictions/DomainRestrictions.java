package net.daechler.domainrestrictions;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class DomainRestrictions extends Plugin implements Listener {
    // Configuration object to hold the settings from config.yml
    private Configuration config;

    @Override
    public void onEnable() {
        // Display a green message indicating that the plugin has been enabled
        getLogger().info(ChatColor.GREEN + "DomainRestrictions has been enabled!");

        // Load the config.yml file
        // Check if the plugin data folder exists, create it if not
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        // Create or load the config.yml file
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Load the configuration into the Configuration object
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Register the event listener
        getProxy().getPluginManager().registerListener(this, this);
    }

    @Override
    public void onDisable() {
        // Display a red message indicating that the plugin has been disabled
        getLogger().info(ChatColor.RED + "DomainRestrictions has been disabled!");

        // Unregister the event listener
        getProxy().getPluginManager().unregisterListener(this);
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        // Get the PendingConnection object for the player trying to connect
        PendingConnection connection = event.getConnection();
        // Get the hostname the player is connecting from
        String hostname = connection.getVirtualHost().getHostName();
        // Log the hostname for debugging purposes
        getLogger().info("User connects via: " + hostname);

        // Get the list of allowed domains from the config.yml
        List<String> allowedDomains = config.getStringList("allowedDomains");
        // Get the cancel message from the config.yml and translate color codes
        String cancelMessage = ChatColor.translateAlternateColorCodes('&', config.getString("cancelMessage"));

        // Flag to determine if the hostname is allowed
        boolean isHostnameAllowed = false;

        // Iterate through all allowed domains
        for (String allowedDomain : allowedDomains) {
            // Convert domains to lowercase to ensure the comparison is case-insensitive
            String allowedDomainLower = allowedDomain.toLowerCase();
            String hostnameLower = hostname.toLowerCase();

            // If the allowedDomain contains a wildcard
            if (allowedDomainLower.contains("*.")) {
                String[] allowedDomainParts = allowedDomainLower.split("\\.");
                String[] hostnameParts = hostnameLower.split("\\.");

                // Check if domain parts match (skipping the wildcard)
                if (allowedDomainParts.length == hostnameParts.length) {
                    boolean match = true;
                    for (int i = 1; i < allowedDomainParts.length; i++) {
                        if (!allowedDomainParts[i].equals(hostnameParts[i])) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        isHostnameAllowed = true;
                        break;
                    }
                }
            } else if (hostnameLower.equals(allowedDomainLower)) {
                isHostnameAllowed = true;
                break;
            }
        }

        // Check if the hostname is in the list of allowed domains
        if (hostname == null || !isHostnameAllowed) {
            // Cancel the connection and set the cancel reason
            event.setCancelled(true);
            event.setCancelReason(cancelMessage);
        }
    }
}
