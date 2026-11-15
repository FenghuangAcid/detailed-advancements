
package fenghuang.detailedadvancements.client.mixin;

import fenghuang.detailedadvancements.client.AdvancementCriteriaScrollState;
import fenghuang.detailedadvancements.client.AdvancementGuideScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.input.MouseButtonEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementsScreenMixin {

    @Shadow
    private AdvancementTab selectedTab;

    // 右鍵打開教學
    @Inject(
            method = "mouseClicked",
            at = @At("HEAD"),
            cancellable = true
    )
    private void detailedadvancements$openGuide(
            MouseButtonEvent event,
            boolean doubleClick,
            CallbackInfoReturnable<Boolean> callbackInfo
    ) {

        if (event.button() != 3) {
            return;
        }

        if (this.selectedTab == null) {
            return;
        }

        AdvancementWidget hovered =
                ((AdvancementTabAccessor) (Object) this.selectedTab)
                        .detailedadvancements$getHovered();

        if (hovered == null) {
            return;
        }

        Minecraft.getInstance().gui.setScreen(
                new AdvancementGuideScreen(
                        (Screen) (Object) this,
                        hovered.getDisplay().title()
                )
        );

        callbackInfo.setReturnValue(true);
    }


    // 保留原本的進度條件滑鼠滾輪功能
    @Inject(
            method = "mouseScrolled",
            at = @At("HEAD"),
            cancellable = true
    )
    private void detailedadvancements$scrollCriteria(
            double x,
            double y,
            double scrollX,
            double scrollY,
            CallbackInfoReturnable<Boolean> callbackInfo
    ) {
        if (this.selectedTab == null) {
            return;
        }

        AdvancementWidget hovered =
                ((AdvancementTabAccessor) (Object) this.selectedTab)
                        .detailedadvancements$getHovered();

        if (hovered != null
                && AdvancementCriteriaScrollState.scroll(hovered, scrollY)) {

            callbackInfo.setReturnValue(true);
        }
    }
}
