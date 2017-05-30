package florent37.github.com.nosql.model;

import java.util.List;

/**
 * Created by florentchampigny on 24/05/2017.
 */

public class User {

    private String name;
    private House house;

    private List<Integer> favorites;
    private List<Car> cars;

    public User() {
    }

    public User(String name) {
        this.name = name;
    }

    public User(String name, House house, List<Integer> favorites, List<Car> cars) {
        this.name = name;
        this.house = house;
        this.favorites = favorites;
        this.cars = cars;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public House getHouse() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }

    @Override
    public String toString() {
        return "User{" + '\n' +
                "name='" + name + '\n' +
                "house=\n" + house + '\n' +
                "favorites=\n" + favorites + '\n' +
                "cars=\n" + cars + '\n' +
                '}';
    }
}
