package fenghuang.detailedadvancements.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;

import fenghuang.detailedadvancements.client.mixin.AdvancementsScreenAccessor;

public class AdvancementGuideScreen extends Screen {

    private final Screen parent;

    public AdvancementGuideScreen(
            Screen parent,
            Component advancementTitle
    ) {
        super(
                Component.translatable(
                        "gui.detailedadvancements.guide.title",
                        advancementTitle
                )
        );

        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 100;
        int buttonX = (this.width - buttonWidth) / 2;
        int buttonY = this.height - 40;

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "gui.detailedadvancements.guide.back"
                                ),
                                button -> this.onClose()
                        )
                        .bounds(buttonX, buttonY, buttonWidth, 20)
                        .build()
        );
    }

    @Override
    public void onClose() {
        System.out.println(
                "[DetailedAdvancements] Guide onClose"
        );

        this.minecraft.gui.setScreen(this.parent);

        if (this.parent instanceof AdvancementsScreen screen) {

            ((AdvancementsScreenAccessor) (Object) screen)
                    .detailedadvancements$getAdvancements()
                    .setListener(screen);

            System.out.println(
                    "[DetailedAdvancements] Listener restored after Guide"
            );
        }
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
                35,
                0xFFFFFFFF
        );

        graphics.centeredText(
                this.font,
                Component.translatable(
                        "gui.detailedadvancements.guide.coming_soon"
                ),
                this.width / 2,
                this.height / 2,
                0xFFFFFFFF
        );
    }
}
