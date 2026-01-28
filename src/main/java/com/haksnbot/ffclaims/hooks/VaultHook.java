package com.haksnbot.ffclaims.hooks;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Vault economy hook for the market module.
 */
public class VaultHook {

    private final JavaPlugin plugin;
    private Economy economy;

    public VaultHook(JavaPlugin plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }

        economy = rsp.getProvider();
    }

    public boolean isAvailable() {
        return economy != null;
    }

    /**
     * Check if a player has at least the specified balance.
     */
    public boolean hasBalance(OfflinePlayer player, double amount) {
        if (economy == null || player == null) {
            return false;
        }
        return economy.has(player, amount);
    }

    /**
     * Get a player's balance.
     */
    public double getBalance(OfflinePlayer player) {
        if (economy == null || player == null) {
            return 0.0;
        }
        return economy.getBalance(player);
    }

    /**
     * Withdraw money from a player's account.
     * Returns true if successful.
     */
    public boolean withdraw(OfflinePlayer player, double amount) {
        if (economy == null || player == null || amount <= 0) {
            return false;
        }

        if (!hasBalance(player, amount)) {
            return false;
        }

        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }

    /**
     * Deposit money to a player's account.
     * Returns true if successful.
     */
    public boolean deposit(OfflinePlayer player, double amount) {
        if (economy == null || player == null || amount <= 0) {
            return false;
        }

        EconomyResponse response = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }

    /**
     * Format a money amount to a display string.
     */
    public String format(double amount) {
        if (economy == null) {
            return String.format("$%.2f", amount);
        }
        return economy.format(amount);
    }

    /**
     * Get the currency name (plural).
     */
    public String getCurrencyNamePlural() {
        if (economy == null) {
            return "dollars";
        }
        return economy.currencyNamePlural();
    }

    /**
     * Get the currency name (singular).
     */
    public String getCurrencyNameSingular() {
        if (economy == null) {
            return "dollar";
        }
        return economy.currencyNameSingular();
    }

    /**
     * Transfer money from one player to another.
     * This is a safe transaction that will rollback on failure.
     * Returns true if successful.
     */
    public TransferResult transfer(OfflinePlayer from, OfflinePlayer to, double amount) {
        if (economy == null || from == null || to == null || amount <= 0) {
            return new TransferResult(false, "Invalid parameters");
        }

        // Check balance
        if (!hasBalance(from, amount)) {
            return new TransferResult(false, "Insufficient funds");
        }

        // Withdraw from sender
        if (!withdraw(from, amount)) {
            return new TransferResult(false, "Failed to withdraw from sender");
        }

        // Deposit to receiver
        if (!deposit(to, amount)) {
            // Rollback: refund the sender
            deposit(from, amount);
            return new TransferResult(false, "Failed to deposit to receiver");
        }

        return new TransferResult(true, "Transfer successful");
    }

    /**
     * Result of a transfer operation.
     */
    public static class TransferResult {
        private final boolean success;
        private final String message;

        public TransferResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
