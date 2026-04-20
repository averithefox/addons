package me.averi.wynntils.features

import com.wynntils.core.consumers.features.ProfileDefault
import com.wynntils.models.gear.type.GearType
import me.averi.wynntils.dx.Feature
import me.averi.wynntils.dx.ItemModelSetting
import me.averi.wynntils.utils.mc
import me.averi.wynntils.utils.modelRange
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.ItemOwner
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomModelData

object SkinChanger : Feature(ProfileDefault.DISABLED) {
  val spearModel by ItemModelSetting(GearType.SPEAR.modelRange, itemSlot = EquipmentSlot.MAINHAND)
  val wandModel by ItemModelSetting(GearType.WAND.modelRange, itemSlot = EquipmentSlot.MAINHAND)
  val daggerModel by ItemModelSetting(GearType.DAGGER.modelRange, itemSlot = EquipmentSlot.MAINHAND)
  val bowModel by ItemModelSetting(GearType.BOW.modelRange, itemSlot = EquipmentSlot.MAINHAND)
  val relikModel by ItemModelSetting(GearType.RELIK.modelRange, itemSlot = EquipmentSlot.MAINHAND)
  val helmetModel by ItemModelSetting(GearType.HELMET.modelRange, itemSlot = EquipmentSlot.HEAD)

  fun apply(itemStack: ItemStack, owner: ItemOwner?): ItemStack {
    if (!isEnabled) return itemStack
    val localPlayer = mc.player ?: return itemStack
    val isHelmet = ItemStack.matches(itemStack, localPlayer.getItemBySlot(EquipmentSlot.HEAD))
    if (!ItemStack.matches(itemStack, localPlayer.mainHandItem) && !isHelmet) return itemStack

    val model = when (itemStack.get(DataComponents.CUSTOM_MODEL_DATA)?.getFloat(0) ?: return itemStack) {
      in GearType.SPEAR.modelRange -> spearModel
      in GearType.WAND.modelRange -> wandModel
      in GearType.DAGGER.modelRange -> daggerModel
      in GearType.BOW.modelRange -> bowModel
      in GearType.RELIK.modelRange -> relikModel
      in GearType.HELMET.modelRange -> helmetModel
      else -> return itemStack
    } ?: return itemStack

    val item = ItemStack(Items.POTION)
    item.set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(listOf(model), listOf(), listOf(), listOf()))
    return item
  }
}
