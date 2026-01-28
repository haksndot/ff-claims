package com.haksnbot.ffclaims.market.listeners;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import com.haksnbot.ffclaims.market.data.AuctionData;
import com.haksnbot.ffclaims.market.data.SaleData;
import com.haksnbot.ffclaims.market.signs.SignManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

public class MarketSignListener implements Listener {

    private final FFClaimsPlugin plugin;

    public MarketSignListener(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = new String[4];
        for (int i = 0; i < 4; i++) {
            lines[i] = event.line(i) != null ?
                    net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                            .serialize(event.line(i)) : "";
        }

        SignManager signManager = plugin.getSignManager();

        if (signManager.isSaleSign(lines)) {
            handleSaleSignCreation(event, player, lines);
            return;
        }

        if (signManager.isAuctionSign(lines)) {
            handleAuctionSignCreation(event, player, lines);
        }
    }

    private void handleSaleSignCreation(SignChangeEvent event, Player player, String[] lines) {
        if (!player.hasPermission("ffclaims.market.sell")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("no-permission"));
            event.setCancelled(true);
            return;
        }

        Location signLocation = event.getBlock().getLocation();

        String validationError = plugin.getListingManager().validateListingCreation(player, signLocation);
        if (validationError != null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + validationError);
            event.setCancelled(true);
            return;
        }

        SignManager.SaleSignInput input;
        try {
            input = plugin.getSignManager().parseSaleSign(lines);
        } catch (IllegalArgumentException e) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + e.getMessage());
            event.setCancelled(true);
            return;
        }

        SaleData sale = plugin.getSaleManager().createSale(player, signLocation, input.getPrice());

        if (sale == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    "Failed to create sale listing.");
            event.setCancelled(true);
            return;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getSignManager().updateSaleSign(signLocation, sale);
        }, 1L);

        String msg = plugin.getConfigManager().getMessage("market.sale-created")
                .replace("%price%", plugin.getVaultHook().format(input.getPrice()));
        player.sendMessage(plugin.getConfigManager().getPrefix() + msg);
    }

    private void handleAuctionSignCreation(SignChangeEvent event, Player player, String[] lines) {
        if (!player.hasPermission("ffclaims.market.auction")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("no-permission"));
            event.setCancelled(true);
            return;
        }

        Location signLocation = event.getBlock().getLocation();

        String validationError = plugin.getListingManager().validateListingCreation(player, signLocation);
        if (validationError != null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + validationError);
            event.setCancelled(true);
            return;
        }

        SignManager.AuctionSignInput input;
        try {
            input = plugin.getSignManager().parseAuctionSign(lines);
        } catch (IllegalArgumentException e) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + e.getMessage());
            event.setCancelled(true);
            return;
        }

        AuctionData auction = plugin.getAuctionManager().createAuction(
                player, signLocation, input.getMinimumBid(), input.getBuyNowPrice(), input.getDurationMs());

        if (auction == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    "Failed to create auction listing.");
            event.setCancelled(true);
            return;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getSignManager().updateAuctionSign(signLocation, auction);
        }, 1L);

        String msg = plugin.getConfigManager().getMessage("market.auction-created")
                .replace("%min%", plugin.getVaultHook().format(input.getMinimumBid()));
        player.sendMessage(plugin.getConfigManager().getPrefix() + msg);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof Sign)) {
            return;
        }

        Location signLocation = block.getLocation();

        if (!plugin.getMarketDataManager().isListingSign(signLocation)) {
            return;
        }

        Player player = event.getPlayer();

        SaleData sale = plugin.getMarketDataManager().getSaleBySign(signLocation);
        if (sale != null) {
            if (!sale.getSellerUUID().equals(player.getUniqueId()) &&
                    !player.hasPermission("ffclaims.admin")) {
                player.sendMessage(plugin.getConfigManager().getPrefix() +
                        "You can only break your own sale signs.");
                event.setCancelled(true);
                return;
            }

            plugin.getMarketDataManager().removeSale(sale.getId());
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("market.sale-cancelled"));
            return;
        }

        AuctionData auction = plugin.getMarketDataManager().getAuctionBySign(signLocation);
        if (auction != null) {
            if (!auction.getSellerUUID().equals(player.getUniqueId()) &&
                    !player.hasPermission("ffclaims.admin")) {
                player.sendMessage(plugin.getConfigManager().getPrefix() +
                        "You can only break your own auction signs.");
                event.setCancelled(true);
                return;
            }

            plugin.getAuctionManager().cancelAuction(auction.getId());
        }
    }
}
