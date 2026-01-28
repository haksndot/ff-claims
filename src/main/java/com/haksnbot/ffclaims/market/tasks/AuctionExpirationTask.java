package com.haksnbot.ffclaims.market.tasks;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import com.haksnbot.ffclaims.market.data.AuctionData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class AuctionExpirationTask extends BukkitRunnable {

    private final FFClaimsPlugin plugin;
    private int signUpdateCounter = 0;

    public AuctionExpirationTask(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Process expired auctions
        List<AuctionData> expiredAuctions = plugin.getMarketDataManager().getExpiredAuctions();

        for (AuctionData auction : expiredAuctions) {
            try {
                plugin.getAuctionManager().processExpiredAuction(auction);
            } catch (Exception e) {
                plugin.getLogger().severe("Error processing expired auction " + auction.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Update auction signs every 2 runs (~1 minute if interval is 30s)
        signUpdateCounter++;
        if (signUpdateCounter >= 2) {
            signUpdateCounter = 0;
            plugin.getAuctionManager().updateAuctionSigns();
        }
    }
}
