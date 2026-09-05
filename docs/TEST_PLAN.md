# Bountiful 6.0.4 Test Plan

Static source review is not enough. This checklist is the gate for calling a quest mechanic `GREEN` in CozyCrazyCraft.

## Phase 0 — static validation

From the repository root:

```bash
python tools/validate_bountiful_content.py
```

Expected:

```text
OK: static Bountiful validation passed (0 warning(s))
```

This checks JSON structure, pool/decree references, custom-only exclusions, ranges, worths, and flags YELLOW objective/reward types.

It does **not** simulate Minecraft registries.

---

# Phase 1 — custom-only load test

Use a disposable new world with the exact CozyCrazyCraft modpack.

Install:

```text
deployment/config/bountiful/bountiful.json
...
```

into the instance's `config/bountiful/` folder.

Launch and run as an operator:

```text
/bo util debug dump
```

Inspect `latest.log`.

### PASS criteria

Loaded Bountiful content should show only our custom IDs:

```text
Decree: ccc_local_notices
Pool: ccc_local_objs
Pool: ccc_local_rews
```

There should be no built-in/default Bountiful decree/pool list and no ExtraBounties/other-mod bounty-pool contributions.

If any default pool/decree survives, stop. Do not continue content authoring until exclusion behavior is corrected.

---

# Phase 2 — direct bounty generation

Run several times:

```text
/bo gen bounty 0
```

Because only one decree is currently loaded, generated bounties should draw only from `ccc_local_objs` and `ccc_local_rews`.

Generate at least 20 samples.

Check:

- no empty objective lists
- no empty reward lists
- quantities are sensible
- no impossible/absurd combinations
- no default Bountiful content appears
- no registry errors in log

The purpose of this baseline is not final balance. It is proof that the custom-only content pipeline works.

---

# Phase 3 — exact-item objective

Generate/take a bounty requiring an exact item such as wheat or string.

1. Carry the physical bounty in normal inventory.
2. Collect part of the required amount.
3. Confirm progress updates.
4. Collect the full amount.
5. Confirm completion.
6. Cash in normally at a board.
7. Confirm the required items are consumed.
8. Confirm reward is delivered.

Then repeat once using:

```text
/bo hand complete
```

while holding the test bounty, solely as a diagnostic shortcut.

### PASS

Count/progress/removal/reward all behave exactly as expected.

---

# Phase 4 — item-tag objective

Take a `minecraft:logs` or `minecraft:wool` objective.

Use a mixture of tag members.

Example:

```text
3 oak logs
3 birch logs
remaining spruce logs
```

Confirm:

- all valid members contribute
- required quantity is consumed on redemption
- unrelated blocks do not count

Also explicitly observe which matching stacks are consumed. This validates whether broad tags are acceptable for normal play.

---

# Phase 5 — entity objective

Take a zombie/skeleton/spider bounty.

1. Carry the bounty.
2. Kill the matching entity.
3. Confirm progress increments.
4. Kill an unrelated entity and confirm it does not increment.
5. Complete and redeem.

Optional confirmation:

- let a tamed pet kill a matching target and verify 6.0.4's pet-credit behavior in this pack.

### PASS

Entity type matching is stable and does not require a special kill location.

---

# Phase 6 — fresh village board

Use a **newly generated** village/board after the custom-only baseline is installed.

The exact 1.20.1 BoardBlockEntity creates a random decree on a pristine board. Since only one decree is loaded, it should receive:

```text
Local Notices
```

Confirm:

- board visibly has Local Notices decree
- generated postings use only our pools
- postings refresh normally
- board reputation still increases normally after redemption

Do not use a stale board created before the exclusions; old decree item IDs are persisted in board NBT.

---

# Phase 7 — criteria certification procedure

No criteria objective may move from YELLOW to GREEN without this procedure.

For each exact trigger/conditions JSON:

1. add exactly one test entry to an isolated test pool
2. generate a bounty containing that criterion
3. perform the intended action once
4. prove progress increments exactly once
5. perform close-but-wrong actions and prove they do not increment
6. relog and repeat
7. test in the full modpack, not a stripped test instance
8. document the exact JSON and result in `docs/criteria_tests/`

Only then may a real quest depend on it.

Priority candidates:

- fishing rod hooked
- tame animal
- breed animals
- item used on block
- placed block

Never use `tick` or `enter_block` for Bountiful 6.0.4 criteria.

---

# Phase 8 — modded reward certification

Before adding a modded reward to a shipped pool:

1. verify exact registry ID in the installed jar/registry
2. `/give` the exact item in the full pack
3. confirm it has the expected mechanic
4. estimate actual progression power
5. add it to an isolated reward pool
6. generate/redeem a bounty
7. confirm it survives Bountiful serialization/reward delivery

For complex custom NBT (Quality Equipment properties, named enchanted Spartan weapons), capture the real stack's NBT using Bountiful's own:

```text
/bo hand
```

and compare/test before authoring the reward JSON.

No guessed NBT.

---

# Phase 9 — future proof-item certification

Once CozyCrazyCraft proof items exist:

1. verify every proof uses a **unique registry item ID**
2. ensure the structure contains exactly the intended proof placement
3. carry the matching bounty
4. collect the proof
5. ensure completion occurs
6. redeem and ensure proof is consumed
7. ensure another proof item's NBT/name cannot satisfy it

This test specifically protects us against the known design mistake of trying to distinguish proof items by NBT.

---

# Definition of done for a Bountiful mechanic

A quest mechanic is not "working" because its JSON loads.

It is working only when:

- exact 1.20.1 source supports it
- registry IDs are real
- it passes the full-pack in-game test
- failure/edge cases are understood
- the quest does not require an unbuilt custom event system

That is the standard for this repository.
