package su.rbv.ecwid_sklad.data

import toothpick.InjectConstructor

@InjectConstructor
class Repository(private val db: DB) : IRepository {

    override val voiceQueryString =
        MutableLiveEvent<Event<String>>()

    override fun getItems(query: String?) =
        db.itemDao().getItems(
            if (query.isNullOrEmpty()) {
                QUERY_PREFIX_SUFFIX
            } else {
                QUERY_PREFIX_SUFFIX + query + QUERY_PREFIX_SUFFIX
            }
        )

    override suspend fun getItem(itemId: Long) =
        db.itemDao().getItem(itemId)

    override suspend fun setItem(item: Item) {
        db.itemDao().insert(item)
    }

    override suspend fun deleteItem(itemId: Long) {
        db.itemDao().delete(itemId)
    }

    companion object {
        private const val QUERY_PREFIX_SUFFIX = "%"
    }
}
