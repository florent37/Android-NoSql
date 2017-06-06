package florent37.github.com.nosql;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.florent37.androidnosql.Listener;
import com.github.florent37.androidnosql.NoSql;

import java.util.Arrays;

import florent37.github.com.nosql.model.Car;
import florent37.github.com.nosql.model.House;
import florent37.github.com.nosql.model.User;

public class MainActivity extends Activity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = ((TextView) findViewById(R.id.text));

        final NoSql noSql = NoSql.getInstance();

        final User user = new User(
                "flo",
                new House("paris"),
                Arrays.asList(3, 5),
                Arrays.asList(new Car("chevrolet", "camaro"), new Car("ford", "gt"))
        );

        noSql.notify("/user/", new Listener() {
            @Override
            public void nodeChanged(String path, NoSql.Value value) {

            }
        });

        noSql.put("/user/florent/", user);

        final User userFetched = noSql.get("/user/florent/", User.class);
        textView.setText(userFetched.toString());
    }
}
