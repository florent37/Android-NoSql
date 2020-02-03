package com.github.florent37.androidnosql.datasaver

import android.util.Log

class LogDataSaver(private val tag: String) : DataSaver {

    override val nodes = HashSet<String>()

    override suspend fun saveNodes(completePath: String, value: Set<String>?) {
        Log.d(tag, "nodes: " + completePath + " -> " + value.toString())
    }

    override suspend fun saveValue(completePath: String, value: Any?) {
        Log.d(tag, "values: " + completePath + " -> " + value.toString())
    }

    override suspend fun getValue(completePath: String): Any? {
        return null
    }

    override suspend fun remove(startingPath: String) {
        Log.d(tag, "remove starting with: $startingPath")
    }

}