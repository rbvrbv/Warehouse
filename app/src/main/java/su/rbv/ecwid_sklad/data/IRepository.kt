package su.rbv.ecwid_sklad.data

import androidx.paging.DataSource

interface IRepository {

    val voiceQueryString: MutableLiveEvent<Event<String>>

    fun getItems(query: String? = null): DataSource.Factory<Int, Item>
    suspend fun getItem(itemId: Long): Item
    suspend fun setItem(item: Item)
    suspend fun deleteItem(itemId: Long)
}
