package me.averi.wynntils.mixin;

import com.wynntils.models.abilities.ShamanTotemModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

import static me.averi.wynntils.ConstantsKt.SHAMAN_TOTEM_CUSTOM_MODEL_DATA;

@Mixin(value = ShamanTotemModel.class, remap = false)
public class ShamanTotemModelMixin {
  @ModifyArg(method = "isShamanTotemItem", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;anyMatch(Ljava/util/function/Predicate;)Z"))
  private Predicate<Float> fox$useMyCustomModelData(Predicate<Float> predicate) {
    return (model) -> model == SHAMAN_TOTEM_CUSTOM_MODEL_DATA || model == SHAMAN_TOTEM_CUSTOM_MODEL_DATA + 1;
  }
}
