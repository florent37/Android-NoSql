package florent37.github.com.nosql

import android.app.Application
import android.content.Context
import com.facebook.stetho.Stetho
import com.github.florent37.androidnosql.AndroidNoSql
import com.github.florent37.androidnosql.datasaver.LogDataSaver
import com.github.florent37.androidnosql.datasaver.SharedPreferencesDataSaver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)

        GlobalScope.launch {
            AndroidNoSql.initWith(
                    SharedPreferencesDataSaver(getSharedPreferences("test", Context.MODE_PRIVATE)),
                    LogDataSaver("LogDataSaver")
            )
            AndroidNoSql.clearDataSavers()
        }
    }
}