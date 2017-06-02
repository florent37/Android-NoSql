package com.github.florent37.androidnosql;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import com.github.florent37.androidnosql.datasaver.DataSaver;
import com.github.florent37.androidnosql.datasaver.SharedPreferencesDataSaver;

/**
 * Created by florentchampigny on 29/05/2017.
 */

public class AndroidNoSql {
    private static final Queue<DataSaver> dataSavers = new LinkedBlockingDeque<>();

    public static Collection<DataSaver> getDataSaver() {
        return new ArrayList<>(dataSavers);
    }

    public static NoSql getInstance() {
        return NoSql.getInstance();
    }

    public static void initWith(DataSaver... savers) {
        for (DataSaver saver : savers) {
            addDataSaver(saver);
        }
    }

    public static void initWithDefault(Context context){
        initWith(new SharedPreferencesDataSaver(context));
    }

    public static void addDataSaver(DataSaver saver){
        synchronized (dataSavers) {
            final boolean wasEmpty = dataSavers.isEmpty();
            dataSavers.add(saver);
            if (wasEmpty && !dataSavers.isEmpty()) {
                final NoSql noSql = NoSql.getInstance();
                noSql.load();
            }
        }
    }

    public static void clearDataSavers(){
        for (DataSaver dataSaver : AndroidNoSql.getDataSaver()) {
            dataSaver.remove(NoSql.PATH_SEPARATOR);
        }
    }
}
