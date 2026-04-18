package me.averi.wynntils.features

import com.wynntils.core.components.Services
import com.wynntils.core.consumers.features.ProfileDefault
import com.wynntils.models.gear.type.GearType
import me.averi.wynntils.dx.Feature
import me.averi.wynntils.dx.Setting
import me.averi.wynntils.mixin.accessors.GearTypeAccessor
import me.averi.wynntils.utils.mc
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.ItemOwner
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomModelData

object SkinChanger : Feature(ProfileDefault.DISABLED) {
  val changeSpearSkin by Setting(false)
  val spearModelOffset by Setting(0)
  val changeWandSkin by Setting(false)
  val wandModelOffset by Setting(0)
  val changeDaggerSkin by Setting(false)
  val daggerModelOffset by Setting(0)
  val changeBowSkin by Setting(false)
  val bowModelOffset by Setting(0)
  val changeRelikSkin by Setting(false)
  val relikModelOffset by Setting(0)

  private var mainHandSkin = ItemStack(Items.POTION)

  private val GearType.modelRange: ClosedFloatingPointRange<Float>
    get() {
      val pair = Services.CustomModel.getRange((this@modelRange as GearTypeAccessor).modelKey).get()
      return pair.a..pair.b
    }

  fun apply(itemStack: ItemStack, owner: ItemOwner?): ItemStack {
    if (!isEnabled) return itemStack
    val localPlayer = mc.player ?: return itemStack
    if (!ItemStack.matches(itemStack, localPlayer.mainHandItem)) return itemStack

    val gearType = when (itemStack.get(DataComponents.CUSTOM_MODEL_DATA)?.getFloat(0) ?: return itemStack) {
      in GearType.SPEAR.modelRange -> GearType.SPEAR
      in GearType.WAND.modelRange -> GearType.WAND
      in GearType.DAGGER.modelRange -> GearType.DAGGER
      in GearType.BOW.modelRange -> GearType.BOW
      in GearType.RELIK.modelRange -> GearType.RELIK
      else -> return itemStack
    }

    val shouldChange = when (gearType) {
      GearType.SPEAR -> changeSpearSkin
      GearType.WAND -> changeWandSkin
      GearType.DAGGER -> changeDaggerSkin
      GearType.BOW -> changeBowSkin
      GearType.RELIK -> changeRelikSkin
      else -> return itemStack
    }

    if (!shouldChange) return itemStack

    val offset = when (gearType) {
      GearType.SPEAR -> spearModelOffset
      GearType.WAND -> wandModelOffset
      GearType.DAGGER -> daggerModelOffset
      GearType.BOW -> bowModelOffset
      GearType.RELIK -> relikModelOffset
    }

    val model = gearType.modelRange.start + offset

    mainHandSkin.set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(listOf(model), listOf(), listOf(), listOf()))
    return mainHandSkin
  }
}