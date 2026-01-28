package com.haksnbot.ffclaims.market.managers;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import com.haksnbot.ffclaims.hooks.GriefPreventionHook;
import com.haksnbot.ffclaims.hooks.VaultHook;
import com.haksnbot.ffclaims.market.data.SaleData;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class SaleManager {

    private final FFClaimsPlugin plugin;

    public SaleManager(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Create a new sale listing.
     */
    public SaleData createSale(Player seller, Location signLocation, double price) {
        GriefPreventionHook gpHook = plugin.getGriefPreventionHook();
        Claim claim = gpHook.getClaimAt(signLocation);

        if (claim == null) {
            return null;
        }

        String id = SaleData.generateId();
        Location claimLocation = gpHook.getLesserBoundaryCorner(claim);
        int area = gpHook.getClaimArea(claim);
        String dimensions = gpHook.getClaimDimensions(claim);

        SaleData sale = new SaleData(
                id,
                seller.getUniqueId(),
                seller.getName(),
                price,
                signLocation,
                claimLocation,
                System.currentTimeMillis(),
                area,
                dimensions
        );

        plugin.getMarketDataManager().addSale(sale);
        plugin.getSignManager().updateSaleSign(signLocation, sale);

        return sale;
    }

    /**
     * Process a purchase of a sale listing.
     */
    public PurchaseResult processPurchase(Player buyer, SaleData sale) {
        VaultHook vault = plugin.getVaultHook();
        GriefPreventionHook gpHook = plugin.getGriefPreventionHook();

        // Cannot buy your own listing
        if (sale.getSellerUUID().equals(buyer.getUniqueId())) {
            return new PurchaseResult(false, "You cannot purchase your own listing.");
        }

        // Check buyer has funds
        if (!vault.hasBalance(buyer, sale.getPrice())) {
            String msg = plugin.getConfigManager().getMessage("market.insufficient-funds")
                    .replace("%price%", vault.format(sale.getPrice()));
            return new PurchaseResult(false, msg);
        }

        // Get seller
        OfflinePlayer seller = Bukkit.getOfflinePlayer(sale.getSellerUUID());

        // Verify claim still exists and seller still owns it
        Claim claim = gpHook.getClaimAt(sale.getSignLocation());
        if (claim == null) {
            cancelSale(sale.getId());
            return new PurchaseResult(false, "This claim no longer exists.");
        }

        if (!gpHook.isClaimOwner(claim, sale.getSellerUUID())) {
            cancelSale(sale.getId());
            return new PurchaseResult(false, "The seller no longer owns this claim.");
        }

        // === TRANSACTION START ===

        // 1. Withdraw from buyer
        if (!vault.withdraw(buyer, sale.getPrice())) {
            return new PurchaseResult(false, "Failed to withdraw payment.");
        }

        // 2. Deposit to seller
        if (!vault.deposit(seller, sale.getPrice())) {
            // Rollback: refund buyer
            vault.deposit(buyer, sale.getPrice());
            return new PurchaseResult(false, "Failed to pay seller. Transaction cancelled.");
        }

        // 3. Transfer claim
        if (!gpHook.transferClaim(claim, buyer.getUniqueId())) {
            // Rollback: refund both
            vault.withdraw(seller, sale.getPrice());
            vault.deposit(buyer, sale.getPrice());
            return new PurchaseResult(false, "Failed to transfer claim. Transaction cancelled.");
        }

        // === TRANSACTION SUCCESS ===

        // Remove the sale listing
        plugin.getSignManager().removeSign(sale.getSignLocation());
        plugin.getMarketDataManager().removeSale(sale.getId());

        // Notify seller if online
        Player sellerOnline = Bukkit.getPlayer(sale.getSellerUUID());
        if (sellerOnline != null) {
            sellerOnline.sendMessage(plugin.getConfigManager().getPrefix() +
                    buyer.getName() + " purchased your claim for " + vault.format(sale.getPrice()) + "!");
        }

        String successMsg = plugin.getConfigManager().getMessage("market.purchase-success")
                .replace("%price%", vault.format(sale.getPrice()));

        return new PurchaseResult(true, successMsg);
    }

    /**
     * Cancel a sale listing.
     */
    public boolean cancelSale(String saleId) {
        SaleData sale = plugin.getMarketDataManager().getSale(saleId);
        if (sale == null) {
            return false;
        }

        plugin.getSignManager().removeSign(sale.getSignLocation());
        plugin.getMarketDataManager().removeSale(saleId);

        // Notify seller
        Player seller = Bukkit.getPlayer(sale.getSellerUUID());
        if (seller != null) {
            seller.sendMessage(plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("market.sale-cancelled"));
        }

        return true;
    }

    /**
     * Result of a purchase attempt.
     */
    public static class PurchaseResult {
        private final boolean success;
        private final String message;

        public PurchaseResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
