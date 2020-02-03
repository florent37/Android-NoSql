package com.github.florent37.androidnosql

import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.util.*

fun Any?.isValueObject(): Boolean {
    return if(this == null)
        false
    else this.isPrimitiveObject() || this is NosqlElement.Node
}

fun Field.genericType(): Class<*> {
    val listType = this.genericType as ParameterizedType
    return listType.actualTypeArguments[0] as Class<*>
}

fun Any?.isPrimitiveObject(): Boolean {
    return this is String ||
            this is Int ||
            this is Double ||
            this is Float ||
            this is Long
}

fun Field.isPrimitiveField(): Boolean {
    return this.type.isPrimitiveType()
}

fun Class<*>?.isPrimitiveType(): Boolean {
    return Int::class.java.isAssignableFrom(this) ||
            Long::class.java.isAssignableFrom(this) ||
            Double::class.java.isAssignableFrom(this) ||
            Float::class.java.isAssignableFrom(this) ||
            Boolean::class.java.isAssignableFrom(this) ||
            String::class.java.isAssignableFrom(this)
}

fun Field.isCollection(): Boolean {
    return MutableList::class.java.isAssignableFrom(this.type)
}

fun Field.isArray(): Boolean {
    return this.type.isArray
}

fun Field.isInterface(): Boolean {
    return this.type.isInterface
}

fun Class<*>?.getAllFields(): Set<Field> {
    val allFields: MutableSet<Field> = HashSet()

    this?.let { initialClass ->
        var tmpClass = initialClass
        while (tmpClass != Any::class.java) {
            allFields.addAll(listOf(*tmpClass.declaredFields))
            tmpClass = tmpClass.superclass
        }
    }

    return allFields
}
