package su.rbv.ecwid_sklad.ui.main

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import su.rbv.ecwid_sklad.App
import su.rbv.ecwid_sklad.R
import su.rbv.ecwid_sklad.ui.Screens

class MainActivity : AppCompatActivity() {

    private val navigator = object : SupportAppNavigator(this,
        R.id.main_container
    ) {}

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            App.cicerone.router.newRootScreen(Screens.MainScreen)
            viewModel.setVoiceQueryString(null)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        /** pass voice search string to searchView */
        if (Intent.ACTION_SEARCH == intent.action) {
            viewModel.setVoiceQueryString(intent.getStringExtra(SearchManager.QUERY))
        }
    }

    override fun onResume() {
        super.onResume()
        App.navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        App.navigatorHolder.removeNavigator()
        super.onPause()
    }

}
