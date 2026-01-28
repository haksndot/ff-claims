package com.haksnbot.ffclaims.naming;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NameClaimCommand implements CommandExecutor {

    private final FFClaimsPlugin plugin;

    public NameClaimCommand(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Claim claim = plugin.getGriefPreventionHook().getClaimAt(player.getLocation(), false);
        if (claim == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("naming.not-in-claim"));
            return true;
        }

        long claimId = claim.getID();
        boolean isAdmin = player.hasPermission("ffclaims.naming.admin");

        // Check ownership: must be owner or trusted (or admin)
        if (!isAdmin) {
            if (claim.getOwnerID() == null || !claim.getOwnerID().equals(player.getUniqueId())) {
                // Check if trusted
                String trustError = claim.allowBuild(player, null);
                if (trustError != null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("naming.no-permission"));
                    return true;
                }
            }
        }

        // No args: show current name
        if (args.length == 0) {
            String name = plugin.getNamingDataManager().getClaimName(claimId);
            if (name != null) {
                player.sendMessage(plugin.getConfigManager().getMessage("naming.current-name")
                        .replace("%name%", ChatColor.translateAlternateColorCodes('&', name)));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("naming.no-name"));
            }
            return true;
        }

        // "clear" subcommand
        if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
            plugin.getNamingDataManager().removeClaimName(claimId);
            if (plugin.getDynmapIntegration() != null) {
                plugin.getDynmapIntegration().updateMarker(claimId, null);
            }
            player.sendMessage(plugin.getConfigManager().getMessage("naming.claim-name-cleared"));
            return true;
        }

        // Set name
        String name = String.join(" ", args);

        // Strip color codes for length check
        String stripped = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name));
        int maxLen = plugin.getConfigManager().getMaxNameLength();
        if (stripped.length() > maxLen) {
            player.sendMessage(plugin.getConfigManager().getMessage("naming.name-too-long")
                    .replace("%max%", String.valueOf(maxLen)));
            return true;
        }

        // Store with & codes (translate on display)
        plugin.getNamingDataManager().setClaimName(claimId, name);
        if (plugin.getDynmapIntegration() != null) {
            plugin.getDynmapIntegration().updateMarker(claimId, name);
        }

        String colorized = ChatColor.translateAlternateColorCodes('&', name);
        player.sendMessage(plugin.getConfigManager().getMessage("naming.claim-named")
                .replace("%name%", colorized));
        return true;
    }
}
