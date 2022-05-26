package gr.stavros_melidoniotis.eortologio

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import gr.stavros_melidoniotis.eortologio.databinding.ActivityMainBinding
import java.io.InputStream
import java.util.*

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var currentYear = 2045 //Calendar.getInstance().get(Calendar.YEAR)
        var textView: TextView = findViewById(R.id.text)
        textView.text = "Easter of $currentYear is in ${calculateEasterDay(currentYear)}"
    }

    fun readJSON() {
        val inputStream: InputStream = assets.open("fixed_namedays.json")
    }

    fun calculateEasterDay(year: Int): String {
        var Y1 = year % 4
        var Y2 = year % 7
        var Y3 = year % 19
        var K = 19 * Y3 + 16
        var Y4 = K % 30
        var L = 2 * Y1 + 4 * Y2 + 6 * Y4
        var Y5 = L % 7
        var N = Y4 + Y5 + 3

        return if (N <= 30) "$N/4" else "${N-30}/5"
    }
}