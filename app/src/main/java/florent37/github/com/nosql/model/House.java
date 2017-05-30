package florent37.github.com.nosql.model;

/**
 * Created by florentchampigny on 24/05/2017.
 */

public class House {
    private String adress;

    public House() {
    }

    public House(String adress) {
        this.adress = adress;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    @Override
    public String toString() {
        return "House {" + '\n' +
                "- adress :" + adress + '\n' +
                '}';
    }
}
