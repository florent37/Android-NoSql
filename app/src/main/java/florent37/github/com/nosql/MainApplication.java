package florent37.github.com.nosql;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

import com.github.florent37.androidnosql.AndroidNoSql;
import com.github.florent37.androidnosql.datasaver.LogDataSaver;
import com.github.florent37.androidnosql.datasaver.SharedPreferencesDataSaver;

import florent37.github.com.androidnosql.paper.PaperDataSaver;

/**
 * Created by florentchampigny on 29/05/2017.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);

        AndroidNoSql.initWith(
                new PaperDataSaver(this),
                //new SharedPreferencesDataSaver(getSharedPreferences("test", Context.MODE_PRIVATE)),
                new LogDataSaver("LogDataSaver")
        );
    }
}
