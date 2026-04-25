package me.averi.wynntils.mixin.minecraft;

import com.llamalad7.mixinextras.sugar.Local;
import me.averi.wynntils.events.EventBus;
import me.averi.wynntils.events.ItemModelResolveEvent;
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
    var event = new ItemModelResolveEvent(itemStack, itemOwner);
    EventBus.publish(event);
    return event.getReturnValue();
  }
}
