package gr.stavros_melidoniotis.eortologio

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import gr.stavros_melidoniotis.eortologio.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var calendar: Calendar
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        calendar = Calendar.getInstance()
        setContentView(binding.root)

        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
        val month: Int = calendar.get(Calendar.MONTH) + 1
        val year: Int = calendar.get(Calendar.YEAR)

        textView = findViewById(R.id.text)

        val fixedNamedays: JSONObject? = getFixedNamedays()
        val namesToCelebrate = fixedNamedays
                                ?.getJSONObject("$day/$month")
                                ?.getJSONArray("names")

        println(namesToCelebrate?.join(", "))

        textView.text = namesToCelebrate?.join(",")
    }

    fun getFixedNamedays(): JSONObject? {
        val json: String?
        var inputStream: InputStream? = null

        return try {
            inputStream = assets.open("fixed_namedays.json")
            json = inputStream.bufferedReader().use { it.readText() }

            JSONObject(json)
        } catch (e: IOException) {
            println(e)
            null
        } finally {
            inputStream?.close()
        }
    }

    fun calculateEasterDay(year: Int): String {
        val Y1 = year % 4
        val Y2 = year % 7
        val Y3 = year % 19
        val K = 19 * Y3 + 16
        val Y4 = K % 30
        val L = 2 * Y1 + 4 * Y2 + 6 * Y4
        val Y5 = L % 7
        val N = Y4 + Y5 + 3

        return if (N <= 30) "$N/4" else "${N-30}/5"
    }
}