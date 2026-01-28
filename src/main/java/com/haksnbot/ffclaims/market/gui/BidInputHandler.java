package com.haksnbot.ffclaims.market.gui;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import com.haksnbot.ffclaims.market.data.AuctionData;
import com.haksnbot.ffclaims.market.managers.AuctionManager;
import com.haksnbot.ffclaims.market.signs.SignFormatter;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import de.rapha149.signgui.exception.SignGUIVersionException;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;

import java.util.List;

public class BidInputHandler {

    private final FFClaimsPlugin plugin;

    public BidInputHandler(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    public void openBidInput(Player player, AuctionData auction) {
        String minBidStr = SignFormatter.formatPriceFull(auction.getMinimumBid());

        try {
            SignGUI signGUI = SignGUI.builder()
                    .setLines(
                            "Enter your bid:",
                            "",
                            "^^^^^^^^^^^",
                            "Min: " + minBidStr
                    )
                    .setColor(DyeColor.BLACK)
                    .setHandler((p, result) -> {
                        String bidInput = result.getLine(1).trim();

                        if (bidInput.isEmpty()) {
                            return List.of(SignGUIAction.run(() ->
                                    player.sendMessage(plugin.getConfigManager().getPrefix() +
                                            "Bid cancelled - no amount entered.")));
                        }

                        return List.of(SignGUIAction.run(() -> processBid(player, auction, bidInput)));
                    })
                    .build();

            signGUI.open(player);
        } catch (SignGUIVersionException e) {
            plugin.getLogger().severe("SignGUI version incompatibility: " + e.getMessage());
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    "Bid input is not available. Please contact an admin.");
        }
    }

    private void processBid(Player player, AuctionData auction, String bidInput) {
        // Re-fetch the auction in case it changed
        AuctionData currentAuction = plugin.getMarketDataManager().getAuction(auction.getId());
        if (currentAuction == null || currentAuction.isEnded() || currentAuction.isExpired()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    "This auction is no longer available.");
            return;
        }

        // Parse the bid amount
        double bidAmount;
        try {
            bidAmount = SignFormatter.parsePrice(bidInput);
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("market.invalid-price"));
            return;
        }

        // Place the bid
        AuctionManager.BidResult result = plugin.getAuctionManager().placeBid(player, currentAuction, bidAmount);
        player.sendMessage(plugin.getConfigManager().getPrefix() + result.getMessage());

        if (result.isInstantWin()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    "Congratulations! You instantly won the auction with Buy Now!");
        }
    }
}
