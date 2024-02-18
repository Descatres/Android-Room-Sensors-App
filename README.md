# Room Sensors App
- This app was made in Java for Android;
- Controls an LED and receives information from two sensors (humidity and temperature);
- Plots the information regarding the humidity and temperature and
- Sends notifications with alerts if the value from one of the sensors (or both) is outside the range specified by the user.

---

- The app communicates with the sensors and LED through MQTT;
- The plotted data is only available when there is a connection;
- The data is saved on Firestore;
- As of now, it only works until Android 12.

---

## How to run
___No apk is available___
- Fork the repo and run it on Android Studio;
- The app can be run by changing the Firestore account with [this tutorial](https://firebase.google.com/docs/firestore/quickstart#:~:text=If%20you%20haven%27t%20already%2C%20create%20a%20Firebase%20project%3A%20In%20the%20Firebase%20console%2C%20click%20Add%20project%2C%20then%20follow%20the%20on%2Dscreen%20instructions%20to%20create%20a%20Firebase%20project%20or%20to%20add%20Firebase%20services%20to%20an%20existing%20GCP%20project.);
- You can simulate the Arduino with the sensors and LED on the following [MQTT sketch](https://wokwi.com/projects/383302430573722625)
- _You can find the code for the sketch [here](/mqtt/sketch.ino)_

## Examples
- LED on and temperature/humidity values of the sensors
  
![image](https://github.com/Descatres/Android-Room-Sensors-App/assets/73725403/bdcd2c78-485a-4c23-9092-962ce0128b23)

- LED off and temperature/humidity values of the sensors
  
![image](https://github.com/Descatres/Android-Room-Sensors-App/assets/73725403/51faef6e-676b-4b20-a116-bc0f00e4bf1b)

- Sensors min and max
  
![image](https://github.com/Descatres/Android-Room-Sensors-App/assets/73725403/7f5fdc8f-5788-496f-9443-b46d053f4a1a)

- Alerts received
  
![image](https://github.com/Descatres/Android-Room-Sensors-App/assets/73725403/9e961b7b-f0be-419e-a9ef-08496d0b9a8a)

- Plot data
  
![image](https://github.com/Descatres/Android-Room-Sensors-App/assets/73725403/514681fe-d50f-48ee-9687-87f88e4d1c83)


#### Note
- _This project was made for a university assignment and the grade was 2.85/3._

