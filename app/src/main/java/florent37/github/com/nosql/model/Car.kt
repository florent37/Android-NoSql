package florent37.github.com.nosql.model

class Car {
    var constructor: String? = null
    var name: String? = null

    constructor(constructor: String?, name: String?) {
        this.constructor = constructor
        this.name = name
    }

    constructor() {}

    override fun toString(): String {
        return "Car{" + '\n' +
                "constructor='" + constructor + '\n' +
                "name='" + name + '\n' +
                '}' + '\n'
    }
}