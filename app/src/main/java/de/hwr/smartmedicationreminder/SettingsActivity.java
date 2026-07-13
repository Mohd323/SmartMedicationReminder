package de.hwr.smartmedicationreminder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    Button buttonResetPassword, buttonLogout, buttonBack;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Buttons finden
        buttonResetPassword = findViewById(R.id.buttonResetPassword);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonBack = findViewById(R.id.buttonBack);

        auth = FirebaseAuth.getInstance();

        buttonResetPassword.setOnClickListener(v -> resetPassword());
        buttonLogout.setOnClickListener(v -> logout());
        buttonBack.setOnClickListener(v -> finish());
    }

    // E-Mail zum Zurücksetzen des Passworts senden
    private void resetPassword() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null || user.getEmail() == null) {
            Toast.makeText(this,
                    "Benutzer nicht angemeldet",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(user.getEmail())
                .addOnSuccessListener(result ->
                        Toast.makeText(this,
                                "E-Mail zum Zurücksetzen wurde gesendet",
                                Toast.LENGTH_LONG).show()
                )
                .addOnFailureListener(error ->
                        Toast.makeText(this,
                                "E-Mail konnte nicht gesendet werden",
                                Toast.LENGTH_SHORT).show()
                );
    }

    // Benutzer ausloggen
    private void logout() {

        auth.signOut();

        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);

        // Alte Screens schließen
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
    }
}
