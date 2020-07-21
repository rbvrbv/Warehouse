package su.rbv.ecwid_sklad.ui.item

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import su.rbv.ecwid_sklad.Const
import su.rbv.ecwid_sklad.R
import su.rbv.ecwid_sklad.data.Event
import su.rbv.ecwid_sklad.data.IRepository
import su.rbv.ecwid_sklad.data.Item
import su.rbv.ecwid_sklad.data.MutableLiveEvent
import su.rbv.ecwid_sklad.utils.loadAndResizePicture
import toothpick.ktp.KTP
import javax.inject.Inject


class ItemViewModel(val itemId: Long) : ViewModel() {

    @Inject lateinit var repo: IRepository

    val errorMessage =
        MutableLiveEvent<Event<Int>>()

    init {
        KTP.openRootScope().inject(this)
    }

    val isExit = MutableLiveData<Boolean>(false)
    val saveButtonEnable = MutableLiveData<Boolean>(false)
    val inputsEnable = MutableLiveData<Boolean>(false)

    var item = Item()

    private val nameObserver = Observer<String> { onFieldsChanged() }
    private val priceObserver = Observer<String> { onFieldsChanged() }

    val itemName = MutableLiveData<String>()
    val itemPrice = MutableLiveData<String>()
    val itemImage = MutableLiveData<ByteArray>()

    init {
        itemName.observeForever(nameObserver)
        itemPrice.observeForever(priceObserver)
        viewModelScope.launch (Dispatchers.IO) {
            if (itemId != Const.NEW_ITEM_ID) {
                /** edit item */
                item = repo.getItem(itemId)
                itemName.postValue(item.name)
                itemPrice.postValue(item.priceStr)
                itemImage.postValue(item.image)
            } else {
                /** new item */
                itemImage.postValue(null)
            }
            inputsEnable.postValue(true)
        }
    }

    /** Save button enable if name and price is not empty */
    private fun onFieldsChanged() {
        saveButtonEnable.value = !itemName.value.isNullOrEmpty() && !itemPrice.value.isNullOrEmpty()
    }

    /** Save button click */
    fun onSaveClick() =
        viewModelScope.launch {
            item.apply {
                name = itemName.value
                priceStr = itemPrice.value
                image = itemImage.value
            }
            repo.setItem(item)
            isExit.postValue(true)
        }

    /** delete item from database */
    suspend fun deleteItem() =
        repo.deleteItem(itemId)

    /**
     *  Write image into item or set it to null
     *  if picture is bad, catch exception and show error message
     */
    fun setItemImage(pictureFileName: String?) {
        try {
            itemImage.value =
                loadAndResizePicture(pictureFileName)
        } catch (e: Exception) {
            errorMessage.value =
                Event(R.string.error_loading_file)
        }
    }

    override fun onCleared() {
        itemName.removeObserver(nameObserver)
        itemPrice.removeObserver(priceObserver)
    }
}