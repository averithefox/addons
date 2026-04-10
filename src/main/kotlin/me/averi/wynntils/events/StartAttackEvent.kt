package me.averi.wynntils.events

data class StartAttackEvent @JvmOverloads constructor(override var isCancelled: Boolean = false) : CancellableEvent
