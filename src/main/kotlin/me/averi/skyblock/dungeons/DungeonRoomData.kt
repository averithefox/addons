package me.averi.skyblock.dungeons

import com.google.gson.JsonParser
import net.minecraft.core.BlockPos
import java.io.InputStreamReader

enum class SecretType(
  val key: String,
  val red: Float,
  val green: Float,
  val blue: Float,
  val alpha: Float = 0.9f,
) {
  CHEST("chest", 0f, 1f, 0f), ITEM("item", 0f, 0.35f, 1f), WITHER("wither", 1f, 0f, 1f), BAT(
    "bat", 0f, 1f, 0f
  ),
  REDSTONE_KEY("redstone_key", 1f, 0f, 0f), ;

  companion object {
    private val byKey = entries.associateBy(SecretType::key)

    fun fromKey(key: String): SecretType? = byKey[key]
  }
}

data class DungeonRoomData(
  val id: List<String>,
  val name: String,
  val shape: String,
  val secrets: Int,
  val cores: List<Int>,
  val secretCoords: Map<SecretType, List<BlockPos>>,
)

object DungeonRoomRepository {
  private val roomsById: Map<String, DungeonRoomData> = loadRooms().flatMap { room ->
    room.id.map { id -> id to room }
  }.toMap()

  private val roomsByCore: Map<Int, DungeonRoomData> = loadRooms().flatMap { room ->
    room.cores.map { core -> core to room }
  }.toMap()

  fun getRoomById(id: String): DungeonRoomData? = roomsById[id]

  fun getRoomByCore(core: Int): DungeonRoomData? = roomsByCore[core]

  private fun loadRooms(): List<DungeonRoomData> {
    val resourcePath = "data/fox-addons/dungeons/roomdata.json"
    val stream =
      javaClass.classLoader.getResourceAsStream(resourcePath) ?: error("Missing dungeon room resource: $resourcePath")

    InputStreamReader(stream, Charsets.UTF_8).use { reader ->
      val root = JsonParser.parseReader(reader).asJsonArray
      return root.map { element ->
        val obj = element.asJsonObject
        val secretsByType = buildMap {
          val secretObj = obj.getAsJsonObject("secret_coords")
          for ((key, value) in secretObj.entrySet()) {
            val type = SecretType.fromKey(key) ?: continue
            put(
              type,
              value.asJsonArray.map { coord ->
                val arr = coord.asJsonArray
                BlockPos(arr[0].asInt, arr[1].asInt, arr[2].asInt)
              },
            )
          }
        }

        DungeonRoomData(
          id = obj.getAsJsonArray("id")?.map { it.asString } ?: emptyList(),
          name = obj.get("name").asString,
          shape = obj.get("shape").asString,
          secrets = obj.get("secrets").asInt,
          cores = obj.getAsJsonArray("cores")?.map { it.asInt } ?: emptyList(),
          secretCoords = secretsByType,
        )
      }
    }
  }
}
