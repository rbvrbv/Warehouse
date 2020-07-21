package su.rbv.ecwid_sklad.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Item::class],
    version = 1,
    exportSchema = false
)
abstract class DB : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {
        const val DB_NAME = "DATABASE"
    }

}
