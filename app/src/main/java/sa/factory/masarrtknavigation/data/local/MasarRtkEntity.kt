package sa.factory.masarrtknavigation.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import sa.factory.masarrtknavigation.model.MasarRtkItem

@Entity(tableName = "navigation_items_cache")
data class MasarRtkEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val priceSar: Double,
    val badge: String,
    val category: String,
    val status: String
) {
    fun toDomain(): MasarRtkItem = MasarRtkItem(
        id = id,
        title = title,
        description = description,
        priceSar = priceSar,
        badge = badge,
        category = category,
        status = status
    )
}
