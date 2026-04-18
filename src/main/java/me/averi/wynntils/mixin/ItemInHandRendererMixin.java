package me.averi.wynntils.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.averi.wynntils.features.SkinChanger;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
  @Shadow
  private ItemStack mainHandItem; // item to be rendered

  @Unique
  private ItemStack actualMainHandItem = ItemStack.EMPTY; // actual held item

  @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;mainHandItem:Lnet/minecraft/world/item/ItemStack;", opcode = Opcodes.GETFIELD))
  private ItemStack fox$getMHI(ItemInHandRenderer instance) {
    return actualMainHandItem;
  }

  @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;mainHandItem:Lnet/minecraft/world/item/ItemStack;", opcode = Opcodes.PUTFIELD))
  private void fox$putMHI(ItemInHandRenderer instance, ItemStack value, @Local LocalPlayer localPlayer) {
    actualMainHandItem = value;
    mainHandItem = SkinChanger.INSTANCE.apply(value, localPlayer);
  }
}
