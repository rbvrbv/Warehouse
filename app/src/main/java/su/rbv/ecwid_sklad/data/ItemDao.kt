package su.rbv.ecwid_sklad.data

import androidx.paging.DataSource
import androidx.room.*
import su.rbv.ecwid_sklad.data.Item

@Dao
interface ItemDao {
    @Query("SELECT * FROM item WHERE name LIKE :query ORDER BY name")
    fun getItems(query: String): DataSource.Factory<Int, Item>

    @Query("SELECT * FROM item WHERE id = :itemId")
    fun getItem(itemId: Long): Item

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    @Query("DELETE FROM item WHERE id = :itemId")
    suspend fun delete(itemId: Long)
}
