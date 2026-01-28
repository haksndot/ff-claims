package com.haksnbot.ffclaims.naming;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import me.ryanhamshire.GriefPrevention.Claim;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimEntryListener implements Listener {

    private final FFClaimsPlugin plugin;
    // Tracks the last claim ID each player was in (0 = wilderness)
    private final Map<UUID, Long> lastClaimId = new HashMap<>();

    public ClaimEntryListener(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check on block changes
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        Claim claim = plugin.getGriefPreventionHook().getClaimAt(event.getTo(), false);
        long currentClaimId = claim != null ? claim.getID() : 0L;

        Long previous = lastClaimId.put(player.getUniqueId(), currentClaimId);
        if (previous == null) previous = 0L;

        // Only fire on claim change
        if (currentClaimId == previous) return;

        // If entering a named claim, show title
        if (currentClaimId != 0L) {
            String name = plugin.getNamingDataManager().getClaimName(currentClaimId);
            if (name != null) {
                Component subtitle = LegacyComponentSerializer.legacyAmpersand().deserialize(name);

                int fadeIn = plugin.getConfigManager().getDisplayFadeIn();
                int stay = plugin.getConfigManager().getDisplayStay();
                int fadeOut = plugin.getConfigManager().getDisplayFadeOut();

                Title title = Title.title(
                        Component.empty(),
                        subtitle,
                        Title.Times.times(
                                Duration.ofMillis(fadeIn * 50L),
                                Duration.ofMillis(stay * 50L),
                                Duration.ofMillis(fadeOut * 50L)
                        )
                );
                player.showTitle(title);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastClaimId.remove(event.getPlayer().getUniqueId());
    }
}
