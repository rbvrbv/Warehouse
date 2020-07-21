package su.rbv.ecwid_sklad.ui.main

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.launch
import su.rbv.ecwid_sklad.data.IRepository
import su.rbv.ecwid_sklad.data.Item
import toothpick.ktp.KTP
import javax.inject.Inject

class MainViewModel : ViewModel() {

    @Inject lateinit var repo: IRepository

    var items: LiveData<PagedList<Item>>

    val queryString = MutableLiveData<String>()

    init {
        KTP.openRootScope().inject(this)

        val config = PagedList.Config.Builder()
            .setPageSize(PAGE_SIZE)
            .setInitialLoadSizeHint(INITIAL_LOAD_SIZE)
            .setPrefetchDistance(PREFETCH_DISTANCE)
            .setEnablePlaceholders(false)
            .build()

        /** get items list with search query */
        items = Transformations.switchMap(queryString) { queryString ->
            LivePagedListBuilder(repo.getItems(queryString), config).build()
        }

        /** initial query string */
        queryString.value = null
    }

    val voiceQueryString = repo.voiceQueryString

    /** remove item from list */
    fun deleteItem(itemId: Long) =
        viewModelScope.launch {
            repo.deleteItem(itemId)
        }


    companion object {
        private const val PAGE_SIZE = 10
        private const val INITIAL_LOAD_SIZE = 20
        private const val PREFETCH_DISTANCE = 5
    }
}