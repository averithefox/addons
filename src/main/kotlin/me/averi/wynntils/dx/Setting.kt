package me.averi.wynntils.dx

import com.wynntils.core.WynntilsMod
import com.wynntils.core.components.Managers
import com.wynntils.core.persisted.config.Config
import me.averi.wynntils.mixin.wynntils.accessors.ConfigAccessor
import me.averi.wynntils.mixin.wynntils.accessors.JsonTypeWrapperInvoker
import me.averi.wynntils.mixin.wynntils.accessors.PersistedManagerInvoker
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaType

open class Setting<T>(val value: T) : Config<T>(value), ReadWriteProperty<Configurable, T>,
  PropertyDelegateProvider<Configurable, ReadWriteProperty<Configurable, T>> {
  private lateinit var owner: Configurable
  private lateinit var property: KProperty<*>

  override fun getValue(thisRef: Configurable, property: KProperty<*>): T = get()

  override fun setValue(
    thisRef: Configurable, property: KProperty<*>, value: T
  ) = setValue(value)

  override fun provideDelegate(
    thisRef: Configurable, property: KProperty<*>
  ): ReadWriteProperty<Configurable, T> {
    this.owner = thisRef
    this.property = property
    thisRef.registerSetting(this)
    return this
  }

  private val allowNull
    get() = property.returnType.isMarkedNullable

  override fun getJsonName(): String {
    @Suppress("cast_never_succeeds")
    val persistedManager = Managers.Persisted as PersistedManagerInvoker
    return "${persistedManager.invokeGetPrefix(owner)}${owner.jsonName}.$fieldName"
  }

  override fun getType(): Type {
    val type = property.returnType.javaType
    return if (type is ParameterizedType) JsonTypeWrapperInvoker.invokeConstructor(type) else type
  }

  override fun setValue(value: T) {
    if (value == null && !allowNull) {
      WynntilsMod.warn("Trying to set null to config $jsonName. Will be replaced by default.")
      reset()
    } else {
      setWithoutTouch(value)
      owner.updateConfigOption(this)
      (this as ConfigAccessor).setUserEdited(true)
    }
  }

  override fun getFieldName() = property.name

  override fun isEnum(): Boolean = type is Class<*> && (type as Class<*>).isEnum

  override fun getDefaultValue() = value

  private fun getI18n(suffix: String): String = owner.getTranslation("$fieldName.$suffix")

  override fun getDisplayName() = getI18n("name")
  override fun getDescription() = getI18n("description")
}
