package sa.factory.masarrtknavigation.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MasarRtkDao {
    @Query("SELECT * FROM navigation_items_cache ORDER BY id ASC")
    fun observeAllItems(): Flow<List<MasarRtkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<MasarRtkEntity>)

    @Query("DELETE FROM navigation_items_cache")
    suspend fun clearAll()
}
