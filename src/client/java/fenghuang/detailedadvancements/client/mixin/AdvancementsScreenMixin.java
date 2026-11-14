package fenghuang.detailedadvancements.client.mixin;

import fenghuang.detailedadvancements.client.AdvancementCriteriaScrollState;

import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementsScreenMixin {

    @Shadow
    private AdvancementTab selectedTab;

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