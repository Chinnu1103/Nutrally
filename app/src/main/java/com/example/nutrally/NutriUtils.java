package com.example.nutrally;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class NutriUtils extends AsyncTask<String, Void, List<Food>> {
    private static final String TAG = "NutriUtils";
    private Context mContext;
    private AsyncResponse mResponse;
    private String mOpt;

    public NutriUtils(Context context, AsyncResponse response) {
        mContext = context;
        mResponse = response;
    }

    @Override
    protected void onPostExecute(List<Food> s) {
        super.onPostExecute(s);
        mResponse.processFinish(s, mOpt);
    }

    @Override
    protected List<Food> doInBackground(String... strings) {
        List<Food> foods = new ArrayList<>();
        mOpt = strings[0];
        try {
            if (strings[0].equals("instantQuery")) foods = instantQuery(strings[1]);
            else if (strings[0].equals("naturalQuery")) foods = naturalQuery(strings[1]);
            else if (strings[0].equals("searchItem")) foods = searchItem(strings[1], (int)Math.ceil(Double.parseDouble(strings[2])));
            else foods = naturalExercise(strings[1]);
        } catch (Exception e){
            Log.d(TAG, "onCreate: " + e.toString());
        }
        return foods;
    }

    private List<Food> naturalExercise(String query) throws Exception{
        SharedPreferences sp = mContext.getSharedPreferences("User", MODE_PRIVATE);
        HttpURLConnection connection = null;
        BufferedReader buffReader = null;
        JSONObject request = new JSONObject();
        request.put("query", query);
        request.put("gender", sp.getString("Gender", "Male").toLowerCase());
        request.put("weight_kg", Math.round(sp.getFloat("Weight", 75f)*10)/10f);
        request.put("height_cm", Math.round(sp.getFloat("Height", 170f)*100)/100f);
        request.put("age", sp.getInt("Age", 30));

        byte[] input = request.toString().getBytes(StandardCharsets.UTF_8);
        URL url = new URL("https://trackapi.nutritionix.com/v2/natural/exercise");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", String.valueOf(input.length));
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("x-app-id", mContext.getString(R.string.nutritionix_app_id));
        connection.setRequestProperty("x-app-key", mContext.getString(R.string.nutritionix_app_key));
        connection.getOutputStream().write(input);
        Log.d(TAG, "naturalExercise: " + request.toString());
        buffReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        String line = buffReader.readLine();
        StringBuilder response = new StringBuilder();
        while (line != null) {
            response.append(line);
            line = buffReader.readLine();
        }
        connection.disconnect();
        buffReader.close();

        List<Food> foods = new ArrayList<>();
        JSONObject reader = new JSONObject(response.toString());
        JSONArray exercises = reader.getJSONArray("exercises");
        for (int i=0; i<exercises.length(); ++i) {
            JSONObject obj = exercises.getJSONObject(i);

            int calories = obj.has("nf_calories")?obj.getInt("nf_calories"):0;
            String qty = obj.getString("duration_min") + " min";
            Nutrition nutrition = new Nutrition(calories, qty);
            Food food = new Food(obj.getString("name"), obj.getString("compendium_code"), null, nutrition, null, "Exercise");
            foods.add(food);
        }

        return foods;
    }

    private List<Food> instantQuery(String query) throws Exception{
        List<Food> foods = new ArrayList<>();
        HttpURLConnection connection = null;
        BufferedReader buffReader = null;
        URL url = new URL("https://trackapi.nutritionix.com/v2/search/instant?query=" + query);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(1000);
        connection.setDoOutput(false);
        connection.setRequestProperty("x-app-id", mContext.getString(R.string.nutritionix_app_id));
        connection.setRequestProperty("x-app-key", mContext.getString(R.string.nutritionix_app_key));
        if (query.equals("")) return  foods;
        connection.connect();
        buffReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = buffReader.readLine();
        StringBuilder response = new StringBuilder();
        while (line != null) {
            response.append(line);
            line = buffReader.readLine();
        }

        JSONObject reader = new JSONObject(response.toString());
        JSONArray common = reader.getJSONArray("common");
        int length = common.length() >= 3 ? 3 : common.length();
        for (int i=0; i<length; ++i) {
            JSONObject obj = common.getJSONObject(i);
            String name = obj.getString("food_name");
            JSONObject photo = null;
            String image = null;
            if (obj.has("photo")){
                photo = obj.getJSONObject("photo");
                if (photo.has("thumb")) image = photo.getString("thumb");
            }
            String id = obj.getString("tag_id");
            Food food = new Food(name, id, image, Type.COMMON);
            foods.add(food);
        }

        JSONArray branded = reader.getJSONArray("branded");
        length = branded.length() >= 3 ? 3 : branded.length();
        for (int i=0; i<length; ++i) {
            JSONObject obj = branded.getJSONObject(i);
            String name = obj.getString("brand_name_item_name");
            JSONObject photo = null;
            String image = null;
            if (obj.has("photo")){
                photo = obj.getJSONObject("photo");
                if (photo.has("thumb")) image = photo.getString("thumb");
            }
            String id = obj.getString("nix_item_id");
            Food food = new Food(name, id, image, Type.BRANDED);
            foods.add(food);
        }

        return foods;
    }

    public List<Food> naturalQuery(String query) throws Exception{
        HttpURLConnection connection = null;
        BufferedReader buffReader = null;
        JSONObject data = new JSONObject();
        data.put("query", query);
        byte[] input = data.toString().getBytes(StandardCharsets.UTF_8);
        URL url = new URL("https://trackapi.nutritionix.com/v2/natural/nutrients");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", String.valueOf(input.length));
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("x-app-id", mContext.getString(R.string.nutritionix_app_id));
        connection.setRequestProperty("x-app-key", mContext.getString(R.string.nutritionix_app_key));
        connection.getOutputStream().write(input);

        buffReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        String line = buffReader.readLine();
        StringBuilder response = new StringBuilder();
        while (line != null) {
            response.append(line);
            line = buffReader.readLine();
        }
        connection.disconnect();
        buffReader.close();

        List<Food> foods = new ArrayList<>();
        JSONObject reader = new JSONObject(response.toString());
        JSONArray j_foods = reader.getJSONArray("foods");
        for (int i=0; i<j_foods.length(); ++i) {
            JSONObject obj = j_foods.getJSONObject(i);
            String name = obj.getString("food_name");
            JSONObject photo = null;
            String image = null;
            if (obj.has("photo")){
                photo = obj.getJSONObject("photo");
                if (photo.has("thumb")) image = photo.getString("thumb");
            }

            int calories = obj.has("nf_calories")?obj.getInt("nf_calories"):0;
            int fats = obj.has("nf_total_fat")?obj.getInt("nf_total_fat"):0;
            int cholesterol = obj.has("nf_cholesterol")?obj.getInt("nf_cholesterol"):0;
            int sodium = obj.has("nf_sodium")?obj.getInt("nf_sodium"):0;
            int carbs = obj.has("nf_total_carbohydrate")?obj.getInt("nf_total_carbohydrate"):0;
            int protein = obj.has("nf_protein")?obj.getInt("nf_protein"):0;
            String qty = obj.has("serving_qty")&&obj.has("serving_unit")?obj.getDouble("serving_qty") + " " + obj.getString("serving_unit"):"";
            Nutrition nutrition = new Nutrition(calories, fats, cholesterol, sodium, carbs, protein, qty);
            Food food = new Food(name, query, image, nutrition, Type.BRANDED);
            foods.add(food);
        }

        return foods;
    }

    public List<Food> searchItem(String query, int multiplier) throws Exception{
        List<Food> foods = new ArrayList<>();
        if (query.equals("")) return  foods;
        HttpURLConnection connection = null;
        BufferedReader buffReader = null;
        URL url = new URL("https://trackapi.nutritionix.com/v2/search/item?nix_item_id=" + query);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(1000);
        connection.setDoOutput(false);
        connection.setRequestProperty("x-app-id", mContext.getString(R.string.nutritionix_app_id));
        connection.setRequestProperty("x-app-key", mContext.getString(R.string.nutritionix_app_key));
        connection.connect();
        buffReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = buffReader.readLine();
        StringBuilder response = new StringBuilder();
        while (line != null) {
            response.append(line);
            line = buffReader.readLine();
        }

        JSONObject reader = new JSONObject(response.toString());
        JSONArray j_foods = reader.getJSONArray("foods");
        for (int i=0; i<j_foods.length(); ++i) {
            JSONObject obj = j_foods.getJSONObject(i);
            String name = obj.getString("brand_name") + " " + obj.getString("food_name");
            JSONObject photo = null;
            String image = null;
            if (obj.has("photo")){
                photo = obj.getJSONObject("photo");
                if (photo.has("thumb")) image = photo.getString("thumb");
            }

            int calories = obj.has("nf_calories")?multiplier*obj.getInt("nf_calories"):0;
            int fats = obj.has("nf_total_fat")?multiplier*obj.getInt("nf_total_fat"):0;
            int cholesterol = obj.has("nf_cholesterol")?multiplier*obj.getInt("nf_cholesterol"):0;
            int sodium = obj.has("nf_sodium")?multiplier*obj.getInt("nf_sodium"):0;
            int carbs = obj.has("nf_total_carbohydrate")?multiplier*obj.getInt("nf_total_carbohydrate"):0;
            int protein = obj.has("nf_protein")?multiplier*obj.getInt("nf_protein"):0;
            String qty = obj.has("serving_qty")&&obj.has("serving_unit")?multiplier*obj.getDouble("serving_qty") + " " + obj.getString("serving_unit"):"";
            Nutrition nutrition = new Nutrition(calories, fats, cholesterol, sodium, carbs, protein, qty);
            Food food = new Food(name, query, image, nutrition, Type.BRANDED);
            foods.add(food);
        }

        return foods;
    }

    public interface AsyncResponse {
        void processFinish(List<Food> output, String opt);
    }
}
