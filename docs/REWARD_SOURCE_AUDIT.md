# Reward Source Audit

This document upgrades reward ideas from pure brainstorming toward implementable candidates.

**Important:** source-checked does not mean full-pack-tested. An item still must survive an actual CozyCrazyCraft give/redeem/equip/use test before it becomes a live signature bounty reward.

## Audit states

- **SOURCE-CHECKED** — exact registry path or mechanic is confirmed in upstream source matching the relevant Minecraft/mod line.
- **FULL-PACK TEST REQUIRED** — source looks correct, but the installed pack still needs in-game verification.
- **HOLD / KNOWN RISK** — upstream issue or compatibility concern means we should not build progression around it yet.

---

# High-priority findings

## Alex's Mobs — Acacia Blossom / elephant progression

**Status: SOURCE-CHECKED, HIGH CONFIDENCE**

Exact item:

```text
alexsmobs:acacia_blossom
```

Upstream 1.20 source/language data confirms:

- the Acacia Blossom item exists;
- the `Flowers of the Savanna` advancement is awarded for obtaining one from Acacia Leaves;
- the elephant-taming advancement explicitly describes taming an elephant with Acacia Blossoms;
- Alex's Mobs defines an `elephant_tameables` item tag;
- wandering traders can also sell Acacia Blossoms.

### Quest consequence

`Flowers of the Savanna` remains one of the strongest regional reward ideas in the project.

It does **not** invent an otherwise inaccessible mechanic. It provides a reliable authored path into a real Alex's Mobs mechanic that would otherwise be easy to miss or leave to chance.

Recommended placement:

- Sunscar Hearthlands or early Frontier
- botanical/naturalist objective
- reward several blossoms, not a single teasing blossom
- player still finds and tames the elephant naturally

**Next test:** determine how many blossoms feels fair in the installed `alexsmobs-1.22.9` build and verify an ordinary Bountiful item reward hands them over correctly.

---

## Backpacked — Unlock Token

**Status: SOURCE-CHECKED, HIGH CONFIDENCE**

Exact item:

```text
backpacked:unlock_token
```

Upstream Backpacked 3.0 source registers `unlock_token`, and the 3.0 progression changelog explicitly describes Unlock Tokens as **reward items intended for modpack developers**. They can be used to unlock backpack inventory slots, augment bays, and/or equippable slots depending on config.

This is unusually well suited to our quest system because it was designed for almost exactly this purpose.

### Quest consequence

Unlock Tokens should become a recurring but controlled medium-value reward across the world:

- first serious courier expedition
- structure recovery contracts
- reputation milestones
- selected Frontier/Wildlands contracts

Do not turn every bounty into a token farm. They should be exciting enough that a player notices one in the reward column.

**Next test:** inspect the current Backpacked config to see which unlock categories tokens are allowed to affect in this pack, then test one token end-to-end.

---

# Cold Sweat 2.4.2

The installed pack log identifies Cold Sweat `2.4.2`; upstream `1.20.1-FG` source also identifies mod version 2.4.2.

## Thermometer

**Status: SOURCE-CHECKED**

Exact item:

```text
cold_sweat:thermometer
```

Upstream source registers `thermometer`, and Cold Sweat has a synchronized `require_thermometer` setting. The Thermometer can also integrate with Curios in this version line.

### Quest consequence

This is an excellent first regional-survival reward because it **teaches the system** instead of simply making the player immune to it.

Recommended:

- Frostmarch Hearthlands: Outfitter's Order
- Sunscar Hearthlands alternative if player reaches the hot direction first
- do not spam duplicates once the player already owns one if we later add player-aware authored issuance

## Waterskin

**Status: SOURCE-CHECKED; FILLED STATE NEEDS TEST**

Exact base item IDs:

```text
cold_sweat:waterskin
cold_sweat:filled_waterskin
```

Cold Sweat 2.4.2 exposes Waterskin use count, consumption strength, and temperature neutralization behavior through config/source.

### Quest consequence

An **empty Waterskin** is a safer direct item reward than a specially temperature-prepared filled Waterskin until exact stack data is captured.

For a quest that promises a chilled/warmed filled waterskin, capture the real item stack from the installed game first; do not guess NBT/component state.

## Sewing Table

**Status: SOURCE-CHECKED**

Exact item/block path:

```text
cold_sweat:sewing_table
```

The 1.20.1 source registers the block item directly and the changelog documents the sewing table as the mechanism for adding/removing insulation.

### Quest consequence

Very strong Frostmarch Frontier progression reward or settlement-service unlock.

