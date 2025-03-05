package com.manishkumar.weather;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private EditText etCityName;
    private TextView tvWeatherDetails, tvWindDetails, tvLocation;
    private Button btnGetWeather;

    private String API_KEY;
    private final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load API Key
        API_KEY = getApiKey();

        // Initialize UI components
        etCityName = findViewById(R.id.etCityName);
        tvWeatherDetails = findViewById(R.id.tvWeatherDetails);
        tvWindDetails = findViewById(R.id.tvWindDetails);
        tvLocation = findViewById(R.id.tvLocation);
        btnGetWeather = findViewById(R.id.btnGetWeather);

        btnGetWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchWeather();
            }
        });
    }

    // Securely get API key from local.properties
    private String getApiKey() {
        Properties properties = new Properties();
        try {
            InputStream inputStream = getAssets().open("local.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "API Key not found!", Toast.LENGTH_SHORT).show();
        }
        return properties.getProperty("API_KEY", "default_value_if_missing");
    }

    private void fetchWeather() {
        String cityName = etCityName.getText().toString().trim();

        if (cityName.isEmpty()) {
            Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + cityName + "&appid=" + API_KEY + "&units=metric";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("main") && response.has("wind") && response.has("name")) {
                                JSONObject main = response.getJSONObject("main");
                                double temperature = main.optDouble("temp", -1);
                                double windSpeed = response.getJSONObject("wind").optDouble("speed", -1);
                                String cityName = response.optString("name", "Unknown");

                                if (temperature == -1 || windSpeed == -1 || cityName.equals("Unknown")) {
                                    Toast.makeText(MainActivity.this, "Invalid response from server!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                tvWeatherDetails.setText("Temperature: " + temperature + "°C");
                                tvWindDetails.setText("Wind Speed: " + windSpeed + " m/s");
                                tvLocation.setText("City: " + cityName);
                            } else {
                                Toast.makeText(MainActivity.this, "Invalid city name! Try again.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("WeatherApp", "JSON Error: " + e.getMessage());
                            Toast.makeText(MainActivity.this, "Error parsing weather data!", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("WeatherApp", "API Error: " + error.toString());
                        Toast.makeText(MainActivity.this, "Failed to fetch weather data! Check your internet connection.", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(request);
    }
}
