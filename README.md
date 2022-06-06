# wear-os-eortologio
Wear OS app that shows the celebrating names of each day.

<p align="center">
  <img src="https://i.imgur.com/czBJh6g.png" alt="UI screenshot">
 </p>

# How it works
Both fixed and moving namedays are stored in local JSON files inside the project. This way the app can work without requiring an internet connection to fetch the data, thus resulting in minimum battery consumption.

Moving namedays are calculated based on the Orthodox Easter date of each year, which is calculated using Gauss' algorithm ([read more](https://www.geeksforgeeks.org/how-to-calculate-the-easter-date-for-a-given-year-using-gauss-algorithm/)).

Built with Android Studio, Kotlin and Jetpack Compose for Wear OS. JSON files containing the namedays data can be found [here](https://github.com/stavros-melidoniotis/greek-namedays).

# More screenshots

![screenshot 1](https://i.imgur.com/XiLVC2q.png)
![screenshot 2](https://i.imgur.com/SWVGv4G.png)
![screenshot 3](https://i.imgur.com/APWmZbg.png)
