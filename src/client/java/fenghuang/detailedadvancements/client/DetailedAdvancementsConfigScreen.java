package fenghuang.detailedadvancements.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DetailedAdvancementsConfigScreen extends Screen {

    private final Screen parent;

    private DetailedAdvancementsConfig.CompletedConditionMode completedMode;

    private DetailedAdvancementsConfig.IncompleteConditionMode incompleteMode;

    public DetailedAdvancementsConfigScreen(Screen parent) {
        super(Component.translatable(
                "gui.detailedadvancements.config.title"
        ));

        this.parent = parent;

        this.completedMode =
                DetailedAdvancementsConfig.getCompletedConditions();

        this.incompleteMode =
                DetailedAdvancementsConfig.getIncompleteConditions();
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(360, this.width - 40);
        int buttonX = (this.width - buttonWidth) / 2;

        int firstY = this.height / 2 - 55;

        this.addRenderableWidget(
                CycleButton.<DetailedAdvancementsConfig.CompletedConditionMode>builder(
                                mode -> Component.translatable(mode.translationKey()),
                                this.completedMode
                        )
                        .withValues(
                                DetailedAdvancementsConfig.CompletedConditionMode.values()
                        )
                        .create(
                                buttonX,
                                firstY,
                                buttonWidth,
                                24,
                                Component.translatable(
                                        "gui.detailedadvancements.config.completed_conditions"
                                ),
                                (button, value) -> this.completedMode = value
                        )
        );

        this.addRenderableWidget(
                CycleButton.<DetailedAdvancementsConfig.IncompleteConditionMode>builder(
                                mode -> Component.translatable(mode.translationKey()),
                                this.incompleteMode
                        )
                        .withValues(
                                DetailedAdvancementsConfig.IncompleteConditionMode.values()
                        )
                        .create(
                                buttonX,
                                firstY + 45,
                                buttonWidth,
                                24,
                                Component.translatable(
                                        "gui.detailedadvancements.config.incomplete_conditions"
                                ),
                                (button, value) -> this.incompleteMode = value
                        )
        );

        int saveX = this.width / 2 - 105;
        int cancelX = this.width / 2 + 5;
        int bottomY = this.height - 40;

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "gui.detailedadvancements.config.save"
                                ),
                                button -> {
                                    DetailedAdvancementsConfig.save(
                                            this.completedMode,
                                            this.incompleteMode
                                    );

                                    this.minecraft.gui.setScreen(this.parent);
                                }
                        )
                        .bounds(saveX, bottomY, 100, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "gui.detailedadvancements.config.cancel"
                                ),
                                button -> this.minecraft.gui.setScreen(this.parent)
                        )
                        .bounds(cancelX, bottomY, 100, 20)
                        .build()
        );
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        graphics.centeredText(
                this.font,
                this.title,
                this.width / 2,
                25,
                0xFFFFFFFF
        );
    }
}