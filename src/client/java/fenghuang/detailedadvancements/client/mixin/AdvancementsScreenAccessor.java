
package fenghuang.detailedadvancements.client.mixin;

import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AdvancementsScreen.class)
public interface AdvancementsScreenAccessor {

    @Accessor("advancements")
    ClientAdvancements detailedadvancements$getAdvancements();
}
