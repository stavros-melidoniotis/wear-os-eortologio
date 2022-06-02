package gr.stavros_melidoniotis.eortologio

import android.content.res.AssetManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.compose.material.TimeTextDefaults.timeFormat
import gr.stavros_melidoniotis.eortologio.ui.theme.EortologioTheme
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : ComponentActivity() {

    private lateinit var calendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        val (easterDay, easterMonth) = calculateEasterDay(year)

        val fixedNamedays: JSONObject = loadNamedaysFromFile(assets, "fixed_namedays.json")
        val movingNamedays: JSONObject = loadNamedaysFromFile(assets, "moving_namedays.json")
        val allNamedays =
            calculateMovingNamedays(fixedNamedays, movingNamedays, easterDay, easterMonth, year)

        val namesJSONArray = allNamedays
            .getJSONObject("$day/$month")
            .getJSONArray("names")
        val namesToCelebrate: ArrayList<String> = arrayListOf<String>()

        for (i: Int in 0 until namesJSONArray.length()) {
            val name = namesJSONArray.getString(i)
            namesToCelebrate.add(name)
        }

        setContent {
            EortologioTheme {
                EortologioApp(
                    day = day,
                    month = month,
                    namesToCelebrate = namesToCelebrate
                )
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
        e.printStackTrace()
        JSONObject()
    } finally {
        inputStream?.close()
    }
}

fun calculateMovingNamedays(
    fixedNamedaysJSON: JSONObject,
    movingNamedaysJSON: JSONObject,
    easterDay: Int,
    easterMonth: Int,
    currentYear: Int
): JSONObject {
    val fixedAndMovingNamedays: JSONObject = fixedNamedaysJSON
    val movingNamedays: JSONArray = movingNamedaysJSON.getJSONArray("namedays")
    val easterDate = LocalDate.parse(
        "$easterDay-$easterMonth-$currentYear",
        DateTimeFormatter.ofPattern("dd-M-yyyy")
    )

    for (i: Int in 0 until movingNamedays.length()) {
        val movingNameday = movingNamedays.getJSONObject(i)
        val names = movingNameday.getJSONArray("names")
        val celebrationMethod = movingNameday.getJSONObject("celebration")
        val celebrationDay: LocalDate

        when (celebrationMethod.getString("dependency")) {
            "special_1" -> {
                celebrationDay = LocalDate
                    .of(currentYear, Calendar.FEBRUARY + 1, 13)
                    .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            }
            "special_2" -> {
                celebrationDay = LocalDate
                    .of(currentYear, Calendar.DECEMBER + 1, 11)
                    .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            }
            "easter_special" -> {
                val easterSpecialReference =
                    LocalDate.parse("23-4-$currentYear", DateTimeFormatter.ofPattern("dd-M-yyyy"))
                val normalDate = celebrationMethod.getString("normal_date")

                celebrationDay = if (easterDate.isAfter(easterSpecialReference)) {
                    val daysToAdd = celebrationMethod.getLong("moved_date")
                    easterDate.plusDays(daysToAdd)
                } else {
                    LocalDate.parse(
                        "$normalDate/$currentYear",
                        DateTimeFormatter.ofPattern("dd/M/yyyy")
                    )
                }
            }
            "easter" -> {
                val differenceFromEaster = celebrationMethod.getLong("moved_date")
                celebrationDay = easterDate.plusDays(differenceFromEaster)
            }
            else -> continue
        }

        for (k: Int in 0 until names.length()) {
            fixedAndMovingNamedays
                .getJSONObject("${celebrationDay.dayOfMonth}/${celebrationDay.monthValue}")
                .getJSONArray("names")
                // Add the "_is_moving" suffix to differentiate between moving and
                // non-moving namedays
                .put("${names[k]}_is_moving")
        }
    }

    return fixedAndMovingNamedays
}

fun calculateEasterDay(year: Int): Array<Int> {
    val y1 = year % 4
    val y2 = year % 7
    val y3 = year % 19
    val k = 19 * y3 + 16
    val y4 = k % 30
    val l = 2 * y1 + 4 * y2 + 6 * y4
    val y5 = l % 7
    val n = y4 + y5 + 3

    return if (n <= 30) arrayOf(n, 4) else arrayOf(n - 30, 5)
}

@Composable
fun EortologioApp(day: Int, month: Int, namesToCelebrate: ArrayList<String>) {
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
        when (namesToCelebrate.size) {
            0 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.no_namedays)
                    )
                }
            }
            1 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NameChip(name = namesToCelebrate[0])
                }
            }
            else -> {
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
                        NameChip(name = namesToCelebrate[name])
                    }
                }
            }
        }
    }
}

@Composable
fun NameChip(name: String) {
    val cleanedName = if (name.contains("_is_moving")) {
        name.removeSuffix("_is_moving")
    } else {
        name
    }

    Chip(
        modifier = Modifier
            .width(140.dp)
            .padding(top = 10.dp),
        label = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.onPrimary,
                text = cleanedName
            )
        },
        secondaryLabel = {
            if (name.contains("_is_moving")) {
                Text(
                    text = stringResource(id = R.string.moving_nameday_sec_label),
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Light,
                )
            }
        },

        onClick = {}
    )
}