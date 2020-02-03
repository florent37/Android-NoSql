package com.github.florent37.androidnosql

import android.content.Context
import com.github.florent37.androidnosql.datasaver.DataSaver
import com.github.florent37.androidnosql.datasaver.SharedPreferencesDataSaver
import java.util.*
import java.util.concurrent.LinkedBlockingDeque

object AndroidNoSql {
    private val dataSavers: Queue<DataSaver> = LinkedBlockingDeque()
    val dataSaver: Collection<DataSaver>
        get() = ArrayList(dataSavers)

    suspend fun initWith(vararg savers: DataSaver) {
        for (saver in savers) {
            addDataSaver(saver)
        }
    }

    suspend fun initWithDefault(context: Context?) {
        initWith(SharedPreferencesDataSaver(context!!))
    }

    suspend fun addDataSaver(saver: DataSaver) {
        val wasEmpty = dataSavers.isEmpty()
        dataSavers.add(saver)
        if (wasEmpty && !dataSavers.isEmpty()) {
            NoSqlKt.load()
        }
    }

    suspend fun clearDataSavers() {
        for (dataSaver in dataSaver) {
            dataSaver.remove(PATH_SEPARATOR)
        }
    }
}