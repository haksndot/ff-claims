package com.haksnbot.ffclaims.market.data;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Logs all completed real estate transactions for record-keeping.
 */
public class TransactionLogger {

    private final FFClaimsPlugin plugin;
    private final File logFile;
    private YamlConfiguration logConfig;
    private int transactionCounter;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public TransactionLogger(FFClaimsPlugin plugin) {
        this.plugin = plugin;
        this.logFile = new File(plugin.getDataFolder(), "transactions.yml");
        load();
    }

    private void load() {
        if (!logFile.exists()) {
            try {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create transactions.yml: " + e.getMessage());
            }
        }
        logConfig = YamlConfiguration.loadConfiguration(logFile);
        transactionCounter = logConfig.getInt("_counter", 0);
    }

    private void save() {
        try {
            logConfig.set("_counter", transactionCounter);
            logConfig.save(logFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save transactions.yml: " + e.getMessage());
        }
    }

    /**
     * Log a completed sale transaction.
     */
    public void logSale(
            UUID sellerUUID, String sellerName,
            UUID buyerUUID, String buyerName,
            double price, int claimArea, String claimDimensions,
            String claimLocation, String claimName
    ) {
        transactionCounter++;
        String txId = "TX" + String.format("%06d", transactionCounter);

        ConfigurationSection tx = logConfig.createSection("transactions." + txId);
        tx.set("type", "SALE");
        tx.set("timestamp", DATE_FORMAT.format(new Date()));
        tx.set("seller.uuid", sellerUUID.toString());
        tx.set("seller.name", sellerName);
        tx.set("buyer.uuid", buyerUUID.toString());
        tx.set("buyer.name", buyerName);
        tx.set("price", price);
        tx.set("claim.area", claimArea);
        tx.set("claim.dimensions", claimDimensions);
        tx.set("claim.location", claimLocation);
        if (claimName != null && !claimName.isEmpty()) {
            tx.set("claim.name", claimName);
        }

        save();

        plugin.getLogger().info(String.format("[%s] SALE: %s sold to %s for $%.2f (%d blocks)",
                txId, sellerName, buyerName, price, claimArea));
    }

    /**
     * Log a completed auction transaction (including Vickrey pricing info).
     */
    public void logAuction(
            UUID sellerUUID, String sellerName,
            UUID winnerUUID, String winnerName,
            double winningBid, double paidPrice, int bidCount,
            int claimArea, String claimDimensions,
            String claimLocation, String claimName
    ) {
        transactionCounter++;
        String txId = "TX" + String.format("%06d", transactionCounter);

        ConfigurationSection tx = logConfig.createSection("transactions." + txId);
        tx.set("type", "AUCTION");
        tx.set("timestamp", DATE_FORMAT.format(new Date()));
        tx.set("seller.uuid", sellerUUID.toString());
        tx.set("seller.name", sellerName);
        tx.set("winner.uuid", winnerUUID.toString());
        tx.set("winner.name", winnerName);
        tx.set("winning-bid", winningBid);
        tx.set("paid-price", paidPrice);  // Vickrey: second-highest bid
        tx.set("bid-count", bidCount);
        tx.set("claim.area", claimArea);
        tx.set("claim.dimensions", claimDimensions);
        tx.set("claim.location", claimLocation);
        if (claimName != null && !claimName.isEmpty()) {
            tx.set("claim.name", claimName);
        }

        save();

        plugin.getLogger().info(String.format("[%s] AUCTION: %s won from %s for $%.2f (bid: $%.2f, %d blocks)",
                txId, winnerName, sellerName, paidPrice, winningBid, claimArea));
    }

    /**
     * Log a buy-now transaction (instant auction purchase).
     */
    public void logBuyNow(
            UUID sellerUUID, String sellerName,
            UUID buyerUUID, String buyerName,
            double price, int claimArea, String claimDimensions,
            String claimLocation, String claimName
    ) {
        transactionCounter++;
        String txId = "TX" + String.format("%06d", transactionCounter);

        ConfigurationSection tx = logConfig.createSection("transactions." + txId);
        tx.set("type", "BUY_NOW");
        tx.set("timestamp", DATE_FORMAT.format(new Date()));
        tx.set("seller.uuid", sellerUUID.toString());
        tx.set("seller.name", sellerName);
        tx.set("buyer.uuid", buyerUUID.toString());
        tx.set("buyer.name", buyerName);
        tx.set("price", price);
        tx.set("claim.area", claimArea);
        tx.set("claim.dimensions", claimDimensions);
        tx.set("claim.location", claimLocation);
        if (claimName != null && !claimName.isEmpty()) {
            tx.set("claim.name", claimName);
        }

        save();

        plugin.getLogger().info(String.format("[%s] BUY_NOW: %s bought from %s for $%.2f (%d blocks)",
                txId, buyerName, sellerName, price, claimArea));
    }

    /**
     * Get the total number of recorded transactions.
     */
    public int getTransactionCount() {
        return transactionCounter;
    }

    /**
     * Get recent transactions formatted for display.
     * @param count Number of transactions to retrieve
     * @param playerFilter Optional UUID to filter by (as buyer or seller). Null for all.
     * @return List of formatted transaction strings
     */
    public List<String> getRecentTransactions(int count, UUID playerFilter) {
        List<String> results = new ArrayList<>();
        ConfigurationSection transactions = logConfig.getConfigurationSection("transactions");
        if (transactions == null) {
            return results;
        }

        // Get all transaction IDs and sort in reverse order (newest first)
        List<String> txIds = new ArrayList<>(transactions.getKeys(false));
        Collections.sort(txIds, Collections.reverseOrder());

        for (String txId : txIds) {
            if (results.size() >= count) {
                break;
            }

            ConfigurationSection tx = transactions.getConfigurationSection(txId);
            if (tx == null) {
                continue;
            }

            // Apply player filter if provided
            if (playerFilter != null) {
                String sellerUUID = tx.getString("seller.uuid", "");
                String buyerUUID = tx.getString("buyer.uuid", tx.getString("winner.uuid", ""));
                if (!sellerUUID.equals(playerFilter.toString()) &&
                        !buyerUUID.equals(playerFilter.toString())) {
                    continue;
                }
            }

            results.add(formatTransaction(txId, tx));
        }

        return results;
    }

    /**
     * Get a specific transaction by ID.
     */
    public String getTransaction(String txId) {
        ConfigurationSection tx = logConfig.getConfigurationSection("transactions." + txId);
        if (tx == null) {
            return null;
        }
        return formatTransactionDetail(txId, tx);
    }

    private String formatTransaction(String txId, ConfigurationSection tx) {
        String type = tx.getString("type", "UNKNOWN");
        String timestamp = tx.getString("timestamp", "?");
        String seller = tx.getString("seller.name", "?");
        String buyer = tx.getString("buyer.name", tx.getString("winner.name", "?"));
        double price = tx.getDouble("price", tx.getDouble("paid-price", 0));
        int area = tx.getInt("claim.area", 0);
        String claimName = tx.getString("claim.name");

        String claimDesc = claimName != null ? claimName : (area + " blocks");

        return String.format("&7[%s] &e%s &7%s → %s &afor &e$%,.0f &7(%s)",
                txId, type, seller, buyer, price, claimDesc);
    }

    private String formatTransactionDetail(String txId, ConfigurationSection tx) {
        StringBuilder sb = new StringBuilder();
        String type = tx.getString("type", "UNKNOWN");
        String timestamp = tx.getString("timestamp", "?");
        String seller = tx.getString("seller.name", "?");
        String buyer = tx.getString("buyer.name", tx.getString("winner.name", "?"));
        double price = tx.getDouble("price", tx.getDouble("paid-price", 0));
        int area = tx.getInt("claim.area", 0);
        String dimensions = tx.getString("claim.dimensions", "?");
        String location = tx.getString("claim.location", "?");
        String claimName = tx.getString("claim.name");

        sb.append("&6--- Transaction ").append(txId).append(" ---\n");
        sb.append("&7Type: &f").append(type).append("\n");
        sb.append("&7Date: &f").append(timestamp).append("\n");
        sb.append("&7Seller: &f").append(seller).append("\n");
        sb.append("&7Buyer: &f").append(buyer).append("\n");
        sb.append("&7Price: &a$").append(String.format("%,.2f", price)).append("\n");
        if (claimName != null) {
            sb.append("&7Claim: &f").append(claimName).append("\n");
        }
        sb.append("&7Size: &f").append(dimensions).append(" (").append(area).append(" blocks)\n");
        sb.append("&7Location: &f").append(location);

        if (type.equals("AUCTION")) {
            double winningBid = tx.getDouble("winning-bid", 0);
            int bidCount = tx.getInt("bid-count", 0);
            sb.append("\n&7Winning Bid: &e$").append(String.format("%,.2f", winningBid));
            sb.append("\n&7Total Bids: &f").append(bidCount);
        }

        return sb.toString();
    }
}
