# Regional Cartographer Datapack Prototype

This prototype is intentionally **not included** in the normal Bountiful playtest overlay yet.

It tests Moonlight/Supplementaries' data-driven `supplementaries:structure_map` villager trades without requiring custom Java code.

## Exact resource path

Moonlight loads trades from `moonlight/villager_trade` and derives the target profession from the resource ID. Because the profession is `minecraft:cartographer`, these files deliberately live under:

```text
data/minecraft/moonlight/villager_trade/cartographer/
```

Do not move them under a `cozycrazyquests` namespace unless we also register a custom villager profession with that namespace.

## Current test maps

- Frostmarch Frontier: Igloo
- Greenveil Frontier: Better Jungle Temple
- Sunscar Hearthlands: Desert Tomb
- Sunscar Frontier: Better Desert Temple

These are chosen because CozyCrazyZones 0.3.6 already restricts their structure generation to the intended region/tier, making them much safer static-trade tests than a generic structure such as Dungeons Enhanced Stables.

No boss/finale maps are included. Those require authored region+tier-aware issuance later.

## Why no custom map names yet

Supplementaries passes `map_name` through `Component.translatable(...)`. A polished named map therefore needs a client-visible translation/resource asset as well as the server datapack. The first test omits custom names so we can validate trade creation, distance behavior, and Atlas interoperability before packaging client assets.

## Expected behavior

A map trade is generated from the cartographer's actual position. If Supplementaries cannot find the requested structure inside its finite map-search radius, `StructureMapListing` returns no offer. That is useful: **no nearby target means no misleading map**.
