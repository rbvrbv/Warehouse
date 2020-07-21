package su.rbv.ecwid_sklad

import android.app.Application
import android.content.Context
import androidx.room.Room
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import su.rbv.ecwid_sklad.data.DB
import su.rbv.ecwid_sklad.data.IRepository
import su.rbv.ecwid_sklad.data.Repository
import toothpick.ktp.KTP
import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this
        ctx = applicationContext
        cicerone = Cicerone.create()
        navigatorHolder = cicerone.navigatorHolder

        KTP.openRootScope()
            .installModules(
                module {
                    bind(IRepository::class).toInstance(
                        Repository(
                            Room.databaseBuilder(ctx, DB::class.java, DB.DB_NAME)
                                .fallbackToDestructiveMigration().build()
                        )
                    )
                }
            )
    }

    companion object {
        lateinit var instance: App private set
        lateinit var ctx: Context private set
        lateinit var cicerone: Cicerone<Router>
        lateinit var navigatorHolder: NavigatorHolder private set
    }

}
