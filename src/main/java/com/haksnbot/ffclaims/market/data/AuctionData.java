package com.haksnbot.ffclaims.market.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AuctionData {

    private final String id;
    private final UUID sellerUUID;
    private final String sellerName;
    private final double minimumBid;
    private final double buyNowPrice;
    private final Location signLocation;
    private final Location claimLocation;
    private final long created;
    private final long expires;
    private final int area;
    private final String dimensions;
    private final List<BidData> bids;
    private boolean ended;

    public AuctionData(String id, UUID sellerUUID, String sellerName, double minimumBid,
                       double buyNowPrice, Location signLocation, Location claimLocation,
                       long created, long expires, int area, String dimensions) {
        this.id = id;
        this.sellerUUID = sellerUUID;
        this.sellerName = sellerName;
        this.minimumBid = minimumBid;
        this.buyNowPrice = buyNowPrice;
        this.signLocation = signLocation;
        this.claimLocation = claimLocation;
        this.created = created;
        this.expires = expires;
        this.area = area;
        this.dimensions = dimensions;
        this.bids = new ArrayList<>();
        this.ended = false;
    }

    public String getId() {
        return id;
    }

    public UUID getSellerUUID() {
        return sellerUUID;
    }

    public String getSellerName() {
        return sellerName;
    }

    public double getMinimumBid() {
        return minimumBid;
    }

    public double getBuyNowPrice() {
        return buyNowPrice;
    }

    public boolean hasBuyNow() {
        return buyNowPrice > 0;
    }

    public Location getSignLocation() {
        return signLocation;
    }

    public Location getClaimLocation() {
        return claimLocation;
    }

    public long getCreated() {
        return created;
    }

    public long getExpires() {
        return expires;
    }

    public int getArea() {
        return area;
    }

    public String getDimensions() {
        return dimensions;
    }

    public List<BidData> getBids() {
        return new ArrayList<>(bids);
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expires;
    }

    public void addBid(BidData bid) {
        bids.add(bid);
    }

    public BidData getHighestBid() {
        return bids.stream()
                .max(Comparator.comparingDouble(BidData::getAmount))
                .orElse(null);
    }

    public BidData getSecondHighestBid() {
        if (bids.size() < 2) {
            return null;
        }
        return bids.stream()
                .sorted(Comparator.comparingDouble(BidData::getAmount).reversed())
                .skip(1)
                .findFirst()
                .orElse(null);
    }

    public double getVickreyPrice() {
        BidData secondHighest = getSecondHighestBid();
        if (secondHighest != null) {
            return secondHighest.getAmount();
        }
        return minimumBid;
    }

    public long getTimeRemaining() {
        return Math.max(0, expires - System.currentTimeMillis());
    }

    public String getTimeRemainingFormatted() {
        long remaining = getTimeRemaining();
        if (remaining <= 0) {
            return "Ended";
        }

        long seconds = remaining / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m";
        } else {
            return seconds + "s";
        }
    }

    public BidData getBidByPlayer(UUID playerUUID) {
        return bids.stream()
                .filter(bid -> bid.getBidderUUID().equals(playerUUID))
                .findFirst()
                .orElse(null);
    }

    public void save(ConfigurationSection section) {
        section.set("seller", sellerUUID.toString());
        section.set("seller_name", sellerName);
        section.set("minimum_bid", minimumBid);
        section.set("buy_now", buyNowPrice);
        section.set("created", created);
        section.set("expires", expires);
        section.set("area", area);
        section.set("dimensions", dimensions);
        section.set("ended", ended);

        // Sign location
        ConfigurationSection signSection = section.createSection("sign");
        signSection.set("world", signLocation.getWorld().getName());
        signSection.set("x", signLocation.getBlockX());
        signSection.set("y", signLocation.getBlockY());
        signSection.set("z", signLocation.getBlockZ());

        // Claim location
        ConfigurationSection claimSection = section.createSection("claim");
        claimSection.set("world", claimLocation.getWorld().getName());
        claimSection.set("x", claimLocation.getBlockX());
        claimSection.set("y", claimLocation.getBlockY());
        claimSection.set("z", claimLocation.getBlockZ());

        // Bids
        for (int i = 0; i < bids.size(); i++) {
            ConfigurationSection bidSection = section.createSection("bids." + i);
            bids.get(i).save(bidSection);
        }
    }

    public static AuctionData load(String id, ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        try {
            UUID sellerUUID = UUID.fromString(section.getString("seller"));
            String sellerName = section.getString("seller_name", "Unknown");
            double minimumBid = section.getDouble("minimum_bid");
            double buyNowPrice = section.getDouble("buy_now", 0.0);
            long created = section.getLong("created");
            long expires = section.getLong("expires");
            int area = section.getInt("area", 0);
            String dimensions = section.getString("dimensions", "Unknown");
            boolean ended = section.getBoolean("ended", false);

            // Sign location
            ConfigurationSection signSection = section.getConfigurationSection("sign");
            Location signLocation = loadLocation(signSection);

            // Claim location
            ConfigurationSection claimSection = section.getConfigurationSection("claim");
            Location claimLocation = loadLocation(claimSection);

            if (signLocation == null || claimLocation == null) {
                return null;
            }

            AuctionData auction = new AuctionData(id, sellerUUID, sellerName, minimumBid,
                    buyNowPrice, signLocation, claimLocation, created, expires, area, dimensions);
            auction.setEnded(ended);

            // Load bids
            ConfigurationSection bidsSection = section.getConfigurationSection("bids");
            if (bidsSection != null) {
                for (String key : bidsSection.getKeys(false)) {
                    BidData bid = BidData.load(bidsSection.getConfigurationSection(key));
                    if (bid != null) {
                        auction.addBid(bid);
                    }
                }
            }

            return auction;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Location loadLocation(ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        String worldName = section.getString("world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }

        int x = section.getInt("x");
        int y = section.getInt("y");
        int z = section.getInt("z");

        return new Location(world, x, y, z);
    }

    public static String generateId() {
        return "auction_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
