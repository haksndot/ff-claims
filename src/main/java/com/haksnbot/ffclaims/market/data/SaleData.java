package com.haksnbot.ffclaims.market.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public class SaleData {

    private final String id;
    private final UUID sellerUUID;
    private final String sellerName;
    private final double price;
    private final Location signLocation;
    private final Location claimLocation;
    private final long created;
    private final int area;
    private final String dimensions;

    public SaleData(String id, UUID sellerUUID, String sellerName, double price,
                    Location signLocation, Location claimLocation, long created,
                    int area, String dimensions) {
        this.id = id;
        this.sellerUUID = sellerUUID;
        this.sellerName = sellerName;
        this.price = price;
        this.signLocation = signLocation;
        this.claimLocation = claimLocation;
        this.created = created;
        this.area = area;
        this.dimensions = dimensions;
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

    public double getPrice() {
        return price;
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

    public int getArea() {
        return area;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void save(ConfigurationSection section) {
        section.set("seller", sellerUUID.toString());
        section.set("seller_name", sellerName);
        section.set("price", price);
        section.set("created", created);
        section.set("area", area);
        section.set("dimensions", dimensions);

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
    }

    public static SaleData load(String id, ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        try {
            UUID sellerUUID = UUID.fromString(section.getString("seller"));
            String sellerName = section.getString("seller_name", "Unknown");
            double price = section.getDouble("price");
            long created = section.getLong("created");
            int area = section.getInt("area", 0);
            String dimensions = section.getString("dimensions", "Unknown");

            // Sign location
            ConfigurationSection signSection = section.getConfigurationSection("sign");
            Location signLocation = loadLocation(signSection);

            // Claim location
            ConfigurationSection claimSection = section.getConfigurationSection("claim");
            Location claimLocation = loadLocation(claimSection);

            if (signLocation == null || claimLocation == null) {
                return null;
            }

            return new SaleData(id, sellerUUID, sellerName, price, signLocation,
                    claimLocation, created, area, dimensions);
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
        return "sale_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