It is probably better as a **deliberate authored progression reward** than as a random repeatable Bountiful payout.

## Hearth / Boiler / Icebox

**Status: SOURCE-CHECKED IDs; BALANCE/DELIVERY TEST REQUIRED**

Source registers block items under:

```text
cold_sweat:hearth
cold_sweat:boiler
cold_sweat:icebox
```

### Quest consequence

Treat these primarily as settlement/base infrastructure rewards:

- North Frontier: Hearth/Boiler-oriented infrastructure
- South Frontier: Icebox/cooling infrastructure

The most atmospheric implementation may be for a quest to establish the device **in the settlement**, with personal recipe/item access as a secondary reward. Do not build that world-changing version until placement persistence is proven.

---

# Majrusz's Accessories

## Ancient Scarab

**Status: SOURCE-CHECKED ID; FULL-PACK EFFECT TEST REQUIRED**

Exact source path:

```text
majruszsaccessories:ancient_scarab
```

This remains a high-priority Sunscar archaeology reward candidate. Exact 1.20.1 installed behavior should still be verified before the quest promises a specific numeric effect.

## Nature Rune

**Status: SOURCE-CHECKED ID; FULL-PACK EFFECT TEST REQUIRED**

The recipe/source ecosystem confirms:

```text
majruszsaccessories:nature_rune
```

The Nature Rune recipe consumes, among other ingredients:

```text
majruszsaccessories:certificate_of_taming
majruszsaccessories:tamed_potato_beetle
```

This reinforces the design relationship among animal/farming accessories, but it also argues for caution about handing the finished Nature Rune out too early: it may skip an intended accessory-combination progression loop.

**Design review:** consider rewarding one of its ingredient accessories first, with the Nature Rune becoming a later Greenveil/Harvestlands progression reward rather than a routine Frontier payout.

## Tamed Potato Beetle

**Status: SOURCE-CHECKED ID; FULL-PACK EFFECT TEST REQUIRED**

Exact item path:

```text
majruszsaccessories:tamed_potato_beetle
```

This remains an excellent Harvestlands reward because it is flavorful agricultural utility rather than generic combat power.

## Certificate of Taming

**Status: HOLD / KNOWN 1.20.1 RISK**

Exact source item path:

```text
majruszsaccessories:certificate_of_taming
```

However, the Majrusz's Accessories issue tracker contains an open Forge 1.20.1 report titled **"Certificate of Taming does not work"**.

### Quest consequence

Do **not** make this the core payoff of `The Elephant Keeper` until the installed pack proves the item works.

The Elephant Keeper quest is still safe because Acacia Blossoms themselves are a much stronger and more direct reward. If the Certificate fails our test, simply omit it rather than holding the entire quest hostage to a buggy accessory.

## Swimmer's Guide

**Status: HOLD FOR AQUAMIRAE COMPATIBILITY TEST**

There is an open upstream report involving Swimmer's Guide and Aquamirae/Valoria world-generation behavior.

### Quest consequence

Do not currently use Swimmer's Guide as a mandatory North/Aquamirae preparation reward. That is exactly where an Aquamirae compatibility concern would hurt us most.

Prefer simpler water/cold preparation until we have tested the installed combination.

---

# Reward implementation policy after this audit

### Safe to prototype first

- `alexsmobs:acacia_blossom`
- `backpacked:unlock_token`
- `cold_sweat:thermometer`
- `cold_sweat:waterskin`
- `cold_sweat:sewing_table`
- vanilla saddle

These still need in-game reward-delivery testing, but their identity and intended mechanic are well supported.

### Source-checked but effect/balance audit still needed

- `majruszsaccessories:ancient_scarab`
- `majruszsaccessories:nature_rune`
- `majruszsaccessories:tamed_potato_beetle`
- Cold Sweat Hearth/Boiler/Icebox

### Explicit hold

- Certificate of Taming as a signature progression reward
- Swimmer's Guide in the Aquamirae path
- special filled Waterskins without captured stack data
- any Quality Equipment curated stack until exact installed stack metadata is captured
- direct tamed-pet rewards until deterministic ownership transfer is tested

---

# Philosophy reinforced by the audit

The strongest quest rewards are not necessarily rare objects.

A quest that gives the player the **right tool for a real mechanic** can be much more satisfying:

- Blossoms → tame an elephant
- Thermometer → understand temperature
- Sewing Table → deliberately build insulation
- Unlock Token → expand expedition capacity
- Ancient Scarab → lean into archaeology

That is the reward language CozyCrazyCraft should favor over generic ore bundles.
