package com.haksnbot.ffclaims.market.managers;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import com.haksnbot.ffclaims.hooks.GriefPreventionHook;
import com.haksnbot.ffclaims.market.data.AuctionData;
import com.haksnbot.ffclaims.market.data.SaleData;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListingManager {

    private final FFClaimsPlugin plugin;

    public ListingManager(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Validate that a player can create a listing at the given location.
     * Returns null if valid, or an error message if invalid.
     */
    public String validateListingCreation(Player player, Location signLocation) {
        GriefPreventionHook gpHook = plugin.getGriefPreventionHook();

        // Check claim exists
        Claim claim = gpHook.getClaimAt(signLocation);
        if (claim == null) {
            return plugin.getConfigManager().getMessage("market.not-in-claim");
        }

        // Check ownership
        if (!gpHook.isClaimOwner(claim, player)) {
            return plugin.getConfigManager().getMessage("market.not-claim-owner");
        }

        // Check for admin claims
        if (gpHook.isAdminClaim(claim)) {
            return "Admin claims cannot be sold.";
        }

        // Check for subclaims
        if (gpHook.isSubclaim(claim)) {
            return "Subclaims cannot be sold separately. Sell the parent claim.";
        }

        // Check if already listed
        Location claimLoc = gpHook.getLesserBoundaryCorner(claim);
        if (plugin.getMarketDataManager().hasListingAtClaim(claimLoc)) {
            return plugin.getConfigManager().getMessage("market.already-listed");
        }

        return null; // Valid
    }

    /**
     * Get all active listings (sales and auctions).
     */
    public List<Object> getAllListings() {
        List<Object> listings = new ArrayList<>();
        listings.addAll(plugin.getMarketDataManager().getAllSales());
        listings.addAll(plugin.getMarketDataManager().getActiveAuctions());
        return listings;
    }

    /**
     * Get all listings for a seller.
     */
    public List<Object> getListingsBySeller(UUID sellerUUID) {
        List<Object> listings = new ArrayList<>();
        listings.addAll(plugin.getMarketDataManager().getSalesBySeller(sellerUUID));
        listings.addAll(plugin.getMarketDataManager().getAuctionsBySeller(sellerUUID));
        return listings;
    }

    /**
     * Cancel a listing (sale or auction) at a sign location.
     * Returns true if successful.
     */
    public boolean cancelListing(Location signLocation, Player player) {
        SaleData sale = plugin.getMarketDataManager().getSaleBySign(signLocation);
        if (sale != null) {
            if (!sale.getSellerUUID().equals(player.getUniqueId()) &&
                    !player.hasPermission("ffclaims.admin")) {
                return false;
            }
            return plugin.getSaleManager().cancelSale(sale.getId());
        }

        AuctionData auction = plugin.getMarketDataManager().getAuctionBySign(signLocation);
        if (auction != null) {
            if (!auction.getSellerUUID().equals(player.getUniqueId()) &&
                    !player.hasPermission("ffclaims.admin")) {
                return false;
            }
            return plugin.getAuctionManager().cancelAuction(auction.getId());
        }

        return false;
    }

    /**
     * Get listing info for display.
     */
    public String getListingInfo(Location signLocation) {
        SaleData sale = plugin.getMarketDataManager().getSaleBySign(signLocation);
        if (sale != null) {
            return String.format("Sale: %s for %s by %s",
                    sale.getDimensions(),
                    plugin.getVaultHook().format(sale.getPrice()),
                    sale.getSellerName());
        }

        AuctionData auction = plugin.getMarketDataManager().getAuctionBySign(signLocation);
        if (auction != null) {
            return String.format("Auction: %s, Min: %s, Ends: %s",
                    auction.getDimensions(),
                    plugin.getVaultHook().format(auction.getMinimumBid()),
                    auction.getTimeRemainingFormatted());
        }

        return null;
    }
}
