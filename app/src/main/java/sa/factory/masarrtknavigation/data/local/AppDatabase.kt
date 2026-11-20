package sa.factory.masarrtknavigation.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MasarRtkEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun navigationDao(): MasarRtkDao
}
