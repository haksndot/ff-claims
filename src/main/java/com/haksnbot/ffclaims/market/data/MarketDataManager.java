package com.haksnbot.ffclaims.market.data;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MarketDataManager {

    private final JavaPlugin plugin;
    private final File salesFile;
    private final File auctionsFile;

    private final Map<String, SaleData> sales = new ConcurrentHashMap<>();
    private final Map<String, AuctionData> auctions = new ConcurrentHashMap<>();

    // Index by sign location for quick lookup
    private final Map<String, String> signToSaleId = new ConcurrentHashMap<>();
    private final Map<String, String> signToAuctionId = new ConcurrentHashMap<>();

    public MarketDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.salesFile = new File(plugin.getDataFolder(), "market-sales.yml");
        this.auctionsFile = new File(plugin.getDataFolder(), "market-auctions.yml");
    }

    public void load() {
        loadSales();
        loadAuctions();
    }

    public void save() {
        saveSales();
        saveAuctions();
    }

    // ==================== SALES ====================

    private void loadSales() {
        sales.clear();
        signToSaleId.clear();

        if (!salesFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(salesFile);
        ConfigurationSection listingsSection = config.getConfigurationSection("listings");

        if (listingsSection == null) {
            return;
        }

        for (String id : listingsSection.getKeys(false)) {
            SaleData sale = SaleData.load(id, listingsSection.getConfigurationSection(id));
            if (sale != null) {
                sales.put(id, sale);
                signToSaleId.put(locationKey(sale.getSignLocation()), id);
            }
        }

        plugin.getLogger().info("Loaded " + sales.size() + " sale listings.");
    }

    private void saveSales() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection listingsSection = config.createSection("listings");

        for (SaleData sale : sales.values()) {
            ConfigurationSection saleSection = listingsSection.createSection(sale.getId());
            sale.save(saleSection);
        }

        try {
            config.save(salesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save sales data: " + e.getMessage());
        }
    }

    public void addSale(SaleData sale) {
        sales.put(sale.getId(), sale);
        signToSaleId.put(locationKey(sale.getSignLocation()), sale.getId());
        saveSales();
    }

    public void removeSale(String id) {
        SaleData sale = sales.remove(id);
        if (sale != null) {
            signToSaleId.remove(locationKey(sale.getSignLocation()));
            saveSales();
        }
    }

    public SaleData getSale(String id) {
        return sales.get(id);
    }

    public SaleData getSaleBySign(Location signLocation) {
        String id = signToSaleId.get(locationKey(signLocation));
        return id != null ? sales.get(id) : null;
    }

    public Collection<SaleData> getAllSales() {
        return Collections.unmodifiableCollection(sales.values());
    }

    public List<SaleData> getSalesBySeller(UUID sellerUUID) {
        List<SaleData> result = new ArrayList<>();
        for (SaleData sale : sales.values()) {
            if (sale.getSellerUUID().equals(sellerUUID)) {
                result.add(sale);
            }
        }
        return result;
    }

    // ==================== AUCTIONS ====================

    private void loadAuctions() {
        auctions.clear();
        signToAuctionId.clear();

        if (!auctionsFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(auctionsFile);
        ConfigurationSection listingsSection = config.getConfigurationSection("listings");

        if (listingsSection == null) {
            return;
        }

        for (String id : listingsSection.getKeys(false)) {
            AuctionData auction = AuctionData.load(id, listingsSection.getConfigurationSection(id));
            if (auction != null) {
                auctions.put(id, auction);
                signToAuctionId.put(locationKey(auction.getSignLocation()), id);
            }
        }

        plugin.getLogger().info("Loaded " + auctions.size() + " auction listings.");
    }

    private void saveAuctions() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection listingsSection = config.createSection("listings");

        for (AuctionData auction : auctions.values()) {
            ConfigurationSection auctionSection = listingsSection.createSection(auction.getId());
            auction.save(auctionSection);
        }

        try {
            config.save(auctionsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save auctions data: " + e.getMessage());
        }
    }

    public void addAuction(AuctionData auction) {
        auctions.put(auction.getId(), auction);
        signToAuctionId.put(locationKey(auction.getSignLocation()), auction.getId());
        saveAuctions();
    }

    public void removeAuction(String id) {
        AuctionData auction = auctions.remove(id);
        if (auction != null) {
            signToAuctionId.remove(locationKey(auction.getSignLocation()));
            saveAuctions();
        }
    }

    public void updateAuction(AuctionData auction) {
        auctions.put(auction.getId(), auction);
        saveAuctions();
    }

    public AuctionData getAuction(String id) {
        return auctions.get(id);
    }

    public AuctionData getAuctionBySign(Location signLocation) {
        String id = signToAuctionId.get(locationKey(signLocation));
        return id != null ? auctions.get(id) : null;
    }

    public Collection<AuctionData> getAllAuctions() {
        return Collections.unmodifiableCollection(auctions.values());
    }

    public List<AuctionData> getActiveAuctions() {
        List<AuctionData> result = new ArrayList<>();
        for (AuctionData auction : auctions.values()) {
            if (!auction.isEnded() && !auction.isExpired()) {
                result.add(auction);
            }
        }
        return result;
    }

    public List<AuctionData> getExpiredAuctions() {
        List<AuctionData> result = new ArrayList<>();
        for (AuctionData auction : auctions.values()) {
            if (!auction.isEnded() && auction.isExpired()) {
                result.add(auction);
            }
        }
        return result;
    }

    public List<AuctionData> getAuctionsBySeller(UUID sellerUUID) {
        List<AuctionData> result = new ArrayList<>();
        for (AuctionData auction : auctions.values()) {
            if (auction.getSellerUUID().equals(sellerUUID)) {
                result.add(auction);
            }
        }
        return result;
    }

    public List<AuctionData> getAuctionsByBidder(UUID bidderUUID) {
        List<AuctionData> result = new ArrayList<>();
        for (AuctionData auction : auctions.values()) {
            if (auction.getBidByPlayer(bidderUUID) != null) {
                result.add(auction);
            }
        }
        return result;
    }

    // ==================== UTILITY ====================

    public boolean hasListingAtClaim(Location claimLocation) {
        String key = locationKey(claimLocation);

        for (SaleData sale : sales.values()) {
            if (locationKey(sale.getClaimLocation()).equals(key)) {
                return true;
            }
        }

        for (AuctionData auction : auctions.values()) {
            if (!auction.isEnded() && locationKey(auction.getClaimLocation()).equals(key)) {
                return true;
            }
        }

        return false;
    }

    public boolean isListingSign(Location signLocation) {
        String key = locationKey(signLocation);
        return signToSaleId.containsKey(key) || signToAuctionId.containsKey(key);
    }

    private String locationKey(Location location) {
        return location.getWorld().getName() + ":" +
                location.getBlockX() + ":" +
                location.getBlockY() + ":" +
                location.getBlockZ();
    }
}
