package com.example.geoquiz_cheraa;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    // Utiliser 10.0.2.2 pour accéder à l'ordinateur hôte depuis l'émulateur Android
    private static final String OLLAMA_URL = "http://10.0.2.2:11434/api/generate";

    private FusedLocationProviderClient fusedLocationClient;
    private List<Question> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;

    private LinearLayout loadingLayout, quizLayout;
    private TextView statusTextView, questionTextView;
    private Button optionA, optionB, optionC, optionD, nextButton;
    private String selectedAnswer = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Désactiver le mode nuit pour éviter les problèmes d'écran noir
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        loadingLayout = findViewById(R.id.loadingLayout);
        quizLayout = findViewById(R.id.quizLayout);
        statusTextView = findViewById(R.id.statusTextView);
        questionTextView = findViewById(R.id.questionTextView);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        nextButton = findViewById(R.id.nextButton);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupClickListeners();
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        statusTextView.setText("Recherche de la position...");
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                String country = getCountryName(location.getLatitude(), location.getLongitude());
                fetchQuizQuestions(country);
            } else {
                fetchQuizQuestions("Maroc");
            }
        }).addOnFailureListener(e -> fetchQuizQuestions("Maroc"));
    }

    private String getCountryName(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getCountryName();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder error", e);
        }
        return "France";
    }

    private void fetchQuizQuestions(String country) {
        statusTextView.setText("Génération du quiz pour " + country + "...");
        
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "llama3.2:3b");
            jsonBody.put("prompt", "Génère 5 questions de quiz sur : " + country + ". " +
                    "Réponds UNIQUEMENT en JSON comme ceci : " +
                    "{\"questions\": [{\"text\": \"...\", \"options\": [\"...\", \"...\", \"...\", \"...\"], \"answer\": \"A\"}]}");
            jsonBody.put("stream", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, OLLAMA_URL, jsonBody,
                response -> {
                    try {
                        parseAIResponse(response.getString("response"));
                    } catch (JSONException e) {
                        showError("Erreur réponse serveur");
                    }
                },
                error -> {
                    Log.e(TAG, "Volley Error: " + error.toString());
                    showError("Serveur injoignable (Vérifiez Ollama sur 10.0.2.2)");
                }
        );

        // Augmenter le timeout à 90 secondes car la génération peut être lente
        request.setRetryPolicy(new DefaultRetryPolicy(90000, 0, 1f));
        Volley.newRequestQueue(this).add(request);
    }

    private void parseAIResponse(String text) {
        try {
            int start = text.indexOf("{");
            int end = text.lastIndexOf("}");
            String jsonPart = text.substring(start, end + 1);
            JSONObject root = new JSONObject(jsonPart);
            JSONArray array = root.getJSONArray("questions");
            
            questionList.clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject qObj = array.getJSONObject(i);
                List<String> opts = new ArrayList<>();
                JSONArray oArr = qObj.getJSONArray("options");
                for (int j = 0; j < 4; j++) opts.add(oArr.getString(j));
                questionList.add(new Question(qObj.getString("text"), opts, qObj.getString("answer").toUpperCase()));
            }

            loadingLayout.setVisibility(View.GONE);
            quizLayout.setVisibility(View.VISIBLE);
            displayQuestion();
        } catch (Exception e) {
            showError("Format IA invalide");
        }
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questionList.size()) {
            Question q = questionList.get(currentQuestionIndex);
            questionTextView.setText(q.getQuestionText());
            optionA.setText("A) " + q.getOptions().get(0));
            optionB.setText("B) " + q.getOptions().get(1));
            optionC.setText("C) " + q.getOptions().get(2));
            optionD.setText("D) " + q.getOptions().get(3));
            resetOptionButtons();
            nextButton.setEnabled(false);
        } else {
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("SCORE", score);
            intent.putExtra("TOTAL", questionList.size());
            startActivity(intent);
            finish();
        }
    }

    private void setupClickListeners() {
        optionA.setOnClickListener(v -> selectOption("A", optionA));
        optionB.setOnClickListener(v -> selectOption("B", optionB));
        optionC.setOnClickListener(v -> selectOption("C", optionC));
        optionD.setOnClickListener(v -> selectOption("D", optionD));

        nextButton.setOnClickListener(v -> {
            if (selectedAnswer.equals(questionList.get(currentQuestionIndex).getCorrectAnswer())) score++;
            currentQuestionIndex++;
            displayQuestion();
        });
    }

    private void selectOption(String letter, Button button) {
        selectedAnswer = letter;
        resetOptionButtons();
        button.setBackgroundColor(getResources().getColor(R.color.teal_200));
        nextButton.setEnabled(true);
    }

    private void resetOptionButtons() {
        int defaultColor = getResources().getColor(R.color.purple_500);
        optionA.setBackgroundColor(defaultColor);
        optionB.setBackgroundColor(defaultColor);
        optionC.setBackgroundColor(defaultColor);
        optionD.setBackgroundColor(defaultColor);
    }

    private void showError(String message) {
        statusTextView.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            fetchQuizQuestions("Maroc");
        }
    }
}