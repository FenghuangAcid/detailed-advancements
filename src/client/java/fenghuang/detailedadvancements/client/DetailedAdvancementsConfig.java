package fenghuang.detailedadvancements.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DetailedAdvancementsConfig {

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("detailed-advancements.json");

    private static CompletedConditionMode completedConditions =
            CompletedConditionMode.ALWAYS;

    private static IncompleteConditionMode incompleteConditions =
            IncompleteConditionMode.ALWAYS;

    private DetailedAdvancementsConfig() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            saveFile();
            return;
        }

        try (Reader reader = Files.newBufferedReader(
                CONFIG_PATH,
                StandardCharsets.UTF_8
        )) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);

            if (data != null) {
                if (data.completed_conditions != null) {
                    completedConditions = data.completed_conditions;
                }

                if (data.incomplete_conditions != null) {
                    incompleteConditions = data.incomplete_conditions;
                }
            }

        } catch (Exception exception) {
            System.err.println(
                    "[DetailedAdvancements] Failed to load config."
            );

            exception.printStackTrace();

            completedConditions = CompletedConditionMode.ALWAYS;
            incompleteConditions = IncompleteConditionMode.ALWAYS;

            saveFile();
        }
    }

    public static synchronized void save(
            CompletedConditionMode completed,
            IncompleteConditionMode incomplete
    ) {
        completedConditions = completed;
        incompleteConditions = incomplete;

        saveFile();
    }

    public static CompletedConditionMode getCompletedConditions() {
        return completedConditions;
    }

    public static IncompleteConditionMode getIncompleteConditions() {
        return incompleteConditions;
    }

    public static boolean shouldShowCompleted(boolean advancementCompleted) {
        return switch (completedConditions) {
            case ALWAYS -> true;
            case AFTER_ADVANCEMENT_COMPLETED -> !advancementCompleted;
            case NEVER -> false;
        };
    }

    public static boolean shouldShowIncomplete() {
        return incompleteConditions == IncompleteConditionMode.ALWAYS;
    }

    private static void saveFile() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            ConfigData data = new ConfigData();

            data.completed_conditions = completedConditions;
            data.incomplete_conditions = incompleteConditions;

            Files.writeString(
                    CONFIG_PATH,
                    GSON.toJson(data),
                    StandardCharsets.UTF_8
            );

        } catch (IOException exception) {
            System.err.println(
                    "[DetailedAdvancements] Failed to save config."
            );

            exception.printStackTrace();
        }
    }

    public enum CompletedConditionMode {

        ALWAYS("gui.detailedadvancements.config.display.always"),

        AFTER_ADVANCEMENT_COMPLETED(
                "gui.detailedadvancements.config.display.after_completed"
        ),

        NEVER("gui.detailedadvancements.config.display.never");

        private final String translationKey;

        CompletedConditionMode(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }
    }

    public enum IncompleteConditionMode {

        ALWAYS("gui.detailedadvancements.config.display.always"),

        NEVER("gui.detailedadvancements.config.display.never");

        private final String translationKey;

        IncompleteConditionMode(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }
    }

    private static final class ConfigData {

        private CompletedConditionMode completed_conditions =
                CompletedConditionMode.ALWAYS;

        private IncompleteConditionMode incomplete_conditions =
                IncompleteConditionMode.ALWAYS;
    }
}
