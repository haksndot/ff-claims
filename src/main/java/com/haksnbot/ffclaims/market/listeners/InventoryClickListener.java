package com.haksnbot.ffclaims.market.listeners;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import com.haksnbot.ffclaims.market.data.AuctionData;
import com.haksnbot.ffclaims.market.data.SaleData;
import com.haksnbot.ffclaims.market.gui.AuctionMenu;
import com.haksnbot.ffclaims.market.gui.MenuManager;
import com.haksnbot.ffclaims.market.gui.SaleMenu;
import com.haksnbot.ffclaims.market.managers.AuctionManager;
import com.haksnbot.ffclaims.market.managers.SaleManager;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Set;

public class InventoryClickListener implements Listener {

    private final FFClaimsPlugin plugin;

    // Menu titles we manage - clicks in these are ALWAYS cancelled
    private static final Set<String> MENU_TITLES = Set.of(
            "Purchase Claim",
            "Confirm Purchase",
            "Auction Details"
    );

    public InventoryClickListener(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Check if this is one of our menus by title
        String title = PlainTextComponentSerializer.plainText()
                .serialize(event.getView().title());

        if (!MENU_TITLES.contains(title)) {
            return;
        }

        // ALWAYS cancel clicks in our menus - prevents item theft
        event.setCancelled(true);

        // Now check if we have tracking info to process the click
        MenuManager.OpenMenu openMenu = plugin.getMenuManager().getOpenMenu(player);
        if (openMenu == null) {
            // Menu tracking lost (shouldn't happen), just block the click
            return;
        }

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

        // Delay removal by 1 tick to allow menu transitions
        // (opening a new menu closes the old one first)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Only remove if player doesn't have another menu open
            if (!plugin.getMenuManager().hasMenuOpen(player)) {
                return;
            }
            // Check if the player still has an inventory open that's one of ours
            String title = PlainTextComponentSerializer.plainText()
                    .serialize(player.getOpenInventory().title());
            if (!MENU_TITLES.contains(title)) {
                plugin.getMenuManager().closeMenu(player);
            }
        }, 1L);
    }
}
