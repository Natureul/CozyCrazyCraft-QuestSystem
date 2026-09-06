package com.natureul.cozycrazyquests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class StoryCatalog {
    public record Card(String title, String issuer, String noticeClass, String body, int trust) {}

    private static final String RESOURCE = "/assets/cozycrazyquests/story/live_notice_stories.json";
    private static final Map<String, Card> CARDS = load();

    private StoryCatalog() {}

    public static Card get(String id) {
        return id == null ? null : CARDS.get(id);
    }

    public static Map<String, Card> all() {
        return CARDS;
    }

    private static Map<String, Card> load() {
        try (InputStream stream = StoryCatalog.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) {
                CozyCrazyQuests.LOGGER.warn("Quest story catalog resource {} is missing", RESOURCE);
                return Collections.emptyMap();
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, Card> result = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                JsonObject value = entry.getValue().getAsJsonObject();
                result.put(entry.getKey(), new Card(
                    text(value, "title", entry.getKey()),
                    text(value, "issuer", "Village Notice"),
                    text(value, "class", "LOCAL NOTICE"),
                    text(value, "body", ""),
                    value.has("trust") ? value.get("trust").getAsInt() : 0
                ));
            }
            CozyCrazyQuests.LOGGER.info("Loaded {} CozyCrazyCraft story-card definitions", result.size());
            return Collections.unmodifiableMap(result);
        } catch (Throwable error) {
            CozyCrazyQuests.LOGGER.warn("Could not load quest story catalog", error);
            return Collections.emptyMap();
        }
    }

    private static String text(JsonObject value, String key, String fallback) {
        return value.has(key) ? value.get(key).getAsString() : fallback;
    }
}
