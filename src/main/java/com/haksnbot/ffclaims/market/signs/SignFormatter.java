package com.haksnbot.ffclaims.market.signs;

import com.haksnbot.ffclaims.market.data.AuctionData;
import com.haksnbot.ffclaims.market.data.SaleData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.text.DecimalFormat;

public class SignFormatter {

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,###");

    /**
     * Format a price for display (shortened).
     */
    public static String formatPrice(double price) {
        if (price >= 1_000_000) {
            return "$" + PRICE_FORMAT.format(price / 1_000_000) + "M";
        } else if (price >= 1_000) {
            double k = price / 1_000;
            if (k == Math.floor(k)) {
                return "$" + (int) k + "k";
            }
            return "$" + String.format("%.1fk", k);
        } else {
            return "$" + PRICE_FORMAT.format(price);
        }
    }

    /**
     * Format a price for full display (no shortening).
     */
    public static String formatPriceFull(double price) {
        return "$" + PRICE_FORMAT.format(price);
    }

    /**
     * Get the sign lines for a sale listing.
     */
    public static Component[] getSaleSignLines(SaleData sale) {
        return new Component[] {
            Component.text("[For Sale]")
                .color(NamedTextColor.DARK_BLUE)
                .decoration(TextDecoration.BOLD, true),
            Component.text(formatPriceFull(sale.getPrice()))
                .color(NamedTextColor.BLACK),
            Component.text(truncateName(sale.getSellerName()))
                .color(NamedTextColor.BLACK),
            Component.text(sale.getDimensions())
                .color(NamedTextColor.DARK_GRAY)
        };
    }

    /**
     * Get the sign lines for an auction listing.
     */
    public static Component[] getAuctionSignLines(AuctionData auction) {
        String line2 = "Min: " + formatPrice(auction.getMinimumBid());
        String line3;
        if (auction.hasBuyNow()) {
            line3 = "BuyNow: " + formatPrice(auction.getBuyNowPrice());
        } else {
            line3 = auction.getSellerName();
        }
        String line4 = "Ends: " + auction.getTimeRemainingFormatted();

        return new Component[] {
            Component.text("[Auction]")
                .color(NamedTextColor.DARK_BLUE)
                .decoration(TextDecoration.BOLD, true),
            Component.text(line2)
                .color(NamedTextColor.BLACK),
            Component.text(truncateName(line3))
                .color(NamedTextColor.BLACK),
            Component.text(line4)
                .color(NamedTextColor.DARK_GRAY)
        };
    }

    /**
     * Truncate a name to fit on a sign line.
     */
    private static String truncateName(String name) {
        if (name.length() > 15) {
            return name.substring(0, 14) + "\u2026";
        }
        return name;
    }

    /**
     * Parse a price string entered by a player.
     */
    public static double parsePrice(String input) throws NumberFormatException {
        if (input == null || input.isEmpty()) {
            throw new NumberFormatException("Empty price");
        }

        String cleaned = input.trim()
            .replace("$", "")
            .replace(",", "")
            .replace(" ", "")
            .toLowerCase();

        if (cleaned.isEmpty()) {
            throw new NumberFormatException("Empty price after cleaning");
        }

        double multiplier = 1.0;

        if (cleaned.endsWith("k")) {
            multiplier = 1_000;
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        } else if (cleaned.endsWith("m")) {
            multiplier = 1_000_000;
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        } else if (cleaned.endsWith("b")) {
            multiplier = 1_000_000_000;
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        double value = Double.parseDouble(cleaned);
        return value * multiplier;
    }

    /**
     * Parse a duration string (e.g., "2d", "48h", "1d12h").
     */
    public static long parseDuration(String input) throws IllegalArgumentException {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Empty duration");
        }

        String cleaned = input.trim().toLowerCase();
        long totalMs = 0;

        StringBuilder number = new StringBuilder();
        for (char c : cleaned.toCharArray()) {
            if (Character.isDigit(c) || c == '.') {
                number.append(c);
            } else {
                if (number.length() == 0) {
                    continue;
                }

                double value = Double.parseDouble(number.toString());
                number = new StringBuilder();

                switch (c) {
                    case 'd' -> totalMs += (long) (value * 24 * 60 * 60 * 1000);
                    case 'h' -> totalMs += (long) (value * 60 * 60 * 1000);
                    case 'm' -> totalMs += (long) (value * 60 * 1000);
                    case 's' -> totalMs += (long) (value * 1000);
                    default -> throw new IllegalArgumentException("Unknown duration unit: " + c);
                }
            }
        }

        // Handle case where only a number is provided (assume hours)
        if (number.length() > 0 && totalMs == 0) {
            totalMs = (long) (Double.parseDouble(number.toString()) * 60 * 60 * 1000);
        }

        if (totalMs <= 0) {
            throw new IllegalArgumentException("Invalid duration");
        }

        return totalMs;
    }
}
