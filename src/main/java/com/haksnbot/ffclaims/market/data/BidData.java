package com.haksnbot.ffclaims.market.data;

import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public class BidData {

    private final UUID bidderUUID;
    private final String bidderName;
    private final double amount;
    private final long timestamp;

    public BidData(UUID bidderUUID, String bidderName, double amount, long timestamp) {
        this.bidderUUID = bidderUUID;
        this.bidderName = bidderName;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public UUID getBidderUUID() {
        return bidderUUID;
    }

    public String getBidderName() {
        return bidderName;
    }

    public double getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void save(ConfigurationSection section) {
        section.set("bidder", bidderUUID.toString());
        section.set("bidder_name", bidderName);
        section.set("amount", amount);
        section.set("timestamp", timestamp);
    }

    public static BidData load(ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        try {
            UUID bidderUUID = UUID.fromString(section.getString("bidder"));
            String bidderName = section.getString("bidder_name", "Unknown");
            double amount = section.getDouble("amount");
            long timestamp = section.getLong("timestamp");

            return new BidData(bidderUUID, bidderName, amount, timestamp);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
