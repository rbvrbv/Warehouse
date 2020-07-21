package su.rbv.ecwid_sklad.ui

import ru.terrakok.cicerone.android.support.SupportAppScreen
import su.rbv.ecwid_sklad.Const
import su.rbv.ecwid_sklad.ui.item.ItemFragment
import su.rbv.ecwid_sklad.ui.main.MainFragment

class Screens {

    object MainScreen : SupportAppScreen() {
        override fun getFragment() = MainFragment()
    }

    class ItemScreen(private val itemId: Long = Const.NEW_ITEM_ID) : SupportAppScreen() {
        override fun getFragment() =
            ItemFragment.create(itemId)
    }

}
