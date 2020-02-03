package com.github.florent37.androidnosql

data class Value(val value : Any?) {

    fun isNode() = value is NosqlElement.Node
    fun node() : NosqlElement.Node = value as NosqlElement.Node

    fun string() = value.toString()

    fun bool() = when (value) {
        is Boolean -> value
        else -> null
    }

    fun integer() = when (value) {
        is Int -> value
        else -> null
    }
}