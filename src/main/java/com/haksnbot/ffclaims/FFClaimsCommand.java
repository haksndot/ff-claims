package com.haksnbot.ffclaims;

import com.haksnbot.ffclaims.market.data.AuctionData;
import com.haksnbot.ffclaims.market.data.BidData;
import com.haksnbot.ffclaims.market.data.SaleData;
import com.haksnbot.ffclaims.market.signs.SignFormatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Main command handler for FF-Claims plugin.
 * Provides unified access to naming and market features.
 */
public class FFClaimsCommand implements CommandExecutor, TabCompleter {

    private final FFClaimsPlugin plugin;

    public FFClaimsCommand(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> handleReload(sender);
            case "list" -> handleList(sender);
            case "mybids" -> handleMyBids(sender);
            case "mylistings" -> handleMyListings(sender);
            case "transactions", "tx" -> handleTransactions(sender, args);
            case "help" -> showHelp(sender);
            default -> sender.sendMessage(plugin.getConfigManager().getPrefix() +
                    "Unknown command. Use /ffc help for help.");
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("ffclaims.admin")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        plugin.reload();
        sender.sendMessage(plugin.getConfigManager().getPrefix() +
                plugin.getConfigManager().getMessage("config-reloaded"));
    }

    private void handleList(CommandSender sender) {
        if (!plugin.isMarketEnabled()) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() +
                    "Market module is not enabled.");
            return;
        }

        if (!sender.hasPermission("ffclaims.market.use")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        List<SaleData> sales = new ArrayList<>(plugin.getMarketDataManager().getAllSales());
        List<AuctionData> auctions = plugin.getMarketDataManager().getActiveAuctions();

        if (sales.isEmpty() && auctions.isEmpty()) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() +
                    "No active listings found.");
            return;
        }

        sender.sendMessage(plugin.getConfigManager().getPrefix() + "Active Listings:");

        // Show sales
        if (!sales.isEmpty()) {
            sender.sendMessage("\u00A76--- Sales ---");
            for (SaleData sale : sales) {
                String location = String.format("(%d, %d, %d)",
                        sale.getSignLocation().getBlockX(),
                        sale.getSignLocation().getBlockY(),
                        sale.getSignLocation().getBlockZ());
                sender.sendMessage(String.format("\u00A77- \u00A7f%s \u00A77| \u00A7e%s \u00A77| \u00A7a%s \u00A77| %s",
                        sale.getSellerName(),
                        SignFormatter.formatPriceFull(sale.getPrice()),
                        sale.getDimensions(),
                        location));
            }
        }

        // Show auctions
        if (!auctions.isEmpty()) {
            sender.sendMessage("\u00A76--- Auctions ---");
            for (AuctionData auction : auctions) {
                String location = String.format("(%d, %d, %d)",
                        auction.getSignLocation().getBlockX(),
                        auction.getSignLocation().getBlockY(),
                        auction.getSignLocation().getBlockZ());
                String buyNow = auction.hasBuyNow() ?
                        " | BuyNow: \u00A7a" + SignFormatter.formatPrice(auction.getBuyNowPrice()) : "";
                sender.sendMessage(String.format("\u00A77- \u00A7f%s \u00A77| Min: \u00A7e%s%s \u00A77| \u00A7a%s \u00A77| %s",
                        auction.getSellerName(),
                        SignFormatter.formatPrice(auction.getMinimumBid()),
                        buyNow,
                        auction.getTimeRemainingFormatted(),
                        location));
            }
        }
    }

    private void handleMyBids(CommandSender sender) {
        if (!plugin.isMarketEnabled()) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() +
                    "Market module is not enabled.");
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() +
                    "This command can only be used by players.");
            return;
        }

        if (!player.hasPermission("ffclaims.market.use")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        List<AuctionData> auctions = plugin.getMarketDataManager().getAuctionsByBidder(player.getUniqueId());

        if (auctions.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    "You haven't placed any bids.");
            return;
        }

        player.sendMessage(plugin.getConfigManager().getPrefix() + "Your Active Bids:");

        for (AuctionData auction : auctions) {
            if (auction.isEnded()) continue;

            BidData bid = auction.getBidByPlayer(player.getUniqueId());
            if (bid == null) continue;

            String status = auction.isExpired() ? "\u00A7c(Ended)" : "\u00A7a" + auction.getTimeRemainingFormatted();
            String location = String.format("(%d, %d, %d)",
                    auction.getSignLocation().getBlockX(),
                    auction.getSignLocation().getBlockY(),
                    auction.getSignLocation().getBlockZ());

            player.sendMessage(String.format("\u00A77- \u00A7f%s \u00A77| Your bid: \u00A7e%s \u00A77| %s \u00A77| %s",
                    auction.getSellerName(),
                    SignFormatter.formatPriceFull(bid.getAmount()),
                    status,
                    location));
        }
    }

    private void handleMyListings(CommandSender sender) {
        if (!plugin.isMarketEnabled()) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() +
                    "Market module is not enabled.");
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() +
                    "This command can only be used by players.");
            return;
        }

        if (!player.hasPermission("ffclaims.market.use")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        List<SaleData> sales = plugin.getMarketDataManager().getSalesBySeller(player.getUniqueId());
        List<AuctionData> auctions = plugin.getMarketDataManager().getAuctionsBySeller(player.getUniqueId());

        if (sales.isEmpty() && auctions.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    "You don't have any active listings.");
            return;
        }

        player.sendMessage(plugin.getConfigManager().getPrefix() + "Your Listings:");

        for (SaleData sale : sales) {
            String location = String.format("(%d, %d, %d)",
                    sale.getSignLocation().getBlockX(),
                    sale.getSignLocation().getBlockY(),
                    sale.getSignLocation().getBlockZ());
            player.sendMessage(String.format("\u00A77- \u00A76[Sale] \u00A7e%s \u00A77| \u00A7a%s \u00A77| %s",
                    SignFormatter.formatPriceFull(sale.getPrice()),
                    sale.getDimensions(),
                    location));
        }

        for (AuctionData auction : auctions) {
            if (auction.isEnded()) continue;

            String location = String.format("(%d, %d, %d)",
                    auction.getSignLocation().getBlockX(),
                    auction.getSignLocation().getBlockY(),
                    auction.getSignLocation().getBlockZ());
            player.sendMessage(String.format("\u00A77- \u00A76[Auction] \u00A7eBids: %d \u00A77| \u00A7a%s \u00A77| %s",
                    auction.getBids().size(),
                    auction.getTimeRemainingFormatted(),
                    location));
        }
    }

    private void handleTransactions(CommandSender sender, String[] args) {
        if (!plugin.isMarketEnabled()) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() +
                    "Market module is not enabled.");
            return;
        }

        if (!sender.hasPermission("ffclaims.market.use")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        // Check if looking up a specific transaction
        if (args.length > 1 && args[1].toUpperCase().startsWith("TX")) {
            String txId = args[1].toUpperCase();
            String detail = plugin.getTransactionLogger().getTransaction(txId);
            if (detail == null) {
                sender.sendMessage(plugin.getConfigManager().getPrefix() +
                        "Transaction " + txId + " not found.");
                return;
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', detail));
            return;
        }

        // Determine count and optional player filter
        int count = 10;
        UUID playerFilter = null;

        if (args.length > 1) {
            // Try to parse as number first
            try {
                count = Integer.parseInt(args[1]);
                count = Math.min(count, 50); // Cap at 50
            } catch (NumberFormatException e) {
                // Try to parse as player name
                if (sender.hasPermission("ffclaims.admin")) {
                    @SuppressWarnings("deprecation")
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    if (target.hasPlayedBefore() || target.isOnline()) {
                        playerFilter = target.getUniqueId();
                    } else {
                        sender.sendMessage(plugin.getConfigManager().getPrefix() +
                                "Player '" + args[1] + "' not found.");
                        return;
                    }
                } else {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() +
                            "You don't have permission to view other players' transactions.");
                    return;
                }
            }
        }

        // If not admin and looking at general history, could show only their own
        // For now, allow all players to see recent transactions (it's public info)

        List<String> transactions = plugin.getTransactionLogger().getRecentTransactions(count, playerFilter);

        if (transactions.isEmpty()) {
            String msg = playerFilter != null ?
                    "No transactions found for that player." :
                    "No transactions recorded yet.";
            sender.sendMessage(plugin.getConfigManager().getPrefix() + msg);
            return;
        }

        int totalCount = plugin.getTransactionLogger().getTransactionCount();
        sender.sendMessage("\u00A76--- Recent Transactions (" + transactions.size() + "/" + totalCount + " total) ---");

        for (String tx : transactions) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', tx));
        }

        sender.sendMessage("\u00A77Use \u00A7e/ffc tx <TX#>\u00A77 for details.");
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("\u00A76=== FF-Claims Help ===");
        sender.sendMessage("\u00A7eFF-Claims \u00A77- Finite Frontier claims enhancements");
        sender.sendMessage("");

        if (plugin.isNamingEnabled()) {
            sender.sendMessage("\u00A76--- Naming ---");
            sender.sendMessage("\u00A7e/nameclaim [name|clear] \u00A77- Name your claim");
            sender.sendMessage("\u00A7e/claimtop [page] \u00A77- Claim block rankings");
        }

        if (plugin.isMarketEnabled()) {
            sender.sendMessage("\u00A76--- Market ---");
            sender.sendMessage("\u00A7e/ffc list \u00A77- Show all active listings");
            sender.sendMessage("\u00A7e/ffc mybids \u00A77- Show your active bids");
            sender.sendMessage("\u00A7e/ffc mylistings \u00A77- Show your active listings");
            sender.sendMessage("\u00A7e/ffc transactions [count] \u00A77- View recent transactions");
            sender.sendMessage("\u00A7e/ffc tx <TX#> \u00A77- View transaction details");
            sender.sendMessage("");
            sender.sendMessage("\u00A77To sell a claim, place a sign with:");
            sender.sendMessage("\u00A7f  [For Sale]");
            sender.sendMessage("\u00A7f  <price>");
            sender.sendMessage("");
            sender.sendMessage("\u00A77To auction a claim, place a sign with:");
            sender.sendMessage("\u00A7f  [Auction]");
            sender.sendMessage("\u00A7f  min:<price>");
            sender.sendMessage("\u00A7f  now:<buyNowPrice> \u00A77(optional)");
            sender.sendMessage("\u00A7f  <duration> \u00A77(e.g. 2d, 48h)");
        }

        if (sender.hasPermission("ffclaims.admin")) {
            sender.sendMessage("");
            sender.sendMessage("\u00A76--- Admin ---");
            sender.sendMessage("\u00A7e/ffc reload \u00A77- Reload configuration");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();

            if (plugin.isMarketEnabled()) {
                if ("list".startsWith(partial)) completions.add("list");
                if ("mybids".startsWith(partial)) completions.add("mybids");
                if ("mylistings".startsWith(partial)) completions.add("mylistings");
                if ("transactions".startsWith(partial)) completions.add("transactions");
                if ("tx".startsWith(partial)) completions.add("tx");
            }
            if ("help".startsWith(partial)) completions.add("help");

            if (sender.hasPermission("ffclaims.admin")) {
                if ("reload".startsWith(partial)) completions.add("reload");
            }
        }

        return completions;
    }
}
