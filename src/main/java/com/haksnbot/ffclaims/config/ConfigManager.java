package com.haksnbot.ffclaims.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Unified configuration manager for FF-Claims.
 * Handles both naming and market module configuration.
 */
public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    // ==================== GLOBAL ====================

    public String getPrefix() {
        String prefix = config.getString("messages.prefix", "&6[FF-Claims]&r ");
        return translateColors(prefix);
    }

    public String getMessage(String key) {
        String message = config.getString("messages." + key, "Message not found: " + key);
        return translateColors(message);
    }

    // ==================== MODULE TOGGLES ====================

    public boolean isNamingEnabled() {
        return config.getBoolean("modules.naming", true);
    }

    public boolean isMarketEnabled() {
        return config.getBoolean("modules.market", true);
    }

    // ==================== NAMING MODULE ====================

    public int getMaxNameLength() {
        return config.getInt("naming.max-name-length", 32);
    }

    public int getDisplayFadeIn() {
        return config.getInt("naming.display.fade-in", 5);
    }

    public int getDisplayStay() {
        return config.getInt("naming.display.stay", 40);
    }

    public int getDisplayFadeOut() {
        return config.getInt("naming.display.fade-out", 10);
    }

    // ==================== MARKET MODULE - SALES ====================

    public double getMinSalePrice() {
        return config.getDouble("market.sales.min-price", 100.0);
    }

    public double getMaxSalePrice() {
        return config.getDouble("market.sales.max-price", 0.0);
    }

    // ==================== MARKET MODULE - AUCTIONS ====================

    public int getMinAuctionDurationHours() {
        return config.getInt("market.auctions.min-duration-hours", 1);
    }

    public int getMaxAuctionDurationDays() {
        return config.getInt("market.auctions.max-duration-days", 14);
    }

    public int getDefaultAuctionDurationHours() {
        return config.getInt("market.auctions.default-duration-hours", 72);
    }

    public int getAuctionExpirationCheckInterval() {
        return config.getInt("market.auctions.expiration-check-interval", 30);
    }

    // ==================== MARKET MODULE - SIGNS ====================

    public String getSaleHeader() {
        return config.getString("market.signs.sale-header", "[For Sale]");
    }

    public String getAuctionHeader() {
        return config.getString("market.signs.auction-header", "[Auction]");
    }

    // ==================== MARKET MODULE - GUI ====================

    public String getSaleMenuTitle() {
        return translateColors(config.getString("market.gui.sale-menu-title", "&1Purchase Claim"));
    }

    public String getAuctionMenuTitle() {
        return translateColors(config.getString("market.gui.auction-menu-title", "&1Auction Details"));
    }

    public String getConfirmTitle() {
        return translateColors(config.getString("market.gui.confirm-title", "&4Confirm Purchase"));
    }

    // ==================== UTILITY ====================

    private String translateColors(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
