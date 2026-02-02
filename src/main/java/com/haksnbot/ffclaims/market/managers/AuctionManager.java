package com.haksnbot.ffclaims.market.managers;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import com.haksnbot.ffclaims.hooks.GriefPreventionHook;
import com.haksnbot.ffclaims.hooks.VaultHook;
import com.haksnbot.ffclaims.market.data.AuctionData;
import com.haksnbot.ffclaims.market.data.BidData;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AuctionManager {

    private final FFClaimsPlugin plugin;

    public AuctionManager(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Create a new auction listing.
     */
    public AuctionData createAuction(Player seller, Location signLocation,
                                      double minimumBid, double buyNowPrice, long durationMs) {
        GriefPreventionHook gpHook = plugin.getGriefPreventionHook();
        Claim claim = gpHook.getClaimAt(signLocation);

        if (claim == null) {
            return null;
        }

        String id = AuctionData.generateId();
        Location claimLocation = gpHook.getLesserBoundaryCorner(claim);
        int area = gpHook.getClaimArea(claim);
        String dimensions = gpHook.getClaimDimensions(claim);
        long now = System.currentTimeMillis();

        AuctionData auction = new AuctionData(
                id,
                seller.getUniqueId(),
                seller.getName(),
                minimumBid,
                buyNowPrice,
                signLocation,
                claimLocation,
                now,
                now + durationMs,
                area,
                dimensions
        );

        plugin.getMarketDataManager().addAuction(auction);
        plugin.getSignManager().updateAuctionSign(signLocation, auction);

        return auction;
    }

    /**
     * Place a sealed bid on an auction.
     */
    public BidResult placeBid(Player bidder, AuctionData auction, double amount) {
        VaultHook vault = plugin.getVaultHook();

        // Cannot bid on your own auction
        if (auction.getSellerUUID().equals(bidder.getUniqueId())) {
            return new BidResult(false, "You cannot bid on your own auction.", false);
        }

        // Check auction is still active
        if (auction.isEnded() || auction.isExpired()) {
            return new BidResult(false, "This auction has ended.", false);
        }

        // Check bid meets minimum
        if (amount < auction.getMinimumBid()) {
            return new BidResult(false,
                    "Bid must be at least " + vault.format(auction.getMinimumBid()), false);
        }

        // Check player has enough money
        if (!vault.hasBalance(bidder, amount)) {
            String msg = plugin.getConfigManager().getMessage("market.insufficient-funds")
                    .replace("%price%", vault.format(amount));
            return new BidResult(false, msg, false);
        }

        // Check if this is a buy-now
        if (auction.hasBuyNow() && amount >= auction.getBuyNowPrice()) {
            return processBuyNow(bidder, auction);
        }

        // Check if player already has a bid
        BidData existingBid = auction.getBidByPlayer(bidder.getUniqueId());
        if (existingBid != null) {
            if (amount <= existingBid.getAmount()) {
                return new BidResult(false,
                        "Your new bid must be higher than your existing bid of " +
                                vault.format(existingBid.getAmount()), false);
            }
        }

        // Record the sealed bid
        BidData bid = new BidData(
                bidder.getUniqueId(),
                bidder.getName(),
                amount,
                System.currentTimeMillis()
        );

        auction.addBid(bid);
        plugin.getMarketDataManager().updateAuction(auction);

        String msg = plugin.getConfigManager().getMessage("market.bid-placed")
                .replace("%amount%", vault.format(amount));

        return new BidResult(true, msg, false);
    }

    /**
     * Process a buy-now purchase.
     */
    private BidResult processBuyNow(Player buyer, AuctionData auction) {
        VaultHook vault = plugin.getVaultHook();
        GriefPreventionHook gpHook = plugin.getGriefPreventionHook();
        double price = auction.getBuyNowPrice();

        // Get seller
        OfflinePlayer seller = Bukkit.getOfflinePlayer(auction.getSellerUUID());

        // Verify claim still exists
        Claim claim = gpHook.getClaimAt(auction.getSignLocation());
        if (claim == null) {
            cancelAuction(auction.getId());
            return new BidResult(false, "This claim no longer exists.", false);
        }

        // === TRANSACTION START ===

        // 1. Withdraw from buyer
        if (!vault.withdraw(buyer, price)) {
            return new BidResult(false, "Failed to withdraw payment.", false);
        }

        // 2. Deposit to seller
        if (!vault.deposit(seller, price)) {
            vault.deposit(buyer, price);
            return new BidResult(false, "Failed to pay seller. Transaction cancelled.", false);
        }

        // 3. Transfer claim
        if (!gpHook.transferClaim(claim, buyer.getUniqueId())) {
            vault.withdraw(seller, price);
            vault.deposit(buyer, price);
            return new BidResult(false, "Failed to transfer claim. Transaction cancelled.", false);
        }

        // === TRANSACTION SUCCESS ===

        // Transfer claim blocks to buyer
        int claimArea = gpHook.getClaimArea(claim);
        gpHook.transferClaimBlocks(auction.getSellerUUID(), buyer.getUniqueId(), claimArea);

        // Get claim name if available
        String claimName = getClaimName(claim);

        // Log the transaction
        Location claimLoc = auction.getClaimLocation();
        String claimLocStr = String.format("%s @ %d, %d, %d",
                claimLoc.getWorld().getName(),
                claimLoc.getBlockX(), claimLoc.getBlockY(), claimLoc.getBlockZ());

        plugin.getTransactionLogger().logBuyNow(
                auction.getSellerUUID(), auction.getSellerName(),
                buyer.getUniqueId(), buyer.getName(),
                price, claimArea, auction.getDimensions(),
                claimLocStr, claimName
        );

        // Mark auction as ended
        auction.setEnded(true);
        plugin.getSignManager().removeSign(auction.getSignLocation());
        plugin.getMarketDataManager().removeAuction(auction.getId());

        // Notify seller
        Player sellerOnline = Bukkit.getPlayer(auction.getSellerUUID());
        if (sellerOnline != null) {
            sellerOnline.sendMessage(plugin.getConfigManager().getPrefix() +
                    buyer.getName() + " bought your claim instantly for " + vault.format(price) + "!");
        }

        String msg = plugin.getConfigManager().getMessage("market.purchase-success")
                .replace("%price%", vault.format(price));

        return new BidResult(true, msg, true);
    }

    /**
     * Process an expired auction.
     */
    public void processExpiredAuction(AuctionData auction) {
        if (auction.isEnded()) {
            return;
        }

        VaultHook vault = plugin.getVaultHook();
        GriefPreventionHook gpHook = plugin.getGriefPreventionHook();

        BidData highestBid = auction.getHighestBid();

        // No bids - just remove the auction
        if (highestBid == null) {
            auction.setEnded(true);
            plugin.getSignManager().removeSign(auction.getSignLocation());
            plugin.getMarketDataManager().removeAuction(auction.getId());

            Player seller = Bukkit.getPlayer(auction.getSellerUUID());
            if (seller != null) {
                seller.sendMessage(plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMessage("market.no-bids-expired"));
            }
            return;
        }

        // Calculate Vickrey price
        double vickreyPrice = auction.getVickreyPrice();

        OfflinePlayer winner = Bukkit.getOfflinePlayer(highestBid.getBidderUUID());
        OfflinePlayer seller = Bukkit.getOfflinePlayer(auction.getSellerUUID());

        // Verify claim still exists
        Claim claim = gpHook.getClaimAt(auction.getSignLocation());
        if (claim == null) {
            auction.setEnded(true);
            plugin.getMarketDataManager().removeAuction(auction.getId());
            plugin.getLogger().warning("Auction " + auction.getId() + " claim no longer exists!");
            return;
        }

        // Check winner can pay
        if (!vault.hasBalance(winner, vickreyPrice)) {
            plugin.getLogger().warning("Auction winner " + highestBid.getBidderName() +
                    " cannot afford " + vickreyPrice);
            auction.setEnded(true);
            plugin.getSignManager().removeSign(auction.getSignLocation());
            plugin.getMarketDataManager().removeAuction(auction.getId());

            notifyPlayer(auction.getSellerUUID(),
                    "Your auction ended but the winner couldn't pay. Auction cancelled.");
            return;
        }

        // === TRANSACTION START ===

        // 1. Withdraw from winner
        if (!vault.withdraw(winner, vickreyPrice)) {
            plugin.getLogger().severe("Failed to withdraw from auction winner!");
            return;
        }

        // 2. Deposit to seller
        if (!vault.deposit(seller, vickreyPrice)) {
            vault.deposit(winner, vickreyPrice);
            plugin.getLogger().severe("Failed to deposit to auction seller!");
            return;
        }

        // 3. Transfer claim
        if (!gpHook.transferClaim(claim, highestBid.getBidderUUID())) {
            vault.withdraw(seller, vickreyPrice);
            vault.deposit(winner, vickreyPrice);
            plugin.getLogger().severe("Failed to transfer claim for auction!");
            return;
        }

        // === TRANSACTION SUCCESS ===

        // Transfer claim blocks to winner
        int claimArea = gpHook.getClaimArea(claim);
        gpHook.transferClaimBlocks(auction.getSellerUUID(), highestBid.getBidderUUID(), claimArea);

        // Get claim name if available
        String claimName = getClaimName(claim);

        // Log the transaction
        Location claimLoc = auction.getClaimLocation();
        String claimLocStr = String.format("%s @ %d, %d, %d",
                claimLoc.getWorld().getName(),
                claimLoc.getBlockX(), claimLoc.getBlockY(), claimLoc.getBlockZ());

        plugin.getTransactionLogger().logAuction(
                auction.getSellerUUID(), auction.getSellerName(),
                highestBid.getBidderUUID(), highestBid.getBidderName(),
                highestBid.getAmount(), vickreyPrice, auction.getBids().size(),
                claimArea, auction.getDimensions(),
                claimLocStr, claimName
        );

        auction.setEnded(true);
        plugin.getSignManager().removeSign(auction.getSignLocation());
        plugin.getMarketDataManager().removeAuction(auction.getId());

        // Notify winner
        String winnerMsg = plugin.getConfigManager().getMessage("market.bid-won")
                .replace("%price%", vault.format(vickreyPrice));
        notifyPlayer(highestBid.getBidderUUID(), winnerMsg);

        // Notify seller
        String sellerMsg = plugin.getConfigManager().getMessage("market.auction-won-seller")
                .replace("%winner%", highestBid.getBidderName())
                .replace("%price%", vault.format(vickreyPrice));
        notifyPlayer(auction.getSellerUUID(), sellerMsg);
    }

    /**
     * Cancel an auction.
     */
    public boolean cancelAuction(String auctionId) {
        AuctionData auction = plugin.getMarketDataManager().getAuction(auctionId);
        if (auction == null) {
            return false;
        }

        auction.setEnded(true);
        plugin.getSignManager().removeSign(auction.getSignLocation());
        plugin.getMarketDataManager().removeAuction(auctionId);

        // Notify seller
        notifyPlayer(auction.getSellerUUID(),
                plugin.getConfigManager().getMessage("market.auction-cancelled"));

        // Notify all bidders
        for (BidData bid : auction.getBids()) {
            notifyPlayer(bid.getBidderUUID(),
                    "An auction you bid on has been cancelled by the seller.");
        }

        return true;
    }

    /**
     * Update all auction signs (to refresh time remaining).
     */
    public void updateAuctionSigns() {
        for (AuctionData auction : plugin.getMarketDataManager().getActiveAuctions()) {
            plugin.getSignManager().updateAuctionSign(auction.getSignLocation(), auction);
        }
    }

    private void notifyPlayer(UUID playerUUID, String message) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + message);
        }
    }

    private String getClaimName(Claim claim) {
        if (!plugin.isNamingEnabled() || plugin.getNamingDataManager() == null) {
            return null;
        }
        Long claimId = plugin.getGriefPreventionHook().getClaimId(claim);
        if (claimId == null) {
            return null;
        }
        return plugin.getNamingDataManager().getClaimName(claimId);
    }

    /**
     * Result of a bid attempt.
     */
    public static class BidResult {
        private final boolean success;
        private final String message;
        private final boolean instantWin;

        public BidResult(boolean success, String message, boolean instantWin) {
            this.success = success;
            this.message = message;
            this.instantWin = instantWin;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public boolean isInstantWin() {
            return instantWin;
        }
    }
}
