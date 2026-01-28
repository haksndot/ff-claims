package com.haksnbot.ffclaims.naming;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;

public class ClaimTopCommand implements CommandExecutor {

    private final FFClaimsPlugin plugin;
    private static final int PAGE_SIZE = 10;

    public ClaimTopCommand(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getConfigManager().getMessage("naming.claimtop-invalid-page"));
                return true;
            }
        }

        DataStore dataStore = GriefPrevention.instance.dataStore;

        // Collect claim blocks used per player
        Map<UUID, Long> usedBlocks = new HashMap<>();
        for (Claim claim : dataStore.getClaims()) {
            UUID owner = claim.getOwnerID();
            if (owner == null) continue; // Skip admin claims

            long area = claim.getArea();
            usedBlocks.merge(owner, area, Long::sum);
        }

        // Build entries with total blocks
        List<ClaimTopEntry> entries = new ArrayList<>();
        Set<UUID> processedPlayers = new HashSet<>(usedBlocks.keySet());

        for (UUID uuid : processedPlayers) {
            PlayerData playerData = dataStore.getPlayerData(uuid);
            long used = usedBlocks.getOrDefault(uuid, 0L);
            int remaining = playerData.getRemainingClaimBlocks();
            long total = used + remaining;

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            String name = offlinePlayer.getName();
            if (name == null) name = uuid.toString().substring(0, 8);

            entries.add(new ClaimTopEntry(name, used, remaining, total));
        }

        // Sort by total descending
        entries.sort((a, b) -> Long.compare(b.total, a.total));

        int totalPages = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, entries.size());

        // Send header
        sender.sendMessage(plugin.getConfigManager().getMessage("naming.claimtop-header")
                .replace("%page%", String.valueOf(page))
                .replace("%total%", String.valueOf(totalPages)));

        if (entries.isEmpty()) {
            sender.sendMessage(plugin.getConfigManager().getMessage("naming.claimtop-empty"));
            return true;
        }

        // Send entries
        String entryFormat = plugin.getConfigManager().getMessage("naming.claimtop-entry");
        for (int i = startIndex; i < endIndex; i++) {
            ClaimTopEntry entry = entries.get(i);
            sender.sendMessage(entryFormat
                    .replace("%rank%", String.valueOf(i + 1))
                    .replace("%player%", entry.name)
                    .replace("%total%", formatNumber(entry.total))
                    .replace("%used%", formatNumber(entry.used))
                    .replace("%unused%", formatNumber(entry.remaining)));
        }

        // Send footer if there are more pages
        if (totalPages > 1) {
            sender.sendMessage(plugin.getConfigManager().getMessage("naming.claimtop-footer")
                    .replace("%page%", String.valueOf(page))
                    .replace("%total%", String.valueOf(totalPages)));
        }

        return true;
    }

    private String formatNumber(long num) {
        return String.format("%,d", num);
    }

    private record ClaimTopEntry(String name, long used, long remaining, long total) {}
}
