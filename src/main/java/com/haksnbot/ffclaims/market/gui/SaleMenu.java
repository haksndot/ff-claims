package com.haksnbot.ffclaims.market.gui;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import com.haksnbot.ffclaims.market.data.SaleData;
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

public class SaleMenu {

    public static final int PURCHASE_SLOT = 11;
    public static final int CANCEL_SLOT = 15;
    public static final int CONFIRM_YES_SLOT = 11;
    public static final int CONFIRM_NO_SLOT = 15;
    public static final int INFO_SLOT = 4;

    private final FFClaimsPlugin plugin;
    private final SaleData sale;

    public SaleMenu(FFClaimsPlugin plugin, SaleData sale) {
        this.plugin = plugin;
        this.sale = sale;
    }

    public Inventory createInventory() {
        Inventory inventory = Bukkit.createInventory(null, 27,
                Component.text("Purchase Claim").color(NamedTextColor.DARK_BLUE));

        fillBorder(inventory);
        inventory.setItem(INFO_SLOT, createInfoItem());
        inventory.setItem(PURCHASE_SLOT, createPurchaseItem());
        inventory.setItem(CANCEL_SLOT, createCancelItem());

        return inventory;
    }

    public Inventory createConfirmInventory() {
        Inventory inventory = Bukkit.createInventory(null, 27,
                Component.text("Confirm Purchase").color(NamedTextColor.DARK_RED));

        fillBorder(inventory);
        inventory.setItem(INFO_SLOT, createInfoItem());
        inventory.setItem(CONFIRM_YES_SLOT, createConfirmItem());
        inventory.setItem(CONFIRM_NO_SLOT, createCancelItem());

        return inventory;
    }

    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.OAK_SIGN);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Claim For Sale")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Seller: ")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(sale.getSellerName()).color(NamedTextColor.WHITE)));
        lore.add(Component.text("Price: ")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(SignFormatter.formatPriceFull(sale.getPrice())).color(NamedTextColor.GREEN)));
        lore.add(Component.text("Size: ")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(sale.getDimensions()).color(NamedTextColor.WHITE)));
        lore.add(Component.text("Area: ")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(sale.getArea() + " blocks").color(NamedTextColor.WHITE)));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPurchaseItem() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Purchase Claim")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Click to purchase this claim")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("for " + SignFormatter.formatPriceFull(sale.getPrice()))
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createConfirmItem() {
        ItemStack item = new ItemStack(Material.LIME_WOOL);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("CONFIRM PURCHASE")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("You will pay:")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(SignFormatter.formatPriceFull(sale.getPrice()))
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("This cannot be undone!")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCancelItem() {
        ItemStack item = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Cancel")
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
