package com.github.florent37.androidnosql.datasaver

interface DataSaver {
    suspend fun saveNodes(completePath: String, value: Set<String>?)
    suspend fun saveValue(completePath: String, value: Any?)
    val nodes: Set<String>
    suspend fun getValue(completePath: String): Any?
    suspend fun remove(startingPath: String)
}