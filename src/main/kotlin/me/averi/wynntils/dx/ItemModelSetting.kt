package me.averi.wynntils.dx

import net.minecraft.world.entity.EquipmentSlot
import kotlin.reflect.KProperty

class ItemModelSetting(
  val modelRange: ClosedFloatingPointRange<Float>, value: Float? = null, val itemSlot: EquipmentSlot? = null
) : Setting<Float?>(value) {
  var modelOffset: Float?
    get() = get()
    set(value) = setValue(value)

  var modelValue: Float?
    get() = modelRange.start + (modelOffset ?: return null)
    set(value) {
      if (value == null) return setValue(null)
      modelOffset = value - modelRange.start
    }

  override fun getValue(thisRef: Configurable, property: KProperty<*>) = modelValue
}
