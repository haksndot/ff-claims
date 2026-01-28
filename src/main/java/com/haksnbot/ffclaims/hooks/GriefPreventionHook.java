package com.haksnbot.ffclaims.hooks;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

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
}
