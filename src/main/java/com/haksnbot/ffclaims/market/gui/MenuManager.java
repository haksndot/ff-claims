package com.haksnbot.ffclaims.market.gui;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import com.haksnbot.ffclaims.market.data.AuctionData;
import com.haksnbot.ffclaims.market.data.SaleData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MenuManager {

    private final FFClaimsPlugin plugin;
    private final Map<UUID, OpenMenu> openMenus = new ConcurrentHashMap<>();

    public MenuManager(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    public void openSaleMenu(Player player, SaleData sale) {
        SaleMenu menu = new SaleMenu(plugin, sale);
        Inventory inventory = menu.createInventory();

        openMenus.put(player.getUniqueId(), new OpenMenu(MenuType.SALE, sale.getId()));
        player.openInventory(inventory);
    }

    public void openAuctionMenu(Player player, AuctionData auction) {
        AuctionMenu menu = new AuctionMenu(plugin, auction);
        Inventory inventory = menu.createInventory();

        openMenus.put(player.getUniqueId(), new OpenMenu(MenuType.AUCTION, auction.getId()));
        player.openInventory(inventory);
    }

    public void openSaleConfirmMenu(Player player, SaleData sale) {
        SaleMenu menu = new SaleMenu(plugin, sale);
        Inventory inventory = menu.createConfirmInventory();

        openMenus.put(player.getUniqueId(), new OpenMenu(MenuType.SALE_CONFIRM, sale.getId()));
        player.openInventory(inventory);
    }

    public void openBidInput(Player player, AuctionData auction) {
        BidInputHandler handler = new BidInputHandler(plugin);
        handler.openBidInput(player, auction);
    }

    public OpenMenu getOpenMenu(Player player) {
        return openMenus.get(player.getUniqueId());
    }

    public void closeMenu(Player player) {
        openMenus.remove(player.getUniqueId());
    }

    public boolean hasMenuOpen(Player player) {
        return openMenus.containsKey(player.getUniqueId());
    }

    public enum MenuType {
        SALE,
        SALE_CONFIRM,
        AUCTION
    }

    public static class OpenMenu {
        private final MenuType type;
        private final String listingId;

        public OpenMenu(MenuType type, String listingId) {
            this.type = type;
            this.listingId = listingId;
        }

        public MenuType getType() {
            return type;
        }

        public String getListingId() {
            return listingId;
        }
    }
}
