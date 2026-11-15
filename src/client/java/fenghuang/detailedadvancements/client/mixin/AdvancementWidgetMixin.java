package fenghuang.detailedadvancements.client.mixin;

import fenghuang.detailedadvancements.client.AdvancementCriteriaReader;
import fenghuang.detailedadvancements.client.AdvancementCriteriaReader.CriterionStatus;
import fenghuang.detailedadvancements.client.AdvancementCriteriaGroupConfig;
import fenghuang.detailedadvancements.client.AdvancementCriteriaScrollState;
import fenghuang.detailedadvancements.client.DetailedAdvancementsConfig;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText;

import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

@Mixin(AdvancementWidget.class)
public abstract class AdvancementWidgetMixin {

    @Shadow
    @Final
    @Mutable
    private List<FormattedCharSequence> description;

    @Shadow
    @Final
    private List<FormattedCharSequence> titleLines;

    @Invoker("findOptimalLines")
    protected abstract List<FormattedText> detailedadvancements$findOptimalLines(
            Component input,
            int preferredWidth
    );

    @Invoker("getMaxProgressWidth")
    protected abstract int detailedadvancements$getMaxProgressWidth();

    @Shadow
    private AdvancementProgress progress;

    @Shadow
    public abstract AdvancementHolder getAdvancement();

    @Unique
    private List<FormattedCharSequence> detailedadvancements$originalDescription;

    @Unique
    private static MutableComponent detailedadvancements$buildCustomGroupLine(
            String translationKey,
            List<Component> groups
    ) {
        MutableComponent line = Component.translatable(translationKey);

        for (int i = 0; i < groups.size(); i++) {
            if (i > 0) {
                line.append(
                        Component.translatable(
                                "gui.detailedadvancements.criteria_separator"
                        )
                );
            }

            line.append(groups.get(i));
        }

        return line;
    }

    @Unique
    private void detailedadvancements$appendLine(
            ArrayList<FormattedCharSequence> description,
            MutableComponent line,
            int preferredWidth
    ) {
        description.addAll(
                Language.getInstance().getVisualOrder(
                        this.detailedadvancements$findOptimalLines(
                                line,
                                preferredWidth
                        )
                )
        );
    }

    @Inject(method = "extractHover", at = @At("HEAD"))
    private void detailedadvancements$appendCriteria(
            GuiGraphicsExtractor graphics,
            int xo,
            int yo,
            float fade,
            int screenxo,
            int screenyo,
            int screenWidth,
            CallbackInfo callbackInfo
    ) {
        if (this.progress == null) {
            return;
        }

        AdvancementHolder advancement = this.getAdvancement();

        ArrayList<CriterionStatus> criteria =
                AdvancementCriteriaReader.read(advancement, this.progress);

        if (criteria.isEmpty()) {
            return;
        }

        // 先檢查這項進度是否有自訂條件分組
        List<AdvancementCriteriaGroupConfig.Group> customGroups =
                AdvancementCriteriaGroupConfig.getGroups(advancement.id().toString());

        if (!customGroups.isEmpty()) {

            HashSet<String> completedCriterionNames = new HashSet<>();

            for (CriterionStatus status : criteria) {
                if (status.completed()) {
                    completedCriterionNames.add(status.criterion());
                }
            }

            ArrayList<Component> completedGroups = new ArrayList<>();
            ArrayList<Component> incompleteGroups = new ArrayList<>();



            for (AdvancementCriteriaGroupConfig.Group group : customGroups) {

                boolean groupCompleted = switch (group.mode()) {
                    case ANY -> group.criteria().stream()
                            .anyMatch(completedCriterionNames::contains);

                    case ALL -> group.criteria().stream()
                            .allMatch(completedCriterionNames::contains);
                };

                Component groupName = Component.translatable(
                        group.translationKey()
                );

                if (groupCompleted) {
                    completedGroups.add(groupName);
                } else {
                    incompleteGroups.add(groupName);

                }
            }



            this.detailedadvancements$originalDescription = this.description;

            Font font = ((MinecraftAccessor) (Object) Minecraft.getInstance())
                    .detailedadvancements$getFont();

            int titleWidth = Math.max(
                    this.titleLines.stream()
                            .mapToInt(font::width)
                            .max()
                            .orElse(0),
                    80
            );

            int criteriaPreferredWidth =
                    29 + titleWidth + this.detailedadvancements$getMaxProgressWidth();

            ArrayList<FormattedCharSequence> extendedDescription =
                    new ArrayList<>(this.description);


            if (DetailedAdvancementsConfig.shouldShowCompleted(this.progress.isDone())
                    && !completedGroups.isEmpty()) {
                MutableComponent completedLine =
                        detailedadvancements$buildCustomGroupLine(
                                "gui.detailedadvancements.criteria_completed",
                                completedGroups
                        );

                this.detailedadvancements$appendLine(
                        extendedDescription,
                        completedLine,
                        criteriaPreferredWidth
                );
            }


            if (DetailedAdvancementsConfig.shouldShowIncomplete()
                    && !incompleteGroups.isEmpty()) {
                MutableComponent incompleteLine =
                        detailedadvancements$buildCustomGroupLine(
                                "gui.detailedadvancements.criteria_incomplete",
                                incompleteGroups
                        ).withStyle(ChatFormatting.RED);

                this.detailedadvancements$appendLine(
                        extendedDescription,
                        incompleteLine,
                        criteriaPreferredWidth
                );
            }

            this.description = AdvancementCriteriaScrollState.getVisibleLines(
                    (AdvancementWidget) (Object) this,
                    extendedDescription
            );
            return;
        }

        // 沒有自訂分組時，沿用原本的判斷規則
        List<List<String>> requirement_groups =
                advancement.value().requirements().requirements();

        if (requirement_groups.size() <= 1) {
            return;
        }

        this.detailedadvancements$originalDescription = this.description;

        Font font = ((MinecraftAccessor) (Object) Minecraft.getInstance())
                .detailedadvancements$getFont();

        int titleWidth = Math.max(
                this.titleLines.stream()
                        .mapToInt(font::width)
                        .max()
                        .orElse(0),
                80
        );

        int criteriaPreferredWidth =
                29 + titleWidth + this.detailedadvancements$getMaxProgressWidth();

        ArrayList<String> completed = new ArrayList<>();
        ArrayList<String> incomplete = new ArrayList<>();

        for (CriterionStatus status : criteria) {
            if (status.completed()) {
                completed.add(status.criterion());
            } else {
                incomplete.add(status.criterion());
            }
        }

        ArrayList<FormattedCharSequence> extendedDescription =
                new ArrayList<>(this.description);

        if (DetailedAdvancementsConfig.shouldShowCompleted(this.progress.isDone())
                && !completed.isEmpty()) {
            MutableComponent completedLine =
                    detailedadvancements$buildGroupLine(
                            advancement,
                            "gui.detailedadvancements.criteria_completed",
                            completed
                    );

            this.detailedadvancements$appendLine(
                    extendedDescription,
                    completedLine,
                    criteriaPreferredWidth
            );
        }

        if (DetailedAdvancementsConfig.shouldShowIncomplete()
                && !incomplete.isEmpty()) {
            MutableComponent incompleteLine =
                    detailedadvancements$buildGroupLine(
                            advancement,
                            "gui.detailedadvancements.criteria_incomplete",
                            incomplete
                    ).withStyle(ChatFormatting.RED);

            this.detailedadvancements$appendLine(
                    extendedDescription,
                    incompleteLine,
                    criteriaPreferredWidth
            );
        }

        this.description = AdvancementCriteriaScrollState.getVisibleLines(
                (AdvancementWidget) (Object) this,
                extendedDescription
        );
    }

