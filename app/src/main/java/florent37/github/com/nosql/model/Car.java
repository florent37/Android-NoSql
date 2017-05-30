package florent37.github.com.nosql.model;

/**
 * Created by florentchampigny on 29/05/2017.
 */

public class Car {
    String constructor;
    String name;

    public String getConstructor() {
        return constructor;
    }

    public void setConstructor(String constructor) {
        this.constructor = constructor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Car(String constructor, String name) {
        this.constructor = constructor;
        this.name = name;
    }

    public Car() {
    }

    @Override
    public String toString() {
        return "Car{" + '\n' +
                "constructor='" + constructor + '\n' +
                "name='" + name + '\n' +
                '}' + '\n';
    }
}
