package florent37.github.com.nosql.model

/**
 * Created by florentchampigny on 24/05/2017.
 */
class User {

    var name: String? = null
    var house: House? = null
    private var favorites: List<Int>? = null
    private var cars: List<Car>? = null

    constructor() {}
    constructor(name: String?) {
        this.name = name
    }

    constructor(name: String?, house: House?, favorites: List<Int>?, cars: List<Car>?) {
        this.name = name
        this.house = house
        this.favorites = favorites
        this.cars = cars
    }

    override fun toString(): String {
        return "User{" + '\n' +
                "name='" + name + '\n' +
                "house=\n" + house + '\n' +
                "favorites=\n" + favorites + '\n' +
                "cars=\n" + cars + '\n' +
                '}'
    }
}