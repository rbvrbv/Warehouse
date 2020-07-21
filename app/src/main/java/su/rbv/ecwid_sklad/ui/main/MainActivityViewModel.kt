package su.rbv.ecwid_sklad.ui.main

import androidx.lifecycle.ViewModel
import su.rbv.ecwid_sklad.data.Event
import su.rbv.ecwid_sklad.data.IRepository
import toothpick.ktp.KTP
import javax.inject.Inject

class MainActivityViewModel : ViewModel() {

    @Inject lateinit var repo: IRepository

    init {
        KTP.openRootScope().inject(this)
    }

    /** pass voice search string to searchView */
    fun setVoiceQueryString(queryString: String?) {
        repo.voiceQueryString.value = Event(queryString)
    }

}