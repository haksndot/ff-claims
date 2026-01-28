package com.haksnbot.ffclaims.market.signs;

import com.haksnbot.ffclaims.FFClaimsPlugin;
import com.haksnbot.ffclaims.market.data.AuctionData;
import com.haksnbot.ffclaims.market.data.SaleData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

public class SignManager {

    private final FFClaimsPlugin plugin;

    public SignManager(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if the sign text indicates a sale listing.
     */
    public boolean isSaleSign(String[] lines) {
        if (lines == null || lines.length == 0) {
            return false;
        }
        String firstLine = lines[0].trim().toLowerCase();
        return firstLine.equals("[for sale]") || firstLine.equals("[forsale]");
    }

    /**
     * Check if the sign text indicates an auction listing.
     */
    public boolean isAuctionSign(String[] lines) {
        if (lines == null || lines.length == 0) {
            return false;
        }
        String firstLine = lines[0].trim().toLowerCase();
        return firstLine.equals("[auction]");
    }

    /**
     * Parse sale sign input from player.
     */
    public SaleSignInput parseSaleSign(String[] lines) throws IllegalArgumentException {
        if (!isSaleSign(lines)) {
            throw new IllegalArgumentException("Not a sale sign");
        }

        String priceStr = lines.length > 1 ? lines[1].trim() : "";
        if (priceStr.isEmpty()) {
            throw new IllegalArgumentException("No price specified");
        }

        double price = SignFormatter.parsePrice(priceStr);

        double minPrice = plugin.getConfigManager().getMinSalePrice();
        double maxPrice = plugin.getConfigManager().getMaxSalePrice();

        if (price < minPrice) {
            throw new IllegalArgumentException("Price too low. Minimum: " + SignFormatter.formatPriceFull(minPrice));
        }

        if (maxPrice > 0 && price > maxPrice) {
            throw new IllegalArgumentException("Price too high. Maximum: " + SignFormatter.formatPriceFull(maxPrice));
        }

        return new SaleSignInput(price);
    }

    /**
     * Parse auction sign input from player.
     */
    public AuctionSignInput parseAuctionSign(String[] lines) throws IllegalArgumentException {
        if (!isAuctionSign(lines)) {
            throw new IllegalArgumentException("Not an auction sign");
        }

        double minimumBid = 0;
        double buyNowPrice = 0;
        long durationMs = 0;

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim().toLowerCase();
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("min:")) {
                minimumBid = SignFormatter.parsePrice(line.substring(4));
            } else if (line.startsWith("now:") || line.startsWith("buy:") || line.startsWith("buynow:")) {
                String priceStr = line.contains(":") ? line.substring(line.indexOf(":") + 1) : line;
                buyNowPrice = SignFormatter.parsePrice(priceStr);
            } else if (line.matches("^[\\d.]+[dhms].*$")) {
                durationMs = SignFormatter.parseDuration(line);
            } else if (minimumBid == 0) {
                try {
                    minimumBid = SignFormatter.parsePrice(line);
                } catch (NumberFormatException e) {
                    try {
                        durationMs = SignFormatter.parseDuration(line);
                    } catch (IllegalArgumentException ex) {
                        // Ignore
                    }
                }
            }
        }

        if (minimumBid <= 0) {
            throw new IllegalArgumentException("No minimum bid specified");
        }

        // Use default duration if not specified
        if (durationMs <= 0) {
            durationMs = plugin.getConfigManager().getDefaultAuctionDurationHours() * 60 * 60 * 1000L;
        }

        // Validate duration
        long minDurationMs = plugin.getConfigManager().getMinAuctionDurationHours() * 60 * 60 * 1000L;
        long maxDurationMs = plugin.getConfigManager().getMaxAuctionDurationDays() * 24 * 60 * 60 * 1000L;

        if (durationMs < minDurationMs) {
            throw new IllegalArgumentException("Duration too short. Minimum: " +
                    plugin.getConfigManager().getMinAuctionDurationHours() + " hours");
        }

        if (durationMs > maxDurationMs) {
            throw new IllegalArgumentException("Duration too long. Maximum: " +
                    plugin.getConfigManager().getMaxAuctionDurationDays() + " days");
        }

        // Buy-now must be higher than minimum
        if (buyNowPrice > 0 && buyNowPrice <= minimumBid) {
            throw new IllegalArgumentException("Buy-now price must be higher than minimum bid");
        }

        return new AuctionSignInput(minimumBid, buyNowPrice, durationMs);
    }

    /**
     * Update a sign block with sale information.
     */
    public void updateSaleSign(Location signLocation, SaleData sale) {
        Block block = signLocation.getBlock();
        if (!(block.getState() instanceof Sign sign)) {
            return;
        }

        Component[] lines = SignFormatter.getSaleSignLines(sale);

        sign.getSide(Side.FRONT).line(0, lines[0]);
        sign.getSide(Side.FRONT).line(1, lines[1]);
        sign.getSide(Side.FRONT).line(2, lines[2]);
        sign.getSide(Side.FRONT).line(3, lines[3]);

        sign.setWaxed(true);
        sign.update();
    }

    /**
     * Update a sign block with auction information.
     */
    public void updateAuctionSign(Location signLocation, AuctionData auction) {
        Block block = signLocation.getBlock();
        if (!(block.getState() instanceof Sign sign)) {
            return;
        }

        Component[] lines = SignFormatter.getAuctionSignLines(auction);

        sign.getSide(Side.FRONT).line(0, lines[0]);
        sign.getSide(Side.FRONT).line(1, lines[1]);
        sign.getSide(Side.FRONT).line(2, lines[2]);
        sign.getSide(Side.FRONT).line(3, lines[3]);

        sign.setWaxed(true);
        sign.update();
    }

    /**
     * Remove a listing sign (break the sign block).
     */
    public void removeSign(Location signLocation) {
        Block block = signLocation.getBlock();
        if (block.getState() instanceof Sign) {
            block.breakNaturally();
        }
    }

    /**
     * Get plain text lines from a sign.
     */
    public String[] getSignLines(Sign sign) {
        String[] lines = new String[4];
        PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();

        for (int i = 0; i < 4; i++) {
            Component line = sign.getSide(Side.FRONT).line(i);
            lines[i] = serializer.serialize(line);
        }

        return lines;
    }

    // ==================== INPUT CLASSES ====================

    public static class SaleSignInput {
        private final double price;

        public SaleSignInput(double price) {
            this.price = price;
        }

        public double getPrice() {
            return price;
        }
    }

    public static class AuctionSignInput {
        private final double minimumBid;
        private final double buyNowPrice;
        private final long durationMs;

        public AuctionSignInput(double minimumBid, double buyNowPrice, long durationMs) {
            this.minimumBid = minimumBid;
            this.buyNowPrice = buyNowPrice;
            this.durationMs = durationMs;
        }

        public double getMinimumBid() {
            return minimumBid;
        }

        public double getBuyNowPrice() {
            return buyNowPrice;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public boolean hasBuyNow() {
            return buyNowPrice > 0;
        }
    }
}
