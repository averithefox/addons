package me.averi.wynntils.features

import com.wynntils.core.consumers.features.ProfileDefault
import me.averi.wynntils.dx.Feature
import me.averi.wynntils.dx.Setting

object CameraTweaks : Feature(ProfileDefault.ENABLED) {
  val noCameraClip by Setting(true)
  val renderCrosshair by Setting(true)
}
