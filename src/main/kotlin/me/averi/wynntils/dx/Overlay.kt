package me.averi.wynntils.dx

import com.wynntils.core.consumers.overlays.Overlay
import com.wynntils.core.consumers.overlays.OverlayPosition
import com.wynntils.core.consumers.overlays.OverlaySize
import com.wynntils.utils.render.type.HorizontalAlignment
import com.wynntils.utils.render.type.VerticalAlignment
import me.averi.wynntils.mixin.accessors.OverlayInvoker
import net.minecraft.client.resources.language.I18n

abstract class Overlay(
  position: OverlayPosition,
  width: Float,
  height: Float,
  horizontalAlignmentOverride: HorizontalAlignment? = null,
  verticalAlignmentOverride: VerticalAlignment? = null
) : Overlay(
  position, OverlaySize(width, height), horizontalAlignmentOverride, verticalAlignmentOverride
), Configurable {
  override val settings: MutableList<Setting<*>> = ArrayList()

  @Suppress("unused")
  val isEnabled: Boolean
    @JvmName($$"fox$getIsEnabled") get() = userEnabled.get()

  override fun getTranslation(keySuffix: String, vararg parameters: Any): String {
    this as OverlayInvoker
    return I18n.get(
      "feature.foxaddons.${invokeGetTranslationFeatureKeyName()}.overlay.$translationKeyName.$keySuffix",
      parameters
    )
  }
}
