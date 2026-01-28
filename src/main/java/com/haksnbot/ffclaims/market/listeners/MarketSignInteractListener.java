package com.haksnbot.ffclaims.market.listeners;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import com.haksnbot.ffclaims.market.data.AuctionData;
import com.haksnbot.ffclaims.market.data.SaleData;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class MarketSignInteractListener implements Listener {

    private final FFClaimsPlugin plugin;

    public MarketSignInteractListener(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Sign)) {
            return;
        }

        Location signLocation = block.getLocation();

        if (!plugin.getMarketDataManager().isListingSign(signLocation)) {
            return;
        }

        Player player = event.getPlayer();
        event.setCancelled(true);

        if (!player.hasPermission("ffclaims.market.buy")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        SaleData sale = plugin.getMarketDataManager().getSaleBySign(signLocation);
        if (sale != null) {
            handleSaleSignClick(player, sale);
            return;
        }

        AuctionData auction = plugin.getMarketDataManager().getAuctionBySign(signLocation);
        if (auction != null) {
            handleAuctionSignClick(player, auction);
        }
    }

    private void handleSaleSignClick(Player player, SaleData sale) {
        if (sale.getSellerUUID().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    "This is your sale listing. Break the sign to cancel.");
            return;
        }

        plugin.getMenuManager().openSaleMenu(player, sale);
    }

    private void handleAuctionSignClick(Player player, AuctionData auction) {
        if (auction.isEnded() || auction.isExpired()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    "This auction has ended.");
            return;
        }

        if (auction.getSellerUUID().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    "This is your auction listing. Break the sign to cancel.");
            int bidCount = auction.getBids().size();
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    "Current bids: " + bidCount);
            return;
        }

        plugin.getMenuManager().openAuctionMenu(player, auction);
    }
}
