package com.haksnbot.ffclaims.market.gui;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import com.haksnbot.ffclaims.market.data.AuctionData;
import com.haksnbot.ffclaims.market.signs.SignFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AuctionMenu {

    public static final int BID_SLOT = 11;
    public static final int BUY_NOW_SLOT = 13;
    public static final int CANCEL_SLOT = 15;
    public static final int INFO_SLOT = 4;

    private final FFClaimsPlugin plugin;
    private final AuctionData auction;

    public AuctionMenu(FFClaimsPlugin plugin, AuctionData auction) {
        this.plugin = plugin;
        this.auction = auction;
    }

    public Inventory createInventory() {
        Inventory inventory = Bukkit.createInventory(null, 27,
                Component.text("Auction Details").color(NamedTextColor.DARK_BLUE));

        fillBorder(inventory);
        inventory.setItem(INFO_SLOT, createInfoItem());
        inventory.setItem(BID_SLOT, createBidItem());

        if (auction.hasBuyNow()) {
            inventory.setItem(BUY_NOW_SLOT, createBuyNowItem());
        }

        inventory.setItem(CANCEL_SLOT, createCancelItem());

        return inventory;
    }

    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.OAK_SIGN);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Claim Auction")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Seller: ")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(auction.getSellerName()).color(NamedTextColor.WHITE)));
        lore.add(Component.text("Minimum Bid: ")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(SignFormatter.formatPriceFull(auction.getMinimumBid())).color(NamedTextColor.YELLOW)));

        if (auction.hasBuyNow()) {
            lore.add(Component.text("Buy Now: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(SignFormatter.formatPriceFull(auction.getBuyNowPrice())).color(NamedTextColor.GREEN)));
        }

        lore.add(Component.text("Size: ")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(auction.getDimensions()).color(NamedTextColor.WHITE)));

        lore.add(Component.empty());
        lore.add(Component.text("Time Remaining: ")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(auction.getTimeRemainingFormatted()).color(NamedTextColor.AQUA)));

        int bidCount = auction.getBids().size();
        lore.add(Component.text("Total Bids: ")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(String.valueOf(bidCount)).color(NamedTextColor.WHITE)));

        lore.add(Component.empty());
        lore.add(Component.text("Vickrey Auction: Winner pays")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.text("the second-highest bid price")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBidItem() {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Place Sealed Bid")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Click to place your bid")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Minimum: " + SignFormatter.formatPriceFull(auction.getMinimumBid()))
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Your bid is sealed - other")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.text("bidders won't see it")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBuyNowItem() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Buy Now!")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Instantly win the auction")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Price: " + SignFormatter.formatPriceFull(auction.getBuyNowPrice()))
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Skip the wait and buy now!")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCancelItem() {
        ItemStack item = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Close")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Click to close this menu")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void fillBorder(Inventory inventory) {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, pane);
            inventory.setItem(18 + i, pane);
        }
        inventory.setItem(9, pane);
        inventory.setItem(17, pane);
    }
}
