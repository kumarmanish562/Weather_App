package com.manishkumar.weather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    TextView cityName, show;
    Button search;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        cityName = findViewById(R.id.cityName);
        search = findViewById(R.id.search);
        show = findViewById(R.id.weather);

        search.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Fetching Weather Data...", Toast.LENGTH_SHORT).show();
            String city = cityName.getText().toString().trim();

            if (!city.isEmpty()) {
                url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=98bc963c6535659b50c613a0e8207e95&units=metric"; // Added metric units
                try {
                    GetWeather task = new GetWeather();
                    task.execute(url);
                } catch (Exception e) {
                    e.printStackTrace();
                    show.setText("Error fetching weather data.");
                }
            } else {
                Toast.makeText(MainActivity.this, "Enter a City Name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class GetWeather extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (urlConnection != null) urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);

                    // Extract temperature data
                    JSONObject mainObject = jsonObject.getJSONObject("main");
                    double temperature = mainObject.getDouble("temp");
                    double feelsLike = mainObject.getDouble("feels_like");
                    int humidity = mainObject.getInt("humidity");

                    // Extract weather condition
                    JSONArray weatherArray = jsonObject.getJSONArray("weather");
                    JSONObject weatherObject = weatherArray.getJSONObject(0);
                    String weatherCondition = weatherObject.getString("main");
                    String description = weatherObject.getString("description");

                    // Extract wind speed
                    JSONObject windObject = jsonObject.getJSONObject("wind");
                    double windSpeed = windObject.getDouble("speed");

                    // Extract city name
                    String cityName = jsonObject.getString("name");

                    // Display the extracted data
                    String weatherInfo = "City: " + cityName +
                            "\nTemperature: " + temperature + "°C" +
                            "\nFeels Like: " + feelsLike + "°C" +
                            "\nWeather: " + weatherCondition + " (" + description + ")" +
                            "\nHumidity: " + humidity + "%" +
                            "\nWind Speed: " + windSpeed + " m/s";

                    show.setText(weatherInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                    show.setText("Error parsing weather data.");
                }
            } else {
                show.setText("Failed to retrieve weather data.");
            }
        }
    }
}
