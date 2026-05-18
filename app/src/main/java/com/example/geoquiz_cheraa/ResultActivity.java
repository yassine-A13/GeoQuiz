package com.example.geoquiz_cheraa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {

    private static final String TAG = "ResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        int scoreValue = getIntent().getIntExtra("SCORE", 0);
        TextView scoreTextView = findViewById(R.id.scoreTextView);
        scoreTextView.setText("Score: " + scoreValue + "/5");

        // Envoi automatique du score au backend
        sendScoreToBackend(scoreValue);

        Button retryButton = findViewById(R.id.retryButton);
        retryButton.setOnClickListener(v -> {
            startActivity(new Intent(ResultActivity.this, MainActivity.class));
            finish();
        });
    }

    private void sendScoreToBackend(int scoreValue) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String username = user.getDisplayName();
        if (username == null || username.isEmpty()) {
            username = user.getEmail();
        }

        Score scoreObj = new Score(username, scoreValue);
        ApiService apiService = RetrofitClient.getApiService();

        apiService.submitScore(scoreObj).enqueue(new Callback<Score>() {
            @Override
            public void onResponse(Call<Score> call, Response<Score> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Score envoyé avec succès !");
                    Toast.makeText(ResultActivity.this, "Score enregistré !", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Erreur lors de l'envoi : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Score> call, Throwable t) {
                Log.e(TAG, "Échec réseau : " + t.getMessage());
                Toast.makeText(ResultActivity.this, "Erreur réseau : impossible d'enregistrer le score", Toast.LENGTH_LONG).show();
            }
        });
    }
}
