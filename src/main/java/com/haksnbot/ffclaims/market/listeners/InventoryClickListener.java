package com.haksnbot.ffclaims.market.listeners;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import com.haksnbot.ffclaims.market.data.AuctionData;
import com.haksnbot.ffclaims.market.data.SaleData;
import com.haksnbot.ffclaims.market.gui.AuctionMenu;
import com.haksnbot.ffclaims.market.gui.MenuManager;
import com.haksnbot.ffclaims.market.gui.SaleMenu;
import com.haksnbot.ffclaims.market.managers.AuctionManager;
import com.haksnbot.ffclaims.market.managers.SaleManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryClickListener implements Listener {

    private final FFClaimsPlugin plugin;

    public InventoryClickListener(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        MenuManager.OpenMenu openMenu = plugin.getMenuManager().getOpenMenu(player);
        if (openMenu == null) {
            return;
        }

        event.setCancelled(true);

        int slot = event.getRawSlot();

        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }

        switch (openMenu.getType()) {
            case SALE -> handleSaleMenuClick(player, openMenu, slot);
            case SALE_CONFIRM -> handleSaleConfirmClick(player, openMenu, slot);
            case AUCTION -> handleAuctionMenuClick(player, openMenu, slot);
        }
    }

    private void handleSaleMenuClick(Player player, MenuManager.OpenMenu openMenu, int slot) {
        SaleData sale = plugin.getMarketDataManager().getSale(openMenu.getListingId());
        if (sale == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    "This listing is no longer available.");
            player.closeInventory();
            return;
        }

        switch (slot) {
            case SaleMenu.PURCHASE_SLOT -> plugin.getMenuManager().openSaleConfirmMenu(player, sale);
            case SaleMenu.CANCEL_SLOT -> player.closeInventory();
        }
    }

    private void handleSaleConfirmClick(Player player, MenuManager.OpenMenu openMenu, int slot) {
        SaleData sale = plugin.getMarketDataManager().getSale(openMenu.getListingId());
        if (sale == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    "This listing is no longer available.");
            player.closeInventory();
            return;
        }

        switch (slot) {
            case SaleMenu.CONFIRM_YES_SLOT -> {
                player.closeInventory();
                SaleManager.PurchaseResult result = plugin.getSaleManager().processPurchase(player, sale);
                player.sendMessage(plugin.getConfigManager().getPrefix() + result.getMessage());
            }
            case SaleMenu.CONFIRM_NO_SLOT -> plugin.getMenuManager().openSaleMenu(player, sale);
        }
    }

    private void handleAuctionMenuClick(Player player, MenuManager.OpenMenu openMenu, int slot) {
        AuctionData auction = plugin.getMarketDataManager().getAuction(openMenu.getListingId());
        if (auction == null || auction.isEnded() || auction.isExpired()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    "This auction is no longer available.");
            player.closeInventory();
            return;
        }

        switch (slot) {
            case AuctionMenu.BID_SLOT -> {
                player.closeInventory();
                plugin.getMenuManager().openBidInput(player, auction);
            }
            case AuctionMenu.BUY_NOW_SLOT -> {
                if (auction.hasBuyNow()) {
                    player.closeInventory();
                    AuctionManager.BidResult result = plugin.getAuctionManager()
                            .placeBid(player, auction, auction.getBuyNowPrice());
                    player.sendMessage(plugin.getConfigManager().getPrefix() + result.getMessage());
                }
            }
            case AuctionMenu.CANCEL_SLOT -> player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        plugin.getMenuManager().closeMenu(player);
    }
}
