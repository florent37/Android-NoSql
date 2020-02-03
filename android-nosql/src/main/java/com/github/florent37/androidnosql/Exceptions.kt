package com.github.florent37.androidnosql

sealed class NoSqlException(message: String) : Throwable(message) {
    class ExpectedNodeButValue(key: String) : NoSqlException("$key should be a node")
}