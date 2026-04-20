package me.averi.wynntils.utils

import com.wynntils.core.components.Services
import com.wynntils.models.gear.type.GearType
import me.averi.wynntils.mixin.accessors.GearTypeAccessor
import net.minecraft.client.gui.GuiGraphics
import org.joml.Matrix3x2fStack

val GearType.modelRange: ClosedFloatingPointRange<Float>
  get() {
    @Suppress("cast_never_succeeds")
    val pair = Services.CustomModel.getRange((this@modelRange as GearTypeAccessor).modelKey).get()
    return pair.a..pair.b
  }

val GuiGraphics.pose: Matrix3x2fStack
  get() = pose()

fun ClosedFloatingPointRange<Float>.toIntRange() = start.toInt()..endInclusive.toInt()
