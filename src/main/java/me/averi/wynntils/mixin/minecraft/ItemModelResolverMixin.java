package me.averi.wynntils.mixin.minecraft;

import com.llamalad7.mixinextras.sugar.Local;
import me.averi.wynntils.features.SkinChanger;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {
  @ModifyVariable(method = "appendItemLayers", at = @At("HEAD"), argsOnly = true)
  private ItemStack fox$modifyItemStack(ItemStack itemStack, @Local(argsOnly = true) @Nullable ItemOwner itemOwner) {
    return SkinChanger.INSTANCE.apply(itemStack, itemOwner);
  }
}
