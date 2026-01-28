package com.haksnbot.ffclaims.naming;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.Map;

public class DynmapIntegration {

    private final FFClaimsPlugin plugin;
    private MarkerSet markerSet;

    public DynmapIntegration(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean init() {
        DynmapAPI dynmap = (DynmapAPI) plugin.getServer().getPluginManager().getPlugin("dynmap");
        if (dynmap == null) return false;

        MarkerAPI markerAPI = dynmap.getMarkerAPI();
        if (markerAPI == null) return false;

        markerSet = markerAPI.getMarkerSet("ffclaims");
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet("ffclaims", "Claim Names", null, false);
        }
        if (markerSet == null) return false;

        markerSet.setHideByDefault(false);
        refreshAll();
        return true;
    }

    public void refreshAll() {
        if (markerSet == null) return;

        // Clear existing markers
        for (Marker m : markerSet.getMarkers()) {
            m.deleteMarker();
        }

        // Add all named claims
        for (Map.Entry<Long, String> entry : plugin.getNamingDataManager().getClaimNames().entrySet()) {
            updateMarker(entry.getKey(), entry.getValue());
        }
    }

    public void updateMarker(long claimId, String name) {
        if (markerSet == null) return;

        String markerId = "claim_" + claimId;

        if (name == null) {
            Marker existing = markerSet.findMarker(markerId);
            if (existing != null) existing.deleteMarker();
            return;
        }

        Claim claim = plugin.getGriefPreventionHook().getClaim(claimId);
        if (claim == null) {
            Marker existing = markerSet.findMarker(markerId);
            if (existing != null) existing.deleteMarker();
            return;
        }

        // Place marker at center of claim
        Location lesser = claim.getLesserBoundaryCorner();
        Location greater = claim.getGreaterBoundaryCorner();
        if (lesser == null || greater == null || lesser.getWorld() == null) return;

        double cx = (lesser.getBlockX() + greater.getBlockX()) / 2.0;
        double cz = (lesser.getBlockZ() + greater.getBlockZ()) / 2.0;
        double cy = lesser.getWorld().getHighestBlockYAt((int) cx, (int) cz);
        String world = lesser.getWorld().getName();

        // Strip & color codes for the marker label
        String label = name.replaceAll("&[0-9a-fk-or]", "");

        Marker existing = markerSet.findMarker(markerId);
        if (existing != null) {
            existing.setLabel(label);
            existing.setLocation(world, cx, cy, cz);
        } else {
            markerSet.createMarker(markerId, label, world, cx, cy, cz, null, false);
        }
    }
}
