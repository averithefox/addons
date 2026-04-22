package me.averi.wynntils.utils

import com.wynntils.core.components.Services
import com.wynntils.models.gear.type.GearType
import me.averi.wynntils.mixin.wynntils.accessors.GearTypeAccessor
import net.minecraft.client.gui.GuiGraphics
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
