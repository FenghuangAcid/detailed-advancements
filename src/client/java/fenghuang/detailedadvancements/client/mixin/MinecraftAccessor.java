package fenghuang.detailedadvancements.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {

    @Accessor("font")
    Font detailedadvancements$getFont();
}