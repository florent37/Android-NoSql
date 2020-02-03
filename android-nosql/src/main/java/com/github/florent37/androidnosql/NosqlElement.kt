package com.github.florent37.androidnosql

import com.github.florent37.androidnosql.datasaver.DataSaver
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

sealed class NosqlElement {

    object None : NosqlElement()

    data class Value(val value : Any?) : NosqlElement() {

        fun isNode() = value is Node

        fun node() : Node = value as Node

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

    class Node(private val noSql: NoSqlKt ,val path: String) : NosqlElement() {
        val values = mutableMapOf<String, Any>()

        override fun toString() = "$path - $values"

        suspend fun get(key: String): Any? {
            return if (values.containsKey(key)) {
                values[key]
            } else {
                val node = Node(noSql, path + PATH_SEPARATOR + key)
                values[key] = node
                node
            }
        }

        suspend fun has(key: String) = values.containsKey(key)

        suspend fun keys(): Collection<String> = values.keys

        suspend fun childNodes(): List<Node> {
            val nodes = mutableListOf<Node>()

            keys().forEach { key ->
                val child = values[key]
                if (child is Node) {
                    nodes.add(child)
                }
            }

            return nodes
        }

        suspend fun child(key: String): Any? {
            return get(key) as? NosqlElement
        }

        suspend fun put(name: String, value: Any) {
            values[name] = value
            if (noSql.autoSave) {
                save(AndroidNoSql.dataSaver)
            }
            noSql.notifyListeners(name)
        }

        suspend fun remove(key: String) {
            this.values.remove(key)
            noSql.notifyListeners(key)
        }

        suspend fun removeAll() {
            this.values.clear()
        }

        suspend fun value(key: String): Value {
            return Value(get(key))
        }

        suspend fun save(dataSaver: Collection<DataSaver>) {
            //save keys
            val p = "$path/"
            val keys = values.keys
            dataSaver.forEach { saver ->
                saver.saveNodes(p, keys)
            }

            values.keys.forEach { key ->
                val value = values[key]

                val completePath = path + PATH_SEPARATOR + key;
                if (value.isPrimitiveObject()) {
                    dataSaver.forEach { saver ->
                        saver.saveValue(completePath, value)
                    }
                } else if (value is Node) {
                    value.save(dataSaver)
                }
            }

        }

        @Throws(NoSqlException::class)
        suspend fun write(value: Any?) {
            when (value) {
                null -> {
                    //TODO
                }
                is JSONObject -> { //json object
                    val keys = value.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        try {
                            val childObject = value.get(key)
                            if (childObject.isValueObject()) {
                                put(key, childObject)
                            } else {
                                val child1 = child(key)
                                if(child1 is Node) {
                                    child1.write(childObject)
                                } else {
                                    throw NoSqlException.ExpectedNodeButValue(key)
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
                is JSONArray -> { //json array
                    val length = value.length()
                    for (i in 0 until length) {
                        try {
                            val childObject = value.get(i)
                            val key = i.toString()

                            if (childObject.isValueObject()) {
                                put(key, childObject)
                            } else {
                                val child1 = child(key)
                                if(child1 is Node) {
                                    child1.write(childObject)
                                } else {
                                    throw NoSqlException.ExpectedNodeButValue(key)
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
                else -> { //custom object, write all fields
                    val allFields = value.javaClass.getAllFields()
                    allFields.forEach { field ->
                        field.isAccessible = true

                        try {
                            val fieldName = field.name
                            val fieldValue = field.get(value)
                            if (fieldValue != null) {

                                val completePath = path + PATH_SEPARATOR + fieldName

                                if (fieldValue.isValueObject()) {
                                    noSql.put(fieldName, fieldValue)
                                    noSql.notifyListeners(completePath)
                                } else {
                                    val node1 = Node(noSql, completePath)
                                    noSql.put(fieldName, node1)
                                    noSql.notifyListeners(completePath)
                                    when {
                                        field.isCollection() -> {
                                            val collection = field.get(value) as Iterable<*>
                                            var index = 0
                                            collection.forEach { childObject ->
                                                val key = index.toString()
                                                when {
                                                    childObject == null -> {
                                                        node1.remove(key)
                                                    }
                                                    childObject.isValueObject() -> {
                                                        node1.put(key, childObject)
                                                    }
                                                    else -> {
                                                        val child = node1.child(key)
                                                        if(child is Node) {
                                                            child.write(childObject)
                                                        } else {
                                                            throw NoSqlException.ExpectedNodeButValue(key)
                                                        }
                                                    }
                                                }
                                                index++
                                            }
                                        }
                                        field.isArray() -> {
                                            val array = field.get(value) as Array<*>
                                            var index = 0
                                            array.forEach { childObject ->
                                                val key = index.toString()
                                                when {
                                                    childObject == null -> {
                                                        node1.remove(key)
                                                    }
                                                    childObject.isValueObject() -> {
                                                        node1.put(key, childObject)
                                                    }
                                                    else -> {
                                                        val child = node1.child(key)
                                                        if(child is Node) {
                                                            child.write(childObject)
                                                        } else {
                                                            throw NoSqlException.ExpectedNodeButValue(key)
                                                        }
                                                    }
                                                }
                                                index++
                                            }
                                        }
                                        else -> {
                                            node1.write(fieldValue)
                                        }
                                    }
                                }
                            }
                        } catch (e: IllegalAccessException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            noSql.notifyListeners(path)
        }

    }
}