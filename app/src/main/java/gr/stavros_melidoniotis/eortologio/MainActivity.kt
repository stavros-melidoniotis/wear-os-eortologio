package gr.stavros_melidoniotis.eortologio

import android.R
import android.content.res.AssetManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.compose.material.TimeTextDefaults.timeFormat
import gr.stavros_melidoniotis.eortologio.ui.theme.EortologioTheme
import gr.stavros_melidoniotis.eortologio.ui.theme.Shapes
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : ComponentActivity() {

    private lateinit var calendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        calendar = Calendar.getInstance()

        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
        val month: Int = calendar.get(Calendar.MONTH) + 1
        val year: Int = calendar.get(Calendar.YEAR)

        val fixedNamedays: JSONObject = loadNamedaysFromFile(assets, "fixed_namedays.json")
        val namesJSONArray = fixedNamedays
            .getJSONObject("$day/$month")
            .getJSONArray("names")
        val namesToCelebrate: ArrayList<String> = arrayListOf<String>()

        for (i: Int in 0 until namesJSONArray.length()) {
            val name = namesJSONArray.getString(i)
            namesToCelebrate.add(name)
        }

        setContent {
            EortologioTheme {
                val scalingLazyListState: ScalingLazyListState = rememberScalingLazyListState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    timeText = {
                        TimeText(
                            startCurvedContent = {
                                curvedText(
                                    text = "$day/$month",
                                )
                            },
                            startLinearContent = {
                                Text(
                                    text = "$day/$month",
                                    style = TimeTextDefaults.timeTextStyle()
                                )
                            },
                            timeSource = TimeTextDefaults.timeSource(timeFormat())
                        )
                    },
                    positionIndicator = {
                        if (scalingLazyListState.isScrollInProgress) {
                            PositionIndicator(
                                scalingLazyListState = scalingLazyListState
                            )
                        }
                    },
                    vignette = {
                        Vignette(vignettePosition = VignettePosition.TopAndBottom)
                    }
                ) {
                    ScalingLazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 60.dp,
                            bottom = 5.dp
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        state = scalingLazyListState
                    ) {
                        items(namesToCelebrate.size) { name ->
                            Chip(
                                modifier = Modifier
                                    .width(140.dp)
                                    .padding(top = 10.dp),
//                            icon = {
//                                Icon(
//                                    painter = painterResource(id = R.drawable.btn_star_big_on),
//                                    contentDescription = "Star",
//                                    modifier = Modifier
//                                        .size(24.dp)
//                                        .wrapContentSize(align = Alignment.Center),
//                                )
//                            },
                                label = {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colors.onPrimary,
                                        text = namesToCelebrate[name]
                                    )
                                },
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

fun loadNamedaysFromFile(assets: AssetManager, filename: String): JSONObject {
    val json: String?
    var inputStream: InputStream? = null

    return try {
        inputStream = assets.open(filename)
        json = inputStream.bufferedReader().use { it.readText() }

        JSONObject(json)
    } catch (e: IOException) {
        println(e)
        JSONObject()
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

    return if (N <= 30) "$N/4" else "${N - 30}/5"
}