    @Inject(method = "extractHover", at = @At("RETURN"))
    private void detailedadvancements$restoreDescription(
            GuiGraphicsExtractor graphics,
            int xo,
            int yo,
            float fade,
            int screenxo,
            int screenyo,
            int screenWidth,
            CallbackInfo callbackInfo
    ) {
        if (this.detailedadvancements$originalDescription != null) {
            this.description = this.detailedadvancements$originalDescription;
            this.detailedadvancements$originalDescription = null;
        }
    }

    @Unique
    private static MutableComponent detailedadvancements$buildGroupLine(
            AdvancementHolder advancement,
            String translationKey,
            ArrayList<String> criteria
    ){
        MutableComponent line = Component.translatable(translationKey);



        for (int i = 0; i < criteria.size(); i++) {
            if (i > 0) {
                line.append(Component.translatable(
                        "gui.detailedadvancements.criteria_separator"));
            }

            line.append(
                    detailedadvancements$criterionName(
                            advancement,
                            criteria.get(i)
                    )
            );
        }

        return line;
    }

    @Unique
    private static MutableComponent detailedadvancements$criterionName(
            AdvancementHolder advancement,
            String criterion
    ) {
        Identifier identifier = Identifier.tryParse(criterion);

        if (identifier == null) {
            return Component.literal(criterion);
        }

        String advancementId = advancement.id().toString();
        String translationKey;

        if (advancementId.equals("minecraft:husbandry/bred_all_animals")
                || advancementId.equals("minecraft:adventure/kill_all_mobs")) {
            translationKey = "entity."
                    + identifier.getNamespace()
                    + "."
                    + identifier.getPath();
        } else if (
                advancementId.equals("minecraft:adventure/adventuring_time")
                        || advancementId.equals("minecraft:nether/explore_nether")
        ) {
            translationKey = "biome."
                    + identifier.getNamespace()
                    + "."
                    + identifier.getPath();
        } else if (advancementId.equals("minecraft:husbandry/complete_catalogue")) {
            translationKey = "detailedadvancements.criterion.cat."
                    + identifier.getPath();
        }
        else if (advancementId.equals("minecraft:husbandry/balanced_diet")) {
            translationKey = "item."
                    + identifier.getNamespace()
                    + "."
                    + identifier.getPath();
        }
        else if (advancementId.equals("minecraft:husbandry/leash_all_frog_variants")) {
            translationKey = "detailedadvancements.criterion.frog."
                    + identifier.getPath();
        }
        else if (advancementId.equals("minecraft:husbandry/whole_pack")) {
            translationKey = "detailedadvancements.criterion.wolf."
                    + identifier.getPath();
        }
        else if (advancementId.equals("minecraft:adventure/trim_with_all_exclusive_armor_patterns")) {
            String templatePath = identifier.getPath();
            String suffix = "_smithing_trim";

            if (!identifier.getNamespace().equals("armor_trimmed_minecraft")
                    || !templatePath.endsWith(suffix)) {
                return Component.literal(criterion);
            }

            templatePath = templatePath.substring(
                    0,
                    templatePath.length() - suffix.length()
            );

            translationKey = "item.minecraft." + templatePath;
        }else {
            return Component.literal(criterion);
        }

        if (Language.getInstance().has(translationKey)) {
            return Component.translatable(translationKey);
        }

        return Component.literal(criterion);
    }
}