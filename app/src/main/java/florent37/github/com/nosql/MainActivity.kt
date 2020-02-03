package florent37.github.com.nosql

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.florent37.androidnosql.Listener
import com.github.florent37.androidnosql.NoSqlKt
import com.github.florent37.androidnosql.NosqlElement
import florent37.github.com.nosql.model.Car
import florent37.github.com.nosql.model.House
import florent37.github.com.nosql.model.User
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity(R.layout.activity_main), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val user = User(
                "flo",
                House("paris"),
                listOf(3, 5),
                listOf(
                        Car("chevrolet", "camaro"),
                        Car("ford", "gt")
                )
        )

        val noSql = NoSqlKt
        launch {
            noSql.notify("/user/", object : Listener {
                override fun nodeChanged(path: String, element: NosqlElement) {

                }
            })
            noSql.put("/user/florent/", user)
            val userFetched: User? = noSql.get("/user/florent/", User::class.java)

            val textView : TextView = findViewById(R.id.text)
            textView.text = userFetched.toString()
        }
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }
}