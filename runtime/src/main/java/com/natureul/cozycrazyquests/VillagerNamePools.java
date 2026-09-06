package com.natureul.cozycrazyquests;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Lazily loads the generated machine-readable pools imported from the Villager Name Bible. */
final class VillagerNamePools {
    private static final String RESOURCE = "/data/cozycrazyquests/names/villager_names.json";
    private static VillagerNamePools instance;

    private final List<String> sharedGiven;
    private final List<String> sharedSurnames;
    private final Map<String, List<String>> regionalGiven;
    private final Map<String, List<String>> regionalSurnames;

    private VillagerNamePools(
            List<String> sharedGiven,
            List<String> sharedSurnames,
            Map<String, List<String>> regionalGiven,
            Map<String, List<String>> regionalSurnames
    ) {
        this.sharedGiven = sharedGiven;
        this.sharedSurnames = sharedSurnames;
        this.regionalGiven = regionalGiven;
        this.regionalSurnames = regionalSurnames;
    }

    static VillagerNamePools get() {
        if (instance == null) instance = load();
        return instance;
    }

    List<String> sharedGiven() {
        return sharedGiven;
    }

    List<String> sharedSurnames() {
        return sharedSurnames;
    }

    List<String> regionalGiven(String macro) {
        return regionalGiven.getOrDefault(key(macro), List.of());
    }

    List<String> regionalSurnames(String macro) {
        return regionalSurnames.getOrDefault(key(macro), List.of());
    }

    private static VillagerNamePools load() {
        try (InputStream stream = VillagerNamePools.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) throw new IllegalStateException("Missing " + RESOURCE);
            JsonObject root = new Gson().fromJson(
                    new InputStreamReader(stream, StandardCharsets.UTF_8),
                    JsonObject.class
            );

            JsonObject shared = root.getAsJsonObject("shared");
            List<String> sharedGiven = readStrings(shared.getAsJsonArray("given"));
            List<String> sharedSurnames = readStrings(shared.getAsJsonArray("surnames"));
            Set<String> sharedGivenSet = new HashSet<>(sharedGiven);
            Set<String> sharedSurnameSet = new HashSet<>(sharedSurnames);

            Map<String, List<String>> regionalGiven = new HashMap<>();
            Map<String, List<String>> regionalSurnames = new HashMap<>();
            for (String region : List.of("north", "east", "south", "west")) {
                JsonObject data = root.getAsJsonObject(region);
                List<String> given = readStrings(data.getAsJsonArray("given"));
                given.removeIf(sharedGivenSet::contains);
                List<String> surnames = readStrings(data.getAsJsonArray("surnames"));
                surnames.removeIf(sharedSurnameSet::contains);
                regionalGiven.put(region, Collections.unmodifiableList(given));
                regionalSurnames.put(region, Collections.unmodifiableList(surnames));
            }

            CozyCrazyQuests.LOGGER.info(
                    "Loaded Villager Name Bible pools: {} shared given names, {} shared surnames",
                    sharedGiven.size(), sharedSurnames.size()
            );
            return new VillagerNamePools(
                    Collections.unmodifiableList(sharedGiven),
                    Collections.unmodifiableList(sharedSurnames),
                    Collections.unmodifiableMap(regionalGiven),
                    Collections.unmodifiableMap(regionalSurnames)
            );
        } catch (Throwable error) {
            CozyCrazyQuests.LOGGER.error("Could not load Villager Name Bible pools; using emergency fallback", error);
            return new VillagerNamePools(
                    List.of("Alden", "Mara", "Rowan", "Clara", "Elias", "Nora", "Jasper", "Tamsin"),
                    List.of("Carter", "Miller", "Reed", "Hale", "Walker", "Finch", "Stone", "Wren"),
                    Map.of(),
                    Map.of()
            );
        }
    }

    private static List<String> readStrings(JsonArray array) {
        List<String> result = new ArrayList<>();
        if (array == null) return result;
        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) {
                String value = element.getAsString().trim();
                if (!value.isBlank() && !result.contains(value)) result.add(value);
            }
        }
        return result;
    }

    private static String key(String macro) {
        return macro == null ? "" : macro.toLowerCase(java.util.Locale.ROOT);
    }
}
