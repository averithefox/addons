package me.averi.skyblock.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin {
  @Shadow
  private SignText text;

  @Shadow
  protected abstract void onDone();

  @Unique
  private boolean isInput;

  @Inject(method = "<init>(Lnet/minecraft/world/level/block/entity/SignBlockEntity;ZZLnet/minecraft/network/chat/Component;)V", at = @At("TAIL"))
  private void init(SignBlockEntity signBlockEntity, boolean bl, boolean bl2, Component component, CallbackInfo ci) {
    isInput = Arrays.stream(text.getMessages(false)).anyMatch(msg -> msg.getString().equals("^^^^^^^^^^^^^^^"));
  }

  @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
  private void keyPressed(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
    if (isInput && keyEvent.isConfirmation()) {
      onDone();
      cir.setReturnValue(true);
    }
  }
}
