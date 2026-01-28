package com.haksnbot.ffclaims;

import com.haksnbot.ffclaims.config.ConfigManager;
import com.haksnbot.ffclaims.hooks.GriefPreventionHook;
import com.haksnbot.ffclaims.hooks.VaultHook;
import com.haksnbot.ffclaims.market.data.MarketDataManager;
import com.haksnbot.ffclaims.market.gui.MenuManager;
import com.haksnbot.ffclaims.market.listeners.InventoryClickListener;
import com.haksnbot.ffclaims.market.listeners.MarketSignInteractListener;
import com.haksnbot.ffclaims.market.listeners.MarketSignListener;
import com.haksnbot.ffclaims.market.managers.AuctionManager;
import com.haksnbot.ffclaims.market.managers.ListingManager;
import com.haksnbot.ffclaims.market.managers.SaleManager;
import com.haksnbot.ffclaims.market.signs.SignManager;
import com.haksnbot.ffclaims.market.tasks.AuctionExpirationTask;
import com.haksnbot.ffclaims.naming.ClaimEntryListener;
import com.haksnbot.ffclaims.naming.ClaimTopCommand;
import com.haksnbot.ffclaims.naming.ClaimsListListener;
import com.haksnbot.ffclaims.naming.DynmapIntegration;
import com.haksnbot.ffclaims.naming.NameClaimCommand;
import com.haksnbot.ffclaims.naming.NamingDataManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * FF-Claims: Unified Finite Frontier claims plugin.
 * Combines claim naming and claim marketplace features.
 */
public class FFClaimsPlugin extends JavaPlugin {

    private static FFClaimsPlugin instance;

    // Core components
    private ConfigManager configManager;
    private GriefPreventionHook griefPreventionHook;

    // Naming module components
    private NamingDataManager namingDataManager;
    private DynmapIntegration dynmapIntegration;
    private boolean namingEnabled = true;

    // Market module components (optional - requires Vault)
    private VaultHook vaultHook;
    private MarketDataManager marketDataManager;
    private SignManager signManager;
    private ListingManager listingManager;
    private SaleManager saleManager;
    private AuctionManager auctionManager;
    private MenuManager menuManager;
    private AuctionExpirationTask expirationTask;
    private boolean marketEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize config
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Initialize GriefPrevention hook (required)
        griefPreventionHook = new GriefPreventionHook();
        if (!griefPreventionHook.isAvailable()) {
            getLogger().severe("GriefPrevention not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize naming module
        initializeNamingModule();

        // Initialize market module (optional - requires Vault)
        initializeMarketModule();

        // Register main command
        FFClaimsCommand mainCommand = new FFClaimsCommand(this);
        getCommand("ffclaims").setExecutor(mainCommand);
        getCommand("ffclaims").setTabCompleter(mainCommand);

        getLogger().info("FF-Claims enabled! Naming: " + (namingEnabled ? "ON" : "OFF") +
                ", Market: " + (marketEnabled ? "ON" : "OFF"));
    }

    private void initializeNamingModule() {
        namingEnabled = configManager.isNamingEnabled();
        if (!namingEnabled) {
            getLogger().info("Naming module disabled in config.");
            return;
        }

        // Initialize naming data
        namingDataManager = new NamingDataManager(this);
        namingDataManager.load();

        // Register naming commands
        getCommand("nameclaim").setExecutor(new NameClaimCommand(this));
        getCommand("claimtop").setExecutor(new ClaimTopCommand(this));

        // Register naming listeners
        getServer().getPluginManager().registerEvents(new ClaimEntryListener(this), this);
        getServer().getPluginManager().registerEvents(new ClaimsListListener(this), this);

        // Dynmap integration (optional)
        if (getServer().getPluginManager().isPluginEnabled("dynmap")) {
            dynmapIntegration = new DynmapIntegration(this);
            if (dynmapIntegration.init()) {
                getLogger().info("Dynmap integration enabled for claim names.");
            } else {
                dynmapIntegration = null;
                getLogger().warning("Dynmap found but marker API unavailable.");
            }
        }

        getLogger().info("Naming module loaded with " + namingDataManager.getClaimNames().size() + " named claims.");
    }

    private void initializeMarketModule() {
        marketEnabled = configManager.isMarketEnabled();
        if (!marketEnabled) {
            getLogger().info("Market module disabled in config.");
            return;
        }

        // Check for Vault
        vaultHook = new VaultHook(this);
        if (!vaultHook.isAvailable()) {
            getLogger().warning("Vault economy not found! Market module disabled.");
            marketEnabled = false;
            return;
        }

        // Initialize market data
        marketDataManager = new MarketDataManager(this);
        marketDataManager.load();

        // Initialize managers
        signManager = new SignManager(this);
        listingManager = new ListingManager(this);
        saleManager = new SaleManager(this);
        auctionManager = new AuctionManager(this);
        menuManager = new MenuManager(this);

        // Register market listeners
        getServer().getPluginManager().registerEvents(new MarketSignListener(this), this);
        getServer().getPluginManager().registerEvents(new MarketSignInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);

        // Start expiration task
        int interval = configManager.getAuctionExpirationCheckInterval();
        expirationTask = new AuctionExpirationTask(this);
        expirationTask.runTaskTimer(this, 20L * interval, 20L * interval);

        getLogger().info("Market module loaded. GriefPrevention and Vault hooks active.");
    }

    @Override
    public void onDisable() {
        // Save naming data
        if (namingDataManager != null) {
            namingDataManager.save();
        }

        // Save market data
        if (marketDataManager != null) {
            marketDataManager.save();
        }

        // Cancel tasks
        if (expirationTask != null) {
            expirationTask.cancel();
        }

        getLogger().info("FF-Claims disabled.");
    }

    public void reload() {
        configManager.loadConfig();

        if (namingDataManager != null) {
            namingDataManager.load();
        }

        if (marketDataManager != null) {
            marketDataManager.load();
        }

        getLogger().info("FF-Claims configuration reloaded.");
    }

    // ==================== GETTERS ====================

    public static FFClaimsPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GriefPreventionHook getGriefPreventionHook() {
        return griefPreventionHook;
    }

    // Naming module getters
    public boolean isNamingEnabled() {
        return namingEnabled;
    }

    public NamingDataManager getNamingDataManager() {
        return namingDataManager;
    }

    public DynmapIntegration getDynmapIntegration() {
        return dynmapIntegration;
    }

    // Market module getters
    public boolean isMarketEnabled() {
        return marketEnabled;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public MarketDataManager getMarketDataManager() {
        return marketDataManager;
    }

    public SignManager getSignManager() {
        return signManager;
    }

    public ListingManager getListingManager() {
        return listingManager;
    }

    public SaleManager getSaleManager() {
        return saleManager;
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }
}
