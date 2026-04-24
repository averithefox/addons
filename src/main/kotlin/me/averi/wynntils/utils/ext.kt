package me.averi.wynntils.utils

import com.wynntils.core.components.Services
import com.wynntils.models.gear.type.GearType
import me.averi.wynntils.mixin.wynntils.accessors.GearTypeAccessor
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomModelData
import org.joml.Matrix3x2fStack

val GearType.modelRange: ClosedFloatingPointRange<Float>
  get() {
    @Suppress("cast_never_succeeds")
    this as GearTypeAccessor
    val pair = Services.CustomModel.getRange(this.skinModelKey ?: this.modelKey).get()
    return pair.a..pair.b
  }

val GuiGraphics.pose: Matrix3x2fStack
  get() = pose()

fun ClosedFloatingPointRange<Float>.toIntRange() = start.toInt()..endInclusive.toInt()

var ItemStack.customModel
  get() = get(DataComponents.CUSTOM_MODEL_DATA)?.getFloat(0)
  set(value) {
    val cmd = get(DataComponents.CUSTOM_MODEL_DATA)
    if (cmd != null) {
      if (value != null) cmd.floats[0] = value
      else cmd.floats.clear()
      return
    }
    if (value == null) return
    set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(mutableListOf(value), listOf(), listOf(), listOf()))
  }

operator fun Float?.plus(b: Float): Float? {
  if (this == null) return null
  return this + b
}
