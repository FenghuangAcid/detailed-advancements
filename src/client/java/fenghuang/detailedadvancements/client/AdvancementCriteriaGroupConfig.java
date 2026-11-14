package fenghuang.detailedadvancements.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;

public final class AdvancementCriteriaGroupConfig {

    private static final String RESOURCE_PATH =
            "/assets/detailed-advancements/criteria_groups.json";

    private static final Map<String, List<Group>> GROUPS = load();
    private static final HashSet<String> DEBUGGED_LOOKUPS = new HashSet<>();

    private AdvancementCriteriaGroupConfig() {
    }

    public static List<Group> getGroups(String advancementId) {
        List<Group> groups = GROUPS.getOrDefault(advancementId, List.of());

        if (DEBUGGED_LOOKUPS.add(advancementId)) {
            System.out.println(
                    "[DetailedAdvancements] "
                            + advancementId
                            + " -> "
                            + groups.size()
                            + " custom group(s)"
            );
        }

        return groups;
    }

    private static Map<String, List<Group>> load() {
        try (
                InputStream input =
                        AdvancementCriteriaGroupConfig.class.getResourceAsStream(RESOURCE_PATH)
        ) {
            if (input == null) {
                System.err.println(
                        "[DetailedAdvancements] Cannot find " + RESOURCE_PATH
                );
                return Map.of();
            }

            try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

                HashMap<String, List<Group>> result = new HashMap<>();

                for (Map.Entry<String, JsonElement> advancementEntry : root.entrySet()) {
                    JsonObject advancementObject =
                            advancementEntry.getValue().getAsJsonObject();

                    JsonArray groupsJson =
                            advancementObject.getAsJsonArray("groups");

                    ArrayList<Group> groups = new ArrayList<>();

                    for (JsonElement groupElement : groupsJson) {
                        JsonObject groupObject = groupElement.getAsJsonObject();

                        String translationKey =
                                groupObject.get("translation_key").getAsString();

                        String modeText =
                                groupObject.get("mode").getAsString();

                        Mode mode = switch (modeText) {
                            case "any" -> Mode.ANY;
                            case "all" -> Mode.ALL;
                            default -> throw new IllegalArgumentException(
                                    "Unknown criteria group mode: " + modeText
                            );
                        };

                        JsonArray criteriaJson =
                                groupObject.getAsJsonArray("criteria");

                        ArrayList<String> criteria = new ArrayList<>();

                        for (JsonElement criterion : criteriaJson) {
                            criteria.add(criterion.getAsString());
                        }

                        if (criteria.isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Criteria group cannot be empty: " + translationKey
                            );
                        }

                        groups.add(
                                new Group(
                                        translationKey,
                                        mode,
                                        List.copyOf(criteria)
                                )
                        );
                    }

                    result.put(
                            advancementEntry.getKey(),
                            List.copyOf(groups)
                    );
                }
                System.out.println(
                        "[DetailedAdvancements] Loaded criteria groups: " + result.keySet()
                );
                return Map.copyOf(result);
            }

        } catch (Exception exception) {
            System.err.println(
                    "[DetailedAdvancements] Failed to load criteria groups."
            );
            exception.printStackTrace();
            return Map.of();
        }
    }

    public enum Mode {
        ANY,
        ALL
    }

    public record Group(
            String translationKey,
            Mode mode,
            List<String> criteria
    ) {
    }
}