package com.github.florent37.androidnosql

interface Listener {
    fun nodeChanged(path: String, element: NosqlElement)
}