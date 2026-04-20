package me.averi.wynntils.features

import com.wynntils.core.consumers.features.ProfileDefault
import com.wynntils.models.gear.type.GearType
import me.averi.wynntils.dx.Feature
import me.averi.wynntils.dx.ItemModelSetting
import me.averi.wynntils.utils.mc
import me.averi.wynntils.utils.modelRange
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.ItemOwner
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomModelData

object SkinChanger : Feature(ProfileDefault.DISABLED) {
  val spearModel by ItemModelSetting(GearType.SPEAR.modelRange)
  val wandModel by ItemModelSetting(GearType.WAND.modelRange)
  val daggerModel by ItemModelSetting(GearType.DAGGER.modelRange)
  val bowModel by ItemModelSetting(GearType.BOW.modelRange)
  val relikModel by ItemModelSetting(GearType.RELIK.modelRange)

  private var mainHandItem = ItemStack(Items.POTION)

  fun apply(itemStack: ItemStack, owner: ItemOwner?): ItemStack {
    if (!isEnabled) return itemStack
    val localPlayer = mc.player ?: return itemStack
    if (!ItemStack.matches(itemStack, localPlayer.mainHandItem)) return itemStack

    val model = when (itemStack.get(DataComponents.CUSTOM_MODEL_DATA)?.getFloat(0) ?: return itemStack) {
      in GearType.SPEAR.modelRange -> spearModel
      in GearType.WAND.modelRange -> wandModel
      in GearType.DAGGER.modelRange -> daggerModel
      in GearType.BOW.modelRange -> bowModel
      in GearType.RELIK.modelRange -> relikModel
      else -> return itemStack
    } ?: return itemStack

    mainHandItem.set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(listOf(model), listOf(), listOf(), listOf()))
    return mainHandItem
  }
}
