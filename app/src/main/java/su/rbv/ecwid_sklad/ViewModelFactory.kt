package su.rbv.ecwid_sklad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import su.rbv.ecwid_sklad.ui.item.ItemViewModel

class ViewModelFactory(private val itemId: Long) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        ItemViewModel(itemId) as T

}
