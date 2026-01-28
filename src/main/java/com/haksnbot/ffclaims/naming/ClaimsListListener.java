package com.haksnbot.ffclaims.naming;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Vector;

public class ClaimsListListener implements Listener {

    private final FFClaimsPlugin plugin;

    public ClaimsListListener(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase().trim();
        if (!msg.equals("/claimslist") && !msg.startsWith("/claimslist ")) {
            return;
        }

        Player player = event.getPlayer();

        // Parse target player name if provided (admins can view others' claims)
        String targetName = null;
        String[] parts = event.getMessage().trim().split("\\s+", 2);
        if (parts.length > 1) {
            targetName = parts[1];
        }

        PlayerData playerData;
        String ownerName;

        if (targetName != null) {
            // Admin viewing another player — check permission
            if (!player.hasPermission("griefprevention.claimslistother")) {
                // Let GP handle the denial
                return;
            }
            @SuppressWarnings("deprecation")
            org.bukkit.OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(targetName);
            if (target.getUniqueId() == null) {
                player.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                event.setCancelled(true);
                return;
            }
            playerData = GriefPrevention.instance.dataStore.getPlayerData(target.getUniqueId());
            ownerName = target.getName() != null ? target.getName() : targetName;
        } else {
            playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
            ownerName = player.getName();
        }

        event.setCancelled(true);

        Vector<Claim> claims = playerData.getClaims();

        if (claims.isEmpty()) {
            player.sendMessage(Component.text("No claims found for " + ownerName + ".", NamedTextColor.YELLOW));
            return;
        }

        player.sendMessage(Component.text(ownerName + "'s Claims:", NamedTextColor.GOLD));

        int index = 1;
        int totalBlocks = 0;
        for (Claim claim : claims) {
            var lesser = claim.getLesserBoundaryCorner();
            int area = claim.getArea();
            totalBlocks += area;

            String coords = lesser.getWorld().getName()
                    + " (" + lesser.getBlockX() + ", " + lesser.getBlockY() + ", " + lesser.getBlockZ() + ")";

            String claimName = plugin.getNamingDataManager().getClaimName(claim.getID());

            Component line;
            if (claimName != null) {
                Component nameComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(claimName);
                line = Component.text(index + ". ", NamedTextColor.WHITE)
                        .append(nameComponent)
                        .append(Component.text(" — " + coords + " — " + area + " blocks", NamedTextColor.GRAY));
            } else {
                line = Component.text(index + ". ", NamedTextColor.WHITE)
                        .append(Component.text(coords + " — " + area + " blocks", NamedTextColor.GRAY));
            }

            player.sendMessage(line);
            index++;
        }

        player.sendMessage(Component.text("Total: " + claims.size() + " claims, " + totalBlocks + " blocks used.",
                NamedTextColor.YELLOW));
    }
}
