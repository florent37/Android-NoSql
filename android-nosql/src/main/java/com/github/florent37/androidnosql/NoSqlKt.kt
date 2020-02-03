package com.github.florent37.androidnosql

import java.lang.ref.Reference
import java.lang.ref.SoftReference

object NoSqlKt {

    var autoSave = true

    private val listeners = mutableMapOf<String, Reference<Listener>>()

    //region listeners
    suspend fun notifyListeners(path: String) {
        listeners.keys.forEach { key ->
            if (path.startsWith(key)) {
                listeners[key]?.get()?.nodeChanged(path, get(path))
            }
        }
    }

    fun notify(path: String, listener: Listener) {
        listeners[path] = SoftReference(listener)
    }

    fun removeNotifier(listener: Listener) {
        listeners.keys.forEach { key ->
            if (listener == listeners[key]?.get()) {
                listeners.remove(key)
            }
        }
    }
    //endregion

    private var root = NosqlElement.Node(this, "")

    private suspend fun getNodeOrCreate(completeFieldPath: String): NosqlElement.Node {
        val nodeDesc = getNode(completeFieldPath)
        return getOrCreate(nodeDesc.first, nodeDesc.second)
    }

    suspend fun remove(path: String): NoSqlKt {
        if (path == PATH_SEPARATOR) {
            root.removeAll()
        } else {
            val nodeDesc = getNode(path)
            nodeDesc.first.remove(nodeDesc.second)
        }
        if (autoSave) {
            AndroidNoSql.dataSaver.forEach { dataSaver ->
                dataSaver.remove(path)
            }
        }
        return this
    }

    @Throws(NoSqlException::class)
    private suspend fun getOrCreate(node: NosqlElement.Node, name: String): NosqlElement.Node {
        if (node.has(name)) {
            if (node.get(name) is NosqlElement.Node) {
                val child = node.child(name)
                if (child is NosqlElement.Node) {
                    return child
                }
            }
            throw NoSqlException.ExpectedNodeButValue(name)
        } else {
            val node1 = NosqlElement.Node(this, node.path + PATH_SEPARATOR + name)
            node.put(name, node1)
            return node1
        }
    }

    private fun split(uri: String): NodeDescritption {
        val uri = uri.replace("//", "/")
        val strings = uri.split("/")
        val path = mutableListOf<String>()
        strings.forEach { string ->
            if (string.isNotEmpty()) {
                path.add(string)
            }
        }

        val indexLast = path.size - 1
        val name = path[indexLast]
        path.removeAt(indexLast)

        return NodeDescritption(path, name)
    }

    suspend fun get(uri: String): NosqlElement {
        val nodeAndName = getNode(uri)
        val node = nodeAndName.first
        val name = nodeAndName.second
        return node.value(name)
    }

    @Throws(NoSqlException::class)
    suspend fun getNode(uri: String): Pair<NosqlElement.Node, String> {
        var node = root

        val nodeDescritption = split(uri)
        //descending into tree
        nodeDescritption.path.forEach { nodeName ->
            node = getOrCreate(node, nodeName)
        }

        return Pair(node, nodeDescritption.name)
    }

    suspend fun load() {
        val dataSavers = AndroidNoSql.dataSaver
        if (dataSavers.isNotEmpty()) {
            dataSavers.forEach { dataSaver ->
                dataSaver.nodes.forEach { node ->
                    dataSaver.getValue(node)?.let { value ->
                        put(node, value)
                    }
                }
            }
        }
    }

    suspend fun reset(): NoSqlKt {
        root = NosqlElement.Node(this, "")
        clearDataSavers()
        return this
    }

    suspend fun clearDataSavers(): NoSqlKt {
        AndroidNoSql.clearDataSavers()
        return this
    }

    suspend fun save(): NoSqlKt {
        clearDataSavers()
        root.save(AndroidNoSql.dataSaver)
        return this
    }

    suspend fun put(uri: String, value: Any): NoSqlKt {
        val node_and_name = getNode(uri)
        val node = node_and_name.first
        val name = node_and_name.second

        if (value.isValueObject()) {
            node.put(name, value)
            notifyListeners(node.path + PATH_SEPARATOR + name)
        } else {
            getOrCreate(node, name).write(value)
        }

        if (autoSave) {
            node.save(AndroidNoSql.dataSaver)
        }

        return this
    }

    @Throws(NoSqlException::class)
    suspend fun node(path: String): NosqlElement.Node {
        return getNodeOrCreate(path)
    }

    suspend fun <T> get(path: String, aClass: Class<T>): T? {
        try {
            val instance = aClass.newInstance()
            val fields = aClass.getAllFields()
            fields.forEach { field ->
                field.isAccessible = true
                val fieldName = field.name
                val completeFieldPath = path + PATH_SEPARATOR + fieldName

                when {
                    field.isPrimitiveField() -> {
                        val value = get(completeFieldPath) as NosqlElement.Value

                        val fetchedValue = value.value

                        try {
                            field.set(instance, fetchedValue)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    field.isArray() -> {
                        //TODO
                    }
                    field.isCollection() -> {
                        val list: MutableList<Any> = if (field.isInterface()) {
                            mutableListOf()
                        } else {
                            field.type.newInstance() as MutableList<Any>
                        }

                        val node = getNodeOrCreate(completeFieldPath)
                        val childClass = field.genericType()
                        if (childClass.isPrimitiveType()) {
                            node.values.keys.forEach { key ->
                                node.get(key)?.let {
                                    list.add(it)
                                }
                            }
                        } else {
                            node.values.forEach { key ->
                                val pathChild = completeFieldPath + PATH_SEPARATOR + key
                                val child = get(pathChild, childClass)
                                child?.let {
                                    list.add(child)
                                }
                            }
                        }

                        try {
                            field.set(instance, list)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    else -> {
                        val child = get(completeFieldPath, field.type)
                        try {
                            field.set(instance, child)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

            }
            return instance as T
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}