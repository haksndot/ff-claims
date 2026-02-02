package com.haksnbot.ffclaims;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Gives players a guidebook explaining claims and the market.
 */
public class ClaimsBookCommand implements CommandExecutor {

    private final FFClaimsPlugin plugin;

    public ClaimsBookCommand(FFClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        ItemStack book = createGuideBook();

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                    "Your inventory is full! Make room for the guidebook.");
            return true;
        }

        player.getInventory().addItem(book);
        player.sendMessage(plugin.getConfigManager().getPrefix() +
                "Claims guidebook added to your inventory.");
        return true;
    }

    private ItemStack createGuideBook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        meta.setTitle("Claims Guide");
        meta.setAuthor("Finite Frontier");
        meta.setGeneration(BookMeta.Generation.ORIGINAL);

        List<Component> pages = new ArrayList<>();

        // Page 1: Title / TOC
        pages.add(Component.text()
                .append(Component.text("Claims Guide\n\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text("1. Creating Claims\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("2. Managing Trust\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("3. Claim Blocks\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("4. Naming Claims\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("5. Selling Claims\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("6. Auctions\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("7. Commands\n", NamedTextColor.DARK_GREEN))
                .build());

        // Page 2: Creating Claims
        pages.add(Component.text()
                .append(Component.text("Creating Claims\n\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text("Use a ", NamedTextColor.BLACK))
                .append(Component.text("golden axe", NamedTextColor.GOLD))
                .append(Component.text(" to claim land.\n\n", NamedTextColor.BLACK))
                .append(Component.text("1. Right-click one corner\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("2. Right-click opposite corner\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("Minimum: 2 wide, 4 blocks\n\n", NamedTextColor.BLACK))
                .append(Component.text("Right-click with a ", NamedTextColor.BLACK))
                .append(Component.text("stick", NamedTextColor.GOLD))
                .append(Component.text(" to see claim boundaries.", NamedTextColor.BLACK))
                .build());

        // Page 3: Managing Trust
        pages.add(Component.text()
                .append(Component.text("Trust Levels\n\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text("/trust <player>\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("Full build access\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("/containertrust <player>\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("Chests, animals, beds\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("/accesstrust <player>\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("Buttons, levers, doors\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("/untrust <player>\n", NamedTextColor.DARK_RED))
                .append(Component.text("Remove all trust", NamedTextColor.DARK_GRAY))
                .build());

        // Page 4: Trust Tips
        pages.add(Component.text()
                .append(Component.text("Trust Tips\n\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text("/trustlist\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("See who's trusted\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("/trust public\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("Trust everyone (careful!)\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("Stand in a claim when using trust commands, or they apply to ALL your claims.", NamedTextColor.BLACK))
                .build());

        // Page 5: Claim Blocks
        pages.add(Component.text()
                .append(Component.text("Claim Blocks\n\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text("/claimslist\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("See your claims & blocks\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("/buyclaimblocks <amount>\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("Buy more claim blocks\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("/sellclaimblocks <amount>\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("Sell blocks for cash\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("/claimtop\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("Leaderboard by blocks", NamedTextColor.DARK_GRAY))
                .build());

        // Page 6: Abandoning Claims
        pages.add(Component.text()
                .append(Component.text("Abandoning\n\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text("/abandonclaim\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("Delete claim you're in\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("/abandonallclaims\n", NamedTextColor.DARK_RED))
                .append(Component.text("Delete ALL claims!\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("You get your claim blocks back when abandoning.", NamedTextColor.BLACK))
                .build());

        // Page 7: Naming Claims
        pages.add(Component.text()
                .append(Component.text("Naming Claims\n\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text("/nameclaim <name>\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("Name your claim\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("/nameclaim clear\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("Remove the name\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("Names show:\n", NamedTextColor.BLACK))
                .append(Component.text("- As title on entry\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("- On dynmap\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("- In /claimslist", NamedTextColor.DARK_GRAY))
                .build());

        // Page 8: Selling Claims
        pages.add(Component.text()
                .append(Component.text("Selling Claims\n\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text("Place a sign in your claim:\n\n", NamedTextColor.BLACK))
                .append(Component.text("[For Sale]\n", NamedTextColor.DARK_BLUE))
                .append(Component.text("50000\n\n", NamedTextColor.BLACK))
                .append(Component.text("Price formats:\n", NamedTextColor.BLACK))
                .append(Component.text("50000, 50k, 1.5M\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("Buyers right-click to purchase.", NamedTextColor.BLACK))
                .build());

        // Page 9: Sale Info
        pages.add(Component.text()
                .append(Component.text("Sale Details\n\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text("Minimum price:\n", NamedTextColor.BLACK))
                .append(Component.text("blocks x $100/block\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("On sale:\n", NamedTextColor.BLACK))
                .append(Component.text("- Money transfers\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("- Claim transfers\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("- Blocks transfer\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("Break sign to cancel.", NamedTextColor.BLACK))
                .build());

        // Page 10: Auctions
        pages.add(Component.text()
                .append(Component.text("Auctions\n\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text("Place a sign:\n\n", NamedTextColor.BLACK))
                .append(Component.text("[Auction]\n", NamedTextColor.DARK_BLUE))
                .append(Component.text("min:10000\n", NamedTextColor.BLACK))
                .append(Component.text("now:25000\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("2d12h\n\n", NamedTextColor.BLACK))
                .append(Component.text("min = minimum bid\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("now = buy-now (optional)\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("2d12h = duration", NamedTextColor.DARK_GRAY))
                .build());

        // Page 11: Vickrey Auctions
        pages.add(Component.text()
                .append(Component.text("Vickrey Auctions\n\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text("Bids are ", NamedTextColor.BLACK))
                .append(Component.text("sealed", NamedTextColor.DARK_RED, TextDecoration.BOLD))
                .append(Component.text(" - nobody sees other bids.\n\n", NamedTextColor.BLACK))
                .append(Component.text("Winner pays the ", NamedTextColor.BLACK))
                .append(Component.text("second-highest", NamedTextColor.DARK_GREEN))
                .append(Component.text(" bid, not their own.\n\n", NamedTextColor.BLACK))
                .append(Component.text("This rewards honest bidding - bid what it's worth to you!", NamedTextColor.DARK_GRAY))
                .build());

        // Page 12: Market Commands
        pages.add(Component.text()
                .append(Component.text("Market Commands\n\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text("/ffc list\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("All active listings\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("/ffc mybids\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("Your auction bids\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("/ffc mylistings\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("Your active listings\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("/ffc transactions\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("Recent sales history", NamedTextColor.DARK_GRAY))
                .build());

        // Page 13: Quick Reference
        pages.add(Component.text()
                .append(Component.text("Quick Reference\n\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text("Golden Axe", NamedTextColor.GOLD))
                .append(Component.text(" = Claim tool\n", NamedTextColor.BLACK))
                .append(Component.text("Stick", NamedTextColor.GOLD))
                .append(Component.text(" = Inspect claims\n\n", NamedTextColor.BLACK))
                .append(Component.text("/ffc help\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("Full command list\n\n", NamedTextColor.DARK_GRAY))
                .append(Component.text("/claimsbook\n", NamedTextColor.DARK_GREEN))
                .append(Component.text("Get this book again", NamedTextColor.DARK_GRAY))
                .build());

        for (Component page : pages) {
            meta.addPages(page);
        }

        book.setItemMeta(meta);
        return book;
    }
}
