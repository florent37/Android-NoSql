package com.github.florent37.androidnosql.datasaver

import android.content.Context
import android.content.SharedPreferences
import java.util.*

class SharedPreferencesDataSaver(private val sharedPreferences: SharedPreferences) : DataSaver {

    constructor(context: Context) : this(context.getSharedPreferences(NODES_SHARED_PREFS, Context.MODE_PRIVATE)) {}

    override suspend fun saveNodes(completePath: String, values: Set<String>?) {
        val nodes = HashSet<String?>()
        nodes.add(completePath)

        nodes.addAll(this.nodes)

        values?.forEach {
            nodes.add(completePath + it)
        }

        sharedPreferences.edit()
                .putStringSet(NODES, nodes)
                .apply()
    }

    override suspend fun saveValue(completePath: String, value: Any?) {
        value?.let { value ->
            val editor = sharedPreferences.edit()
            when (value) {
                is Int -> {
                    editor.putInt(completePath, value)
                }
                is Float -> {
                    editor.putFloat(completePath, value)
                }
                is Boolean -> {
                    editor.putBoolean(completePath, value)
                }
                is Long -> {
                    editor.putLong(completePath, value)
                }
                else -> {
                    editor.putString(completePath, value.toString())
                }
            }
            editor.apply()
        } ?: run {
            sharedPreferences
                    .edit()
                    .remove(completePath).apply()
        }

    }

    override val nodes: Set<String>
        get() = sharedPreferences.getStringSet(NODES, HashSet())

    override suspend fun getValue(completePath: String): Any? {
        return sharedPreferences.all[completePath]
    }

    override suspend fun remove(startingPath: String) {
        val nodes = nodes
        val nodesToKeep: MutableSet<String> = HashSet()
        //update values
        for (node in nodes) {
            if (node.startsWith(startingPath)) {
                sharedPreferences.edit().remove(node).apply()
            } else {
                nodesToKeep.add(node)
            }
        }
        sharedPreferences.edit()
                .putStringSet(NODES, nodesToKeep)
                .apply()
    }

    companion object {
        const val NODES = "%nodes%"
        const val NODES_SHARED_PREFS = "%nodes%"
    }

}