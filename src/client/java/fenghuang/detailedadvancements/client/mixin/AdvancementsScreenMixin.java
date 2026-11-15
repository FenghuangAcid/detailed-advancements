
package fenghuang.detailedadvancements.client.mixin;

import fenghuang.detailedadvancements.client.AdvancementCriteriaScrollState;
import fenghuang.detailedadvancements.client.AdvancementGuideScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.advancements.AdvancementHolder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementsScreenMixin {

    @Shadow
    private int leftPos;

    @Shadow
    private int topPos;

    @Shadow
    @Final
    private Map<AdvancementHolder, AdvancementTab> tabs;

    @Shadow
    private AdvancementTab selectedTab;

    @Shadow
    @Final
    private ClientAdvancements advancements;


    // 診斷：確認進度畫面是否開始初始化
    @Inject(
            method = "init",
            at = @At("HEAD")
    )
    private void detailedadvancements$debugInit(
            CallbackInfo callbackInfo
    ) {
        System.out.println(
                "[DetailedAdvancements] AdvancementsScreen init HEAD"
        );
    }


    // 返回進度畫面時，重新設定 Advancement Listener
    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    private void detailedadvancements$restoreAdvancementListener(
            CallbackInfo callbackInfo
    ) {
        System.out.println(
                "[DetailedAdvancements] Restoring advancement listener"
        );

        this.advancements.setListener(
                (AdvancementsScreen) (Object) this
        );
    }


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
        System.out.println(
                "AdvancementsScreen mouseClicked: "
                        + event.button()
        );

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


    // 診斷：觀察左鍵點擊結果與分類標籤命中狀況
    @Inject(
            method = "mouseClicked",
            at = @At("TAIL")
    )
    private void detailedadvancements$debugMouseClicked(
            MouseButtonEvent event,
            boolean doubleClick,
            CallbackInfoReturnable<Boolean> callbackInfo
    ) {
        if (event.button() == 1) {

            System.out.println(
                    "Mouse result: " + callbackInfo.getReturnValue()
            );

            System.out.println(
                    "Mouse position: "
                            + event.x() + ", " + event.y()
            );

            System.out.println(
                    "Screen position: "
                            + this.leftPos + ", " + this.topPos
            );

            System.out.println(
                    "Tab count: " + this.tabs.size()
            );

            for (AdvancementTab tab : this.tabs.values()) {
                System.out.println(
                        "Tab: " + tab.getTitle().getString()
                                + " | Hover: "
                                + tab.isMouseOver(
                                this.leftPos,
                                this.topPos,
                                event.x(),
                                event.y()
                        )
                );
            }
        }
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
