# FF-Claims

**Finite Frontier Claims Pack** — a Paper plugin that extends [GriefPrevention](https://github.com/TechFortress/GriefPrevention) with claim naming, a sign-based claim marketplace, and sealed-bid Vickrey auctions.

Built for the [Finite Frontier](https://haksndot.com) Minecraft community, where players have been naming, buying, and auctioning land claims for years. This plugin bundles those features into a single, modular package.

## Features

### Naming Module

Give your claims custom names that show up everywhere — on entry, on dynmap, and in claim listings.

- **`/nameclaim <name>`** — Name the claim you're standing in (supports `&` color codes)
- **`/nameclaim clear`** — Remove the name
- **`/nameclaim`** — View the current claim's name
- **`/claimtop [page]`** — Leaderboard ranking players by total claim blocks
- **`/claimsbook`** — Get a guidebook explaining claims and the market (includes GP basics)
- **Title on entry** — Players see the claim name as a title when they walk in
- **`/claimslist` enhancement** — Intercepts GriefPrevention's claim list to show names alongside coordinates
- **Dynmap markers** — Named claims appear as markers on the web map (requires [dynmap](https://github.com/webbukkit/dynmap))

### Market Module

Sell claims with signs. Run auctions with sealed bids and Vickrey pricing.

#### Sales

Place a sign inside your claim:

```
[For Sale]
50000
```

The sign auto-formats with the price, your name, and claim dimensions. Other players right-click the sign to open a purchase GUI with a confirmation step. On purchase, the money transfers via Vault and the claim ownership transfers via GriefPrevention — with full rollback if any step fails.

Price supports shorthand: `50k`, `1.5M`, or plain numbers like `50000`.

**Minimum Price:** The plugin enforces a minimum price based on claim block value. If GriefPrevention's economy is enabled, no claim can be listed below `area × ClaimBlocksPurchaseCost`. This prevents players from accidentally selling land below its claim block value. Want to give away a claim for less? Abandon it and let the recipient claim it themselves.

#### Auctions (Sealed-Bid Vickrey)

Place a sign inside your claim:

```
[Auction]
min:10000
now:25000
2d12h
```

| Line | Meaning |
|------|---------|
| `min:<price>` | Minimum bid |
| `now:<price>` | Buy-now price (optional) |
| Duration | How long the auction runs (`2d`, `48h`, `2d12h`, etc.) |

**How Vickrey auctions work:** All bids are sealed — bidders don't see each other's amounts. When the auction ends, the highest bidder wins but pays the **second-highest bid**. This incentivizes honest bidding since you can't gain by underbidding. If there's only one bid, the winner pays the minimum bid.

Example:
- Minimum: $5,000
- Bids: $15,000 · $12,000 · $8,000
- Winner pays: **$12,000** (second-highest), not $15,000

Players bid through a sign GUI that opens when they click "Place Sealed Bid" in the auction menu. If a bid meets the buy-now price, the claim sells immediately.

An expiration task runs periodically to settle ended auctions and update countdown timers on signs.

#### Claim Block Transfer

When a claim is sold (via sale or auction), the buyer receives bonus claim blocks equal to the claim's area. This ensures the buyer can own the claim without needing to have accumulated enough blocks beforehand. The seller effectively "transfers" their invested claim blocks to the buyer.

#### Transaction History

All completed transactions are permanently logged to `transactions.yml`. Each transaction records:
- Transaction ID (TX000001, TX000002, etc.)
- Type (SALE, AUCTION, or BUY_NOW)
- Timestamp
- Seller and buyer names/UUIDs
- Price paid
- Claim details (size, dimensions, location, name)
- For auctions: winning bid vs. Vickrey price paid, bid count

Use `/ffc transactions` to view recent transactions or `/ffc tx TX000123` to view details of a specific transaction.

#### Market Commands

| Command | Description |
|---------|-------------|
| `/ffc list` | View all active sales and auctions |
| `/ffc mybids` | View auctions you've bid on |
| `/ffc mylistings` | View your active listings |
| `/ffc transactions [count]` | View recent transactions (default 10, max 50) |
| `/ffc tx <TX#>` | View details of a specific transaction |
| `/ffc help` | Sign formats and command help |
| `/ffc reload` | Reload config (admin) |
| `/ffc transactions <player>` | View a player's transactions (admin) |

## Requirements

| Dependency | Required | Purpose |
|------------|----------|---------|
| [Paper](https://papermc.io/) 1.21+ | Yes | Server platform |
| [GriefPrevention](https://github.com/TechFortress/GriefPrevention) | Yes | Claims system |
| [Vault](https://github.com/MilkBowl/Vault) + economy plugin | For market | Money transfers |
| [dynmap](https://github.com/webbukkit/dynmap) | Optional | Web map markers for named claims |

The naming module works without Vault. The market module gracefully disables itself if Vault isn't present.

### Dual-Currency Compatibility

If the DualCurrency plugin is detected (where claim blocks are purchased with items like netherite instead of cash), the automatic minimum price enforcement is disabled. This is because there's no direct conversion between the item currency and the cash economy used for real estate transactions.

## Installation

1. Drop `FFClaims-<version>.jar` into your server's `plugins/` folder
2. Restart the server
3. Edit `plugins/FFClaims/config.yml` to taste
4. `/ffc reload` to apply changes

## Building

```bash
./gradlew shadowJar
```

The output jar lands in `build/libs/`. The shadow jar bundles the [SignGUI](https://github.com/Rapha149/SignGUI) library used for bid input.

## Configuration

Both modules can be toggled independently:

```yaml
modules:
  naming: true
  market: true
```

### Naming

```yaml
naming:
  max-name-length: 32
  display:
    fade-in: 5      # ticks
    stay: 40
    fade-out: 10
```

### Market

```yaml
market:
  sales:
    min-price: 100.0
    max-price: 0.0           # 0 = no limit

  auctions:
    min-duration-hours: 1
    max-duration-days: 14
    default-duration-hours: 72
    expiration-check-interval: 30  # seconds

  signs:
    sale-header: "[For Sale]"
    auction-header: "[Auction]"

  gui:
    sale-menu-title: "&1Purchase Claim"
    auction-menu-title: "&1Auction Details"
    confirm-title: "&4Confirm Purchase"
```

All messages are configurable under `messages.naming.*` and `messages.market.*` in `config.yml`, with `&` color code support.

## Permissions

### Naming

| Permission | Default | Description |
|------------|---------|-------------|
| `ffclaims.naming.use` | true | Use `/nameclaim` on your own claims |
| `ffclaims.naming.claimtop` | true | Use `/claimtop` |
| `ffclaims.naming.admin` | op | Name any claim regardless of ownership |
| `ffclaims.book` | true | Use `/claimsbook` to get the guidebook |

### Market

| Permission | Default | Description |
|------------|---------|-------------|
| `ffclaims.market.sell` | true | Create sale listings |
| `ffclaims.market.auction` | true | Create auction listings |
| `ffclaims.market.buy` | true | Purchase claims and place bids |
| `ffclaims.market.use` | true | Use `/ffc list`, `mybids`, `mylistings` |

### Admin

| Permission | Default | Description |
|------------|---------|-------------|
| `ffclaims.admin` | op | Reload config, cancel any listing |

## Data Storage

All data is stored as YAML files in the plugin folder:

| File | Contents |
|------|----------|
| `naming-data.yml` | Claim ID → name mappings |
| `market-sales.yml` | Active sale listings |
| `market-auctions.yml` | Active auctions with all bids |
| `transactions.yml` | Permanent log of all completed transactions |

## Architecture

```
com.haksnbot.ffclaims
├── FFClaimsPlugin          Main plugin, module initialization
├── FFClaimsCommand          /ffc command router
├── config/
│   └── ConfigManager        Unified config access
├── hooks/
│   ├── GriefPreventionHook  Claim operations (shared by both modules)
│   └── VaultHook            Economy operations (market only)
├── naming/
│   ├── NamingDataManager    YAML persistence for claim names
│   ├── NameClaimCommand     /nameclaim executor
│   ├── ClaimTopCommand      /claimtop executor
│   ├── ClaimEntryListener   Title display on claim entry
│   ├── ClaimsListListener   Enhanced /claimslist output
│   └── DynmapIntegration    Web map markers
└── market/
    ├── data/
    │   ├── MarketDataManager  YAML persistence, indexing
    │   ├── TransactionLogger  Permanent transaction history
    │   ├── SaleData           Sale listing model
    │   ├── AuctionData        Auction model with Vickrey settlement
    │   └── BidData            Individual bid record
    ├── managers/
    │   ├── ListingManager     Validation, queries
    │   ├── SaleManager        Sale creation & purchase flow
    │   └── AuctionManager     Auction creation, bidding, expiration
    ├── signs/
    │   ├── SignManager        Sign parsing & placement
    │   └── SignFormatter      Price/duration formatting
    ├── gui/
    │   ├── MenuManager        Open menu tracking
    │   ├── SaleMenu           Purchase confirmation GUI
    │   ├── AuctionMenu        Bid/buy-now GUI
    │   └── BidInputHandler    Sign-based bid input
    ├── listeners/
    │   ├── MarketSignListener         Sign creation & break events
    │   ├── MarketSignInteractListener  Right-click to open menu
    │   └── InventoryClickListener     GUI click handling
    └── tasks/
        └── AuctionExpirationTask      Periodic auction settlement
```

## License

MIT
