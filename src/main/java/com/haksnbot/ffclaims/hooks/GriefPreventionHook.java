package com.haksnbot.ffclaims.hooks;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collection;
import java.util.UUID;

/**
 * Shared GriefPrevention hook for both naming and market modules.
 */
public class GriefPreventionHook {

    private final GriefPrevention griefPrevention;

    public GriefPreventionHook() {
        this.griefPrevention = GriefPrevention.instance;
    }

    public boolean isAvailable() {
        return griefPrevention != null;
    }

    public GriefPrevention getGriefPrevention() {
        return griefPrevention;
    }

    /**
     * Get the claim at a specific location.
     */
    public Claim getClaimAt(Location location) {
        return griefPrevention.dataStore.getClaimAt(location, true, null);
    }

    /**
     * Get the claim at a location (with option to ignore height).
     */
    public Claim getClaimAt(Location location, boolean ignoreHeight) {
        return griefPrevention.dataStore.getClaimAt(location, ignoreHeight, null);
    }

    /**
     * Get a claim by its ID.
     */
    public Claim getClaim(long claimId) {
        return griefPrevention.dataStore.getClaim(claimId);
    }

    /**
     * Check if a player owns the claim.
     */
    public boolean isClaimOwner(Claim claim, UUID playerUUID) {
        if (claim == null || playerUUID == null) {
            return false;
        }
        return playerUUID.equals(claim.ownerID);
    }

    /**
     * Check if a player owns the claim (by OfflinePlayer).
     */
    public boolean isClaimOwner(Claim claim, OfflinePlayer player) {
        if (player == null) {
            return false;
        }
        return isClaimOwner(claim, player.getUniqueId());
    }

    /**
     * Get the owner UUID of a claim.
     */
    public UUID getClaimOwner(Claim claim) {
        if (claim == null) {
            return null;
        }
        return claim.ownerID;
    }

    /**
     * Get the claim ID.
     */
    public Long getClaimId(Claim claim) {
        if (claim == null) {
            return null;
        }
        return claim.getID();
    }

    /**
     * Transfer claim ownership to a new owner.
     */
    public boolean transferClaim(Claim claim, UUID newOwnerUUID) {
        if (claim == null || newOwnerUUID == null) {
            return false;
        }

        try {
            griefPrevention.dataStore.changeClaimOwner(claim, newOwnerUUID);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get claim dimensions as a string (e.g., "32x32").
     */
    public String getClaimDimensions(Claim claim) {
        if (claim == null) {
            return "Unknown";
        }

        int width = claim.getGreaterBoundaryCorner().getBlockX() - claim.getLesserBoundaryCorner().getBlockX() + 1;
        int length = claim.getGreaterBoundaryCorner().getBlockZ() - claim.getLesserBoundaryCorner().getBlockZ() + 1;

        return width + "x" + length;
    }

    /**
     * Get claim area in blocks.
     */
    public int getClaimArea(Claim claim) {
        if (claim == null) {
            return 0;
        }
        return claim.getArea();
    }

    /**
     * Get the lesser (min) boundary corner of a claim.
     */
    public Location getLesserBoundaryCorner(Claim claim) {
        if (claim == null) {
            return null;
        }
        return claim.getLesserBoundaryCorner();
    }

    /**
     * Get the greater (max) boundary corner of a claim.
     */
    public Location getGreaterBoundaryCorner(Claim claim) {
        if (claim == null) {
            return null;
        }
        return claim.getGreaterBoundaryCorner();
    }

    /**
     * Check if a location is inside a specific claim.
     */
    public boolean isInsideClaim(Claim claim, Location location) {
        if (claim == null || location == null) {
            return false;
        }
        return claim.contains(location, true, false);
    }

    /**
     * Check if this is an admin claim (no owner).
     */
    public boolean isAdminClaim(Claim claim) {
        if (claim == null) {
            return false;
        }
        return claim.ownerID == null;
    }

    /**
     * Check if this is a subclaim.
     */
    public boolean isSubclaim(Claim claim) {
        if (claim == null) {
            return false;
        }
        return claim.parent != null;
    }

    /**
     * Get player data for a UUID.
     */
    public PlayerData getPlayerData(UUID playerUUID) {
        return griefPrevention.dataStore.getPlayerData(playerUUID);
    }

    /**
     * Get all claims in the data store.
     */
    public Collection<Claim> getAllClaims() {
        return griefPrevention.dataStore.getClaims();
    }

    /**
     * Check if a player has build permission in a claim.
     */
    public String allowBuild(Claim claim, org.bukkit.entity.Player player) {
        if (claim == null) {
            return null;
        }
        return claim.allowBuild(player, null);
    }

    /**
     * Get the claim block purchase cost from GriefPrevention config.
     * Returns 0 if not configured or unavailable.
     */
    public double getClaimBlockPurchaseCost() {
        try {
            FileConfiguration gpConfig = griefPrevention.getConfig();
            return gpConfig.getDouble("GriefPrevention.Economy.ClaimBlocksPurchaseCost", 0.0);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Check if claim block purchasing with economy is enabled in GP.
     */
    public boolean isEconomyModeEnabled() {
        return getClaimBlockPurchaseCost() > 0;
    }

    /**
     * Calculate the minimum sensible price for a claim based on claim block value.
     * Returns 0 if economy mode is not enabled.
     */
    public double getClaimBlockValue(Claim claim) {
        if (claim == null) {
            return 0;
        }
        double costPerBlock = getClaimBlockPurchaseCost();
        if (costPerBlock <= 0) {
            return 0;
        }
        return claim.getArea() * costPerBlock;
    }

    /**
     * Get a player's total claim blocks (accrued + bonus).
     */
    public int getPlayerTotalClaimBlocks(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        if (playerData == null) {
            return 0;
        }
        return playerData.getAccruedClaimBlocks() + playerData.getBonusClaimBlocks();
    }

    /**
     * Get a player's remaining (unused) claim blocks.
     */
    public int getPlayerRemainingClaimBlocks(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        if (playerData == null) {
            return 0;
        }
        return playerData.getRemainingClaimBlocks();
    }

    /**
     * Adjust a player's bonus claim blocks by a delta amount.
     * Positive delta adds blocks, negative removes blocks.
     */
    public void adjustPlayerBonusClaimBlocks(UUID playerUUID, int delta) {
        PlayerData playerData = getPlayerData(playerUUID);
        if (playerData == null) {
            return;
        }
        int currentBonus = playerData.getBonusClaimBlocks();
        playerData.setBonusClaimBlocks(currentBonus + delta);
        griefPrevention.dataStore.savePlayerData(playerUUID, playerData);
    }

    /**
     * Transfer claim blocks from seller to buyer as part of a sale.
     * The claim area is removed from seller's effective balance and added to buyer's.
     */
    public void transferClaimBlocks(UUID sellerUUID, UUID buyerUUID, int claimArea) {
        // Seller loses the claim blocks (they were "invested" in the claim)
        // We don't need to remove from seller - the claim ownership change handles that
        // But we DO need to give the buyer enough blocks if they don't have them

        // Actually, GP's changeClaimOwner just reassigns. The buyer might not have enough
        // blocks to "afford" the claim in their balance. We need to grant them bonus blocks
        // equal to the claim size to ensure they can own it.
        adjustPlayerBonusClaimBlocks(buyerUUID, claimArea);
    }
}
