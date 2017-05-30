package florent37.github.com.nosql;

import android.content.Context;
import android.content.SharedPreferences;

import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;

import com.github.florent37.androidnosql.AndroidNoSql;
import com.github.florent37.androidnosql.Listener;
import com.github.florent37.androidnosql.NoSql;
import com.github.florent37.androidnosql.datasaver.DataSaver;
import com.github.florent37.androidnosql.datasaver.SharedPreferencesDataSaver;
import florent37.github.com.nosql.model.Car;
import florent37.github.com.nosql.model.House;
import florent37.github.com.nosql.model.User;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class NoSqlTest {

    private NoSql noSql;
    private SharedPreferences sharedPreferences;
    private SharedPreferencesDataSaver sharedPreferencesDataSaver;

    @Before
    public void setUp() throws Exception {
        sharedPreferences = RuntimeEnvironment.application.getSharedPreferences("test", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        sharedPreferencesDataSaver = spy(new SharedPreferencesDataSaver(sharedPreferences));

        AndroidNoSql.initWith(
                sharedPreferencesDataSaver
        );
        noSql = NoSql.getInstance();
    }

    @Test
    public void testAdd() throws Exception {
        //Given

        //When
        noSql.put("/numbers/a", 3);
        noSql.put("/numbers/b", 2);

        //Then
        final NoSql.Node node = noSql.node("/numbers/");
        assertThat(node.keys()).containsAllIn(Arrays.asList("a", "b"));
    }

    @Test
    public void testAddUser() throws Exception {
        //Given
        final User user = new User("flo");

        //When
        noSql.put("/user/florent/", user);

        //Then
        final NoSql.Node node = noSql.node("/user/florent/");
        assertThat(node.keys()).contains("name");
        assertThat(node.get("name")).isEqualTo("flo");
    }

    @Test
    public void testRemove() throws Exception {
        //Given
        final User user = new User("flo");
        noSql.put("/user/florent/", user);

        //When
        noSql.remove("/user/florent/");

        //Then
        final NoSql.Node node = noSql.node("/user/");
        assertThat(node.keys()).isEmpty();
    }

    @Test
    public void testAddUser_complex() throws Exception {
        //Given
        final User user = new User(
                "flo",
                new House("paris"),
                Arrays.asList(3, 5),
                Arrays.asList(new Car("chevrolet", "camaro"), new Car("ford", "gt"))
        );

        //When
        noSql.put("/user/florent/", user);

        //Then
        assertThat(noSql.get("/user/florent/house/adress").object()).isEqualTo("paris");
        assertThat(noSql.get("/user/florent/favorites/0").object()).isEqualTo(3);
        assertThat(noSql.get("/user/florent/favorites/1").object()).isEqualTo(5);
        assertThat(noSql.get("/user/florent/cars/0/constructor").object()).isEqualTo("chevrolet");
        assertThat(noSql.get("/user/florent/cars/0/name").object()).isEqualTo("camaro");
        assertThat(noSql.get("/user/florent/cars/1/constructor").object()).isEqualTo("ford");
        assertThat(noSql.get("/user/florent/cars/1/name").object()).isEqualTo("gt");
    }

    @Test
    public void testSave() throws Exception {
        //Given
        final User user = new User("flo");
        noSql.put("/user/florent/", user);

        //When
        noSql.save();

        //Then
        assertThat(sharedPreferences.getStringSet(SharedPreferencesDataSaver.NODES, null))
                .containsAllIn(Arrays.asList("/", "/user", "/user/florent", "/user/florent/name"));
        assertThat(sharedPreferences.getString("/user/florent/name", null))
                .isEqualTo("flo");
    }

    @Test
    public void testSave_incremental() throws Exception {
        //Given
        noSql.put("/numbers/a", 1);
        noSql.save();

        final DataSaver dataSaver = mock(DataSaver.class);
        AndroidNoSql.addDataSaver(dataSaver);

        //When
        noSql.put("/numbers/b", 2);

        //Then

        verify(dataSaver).saveNodes(eq("/numbers/"), eq(Sets.newHashSet(Arrays.asList("a", "b"))));
        verify(dataSaver).saveValue(eq("/numbers/a"), eq(1));
        verify(dataSaver).saveValue(eq("/numbers/b"), eq(2));
        verifyNoMoreInteractions(dataSaver);
    }

    @Test
    public void testNotify() throws Exception {
        //Given
        final User user = new User("flo");
        final Listener listener = spy(new Listener() {

            @Override
            public void nodeChanged(String path, NoSql.Value value) {
                System.out.println(value);
            }

        });
        noSql.notify("/user/", listener);

        //When
        noSql.put("/user/florent/", user);

        //Then
        verify(listener)
                .nodeChanged(eq("/user/florent"), any(NoSql.Value.class));
        verify(listener)
                .nodeChanged(eq("/user/florent"), any(NoSql.Value.class));
    }

    @Test
    public void testLoad() throws Exception {
        //Given
        final User user = new User("flo");
        noSql.autoSave = false;
        noSql.put("/user/florent/", user);
        noSql.put("/numbers/a", 1);
        noSql.put("/numbers/b", 2);
        noSql.save();
        noSql.remove("/");

        //When
        noSql.load();
        noSql.autoSave = true;

        //Then
        assertThat(noSql.node("/numbers/").keys())
                .containsAllIn(Arrays.asList("a", "b"));

        assertThat(noSql.get("/user/florent/name").string())
                .isEqualTo("flo");

        assertThat(noSql.get("/numbers/a").object())
                .isEqualTo(1);
        assertThat(noSql.get("/numbers/b").object())
                .isEqualTo(2);
    }

    @Test
    public void test_delete_recursive() throws Exception {
        //Given
        noSql.put("/numbers/a", 1);
        noSql.put("/numbers/b", 2);

        //When
        noSql.remove("/");

        //Then
        verify(sharedPreferencesDataSaver).remove("/");
        assertThat(sharedPreferencesDataSaver.getNodes()).isEmpty();

        assertThat(noSql.node("/numbers/")).isNull();
    }

    @Test
    public void test_delete_recursive_number() throws Exception {
        //Given
        noSql.put("/users/", "flo");
        noSql.put("/numbers/a", 1);
        noSql.put("/numbers/b", 2);


        //When
        noSql.remove("/numbers/");

        //Then
        verify(sharedPreferencesDataSaver).remove("/numbers/");

        assertThat(sharedPreferencesDataSaver.getNodes()).containsAllIn(Arrays.asList("/", "/users"));

        assertThat(noSql.node("/numbers/")).isNull();
    }

    public static class ValueWith implements ArgumentMatcher<NoSql.Value> {

        private final Object value;

        public ValueWith(Object value) {
            this.value = value;
        }

        @Override
        public boolean matches(NoSql.Value argument) {
            return argument.object().equals(value);
        }

        @Override
        public String toString() {
            return "ValueWith{" +
                    "value=" + value +
                    '}';
        }
    }
}
