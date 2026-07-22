package de.hwr.smartmedicationreminder;

import android.content.Intent;                      // Zwischen Activities wechseln
import android.os.Bundle;                           // Daten der Activity verwalten
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;   // Activity als Bildschirm anzeigen

import com.google.firebase.auth.FirebaseAuth;      // Firebase Authentication

public class MainActivity extends AppCompatActivity {

    // Eingabefelder und Buttons
    EditText editEmail, editPassword;
    Button buttonLogin, buttonRegister;

    // Firebase Authentication
    FirebaseAuth auth;

    @Override                                          // Methode der Elternklasse überschreiben
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);            // Elternklasse initialisieren
        setContentView(R.layout.activity_main);        // XML-Layout anzeigen

        // Java mit den Elementen aus dem XML verbinden
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Verbindung zu Firebase Authentication herstellen
        auth = FirebaseAuth.getInstance();

        // Beim Klick auf Login Benutzer anmelden
        buttonLogin.setOnClickListener(v -> login());

        // Zur Registrierungsseite wechseln
        buttonRegister.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Prüfen, ob der Benutzer bereits angemeldet ist
        if (auth.getCurrentUser() != null) {

            Intent intent = new Intent(
                    MainActivity.this,
                    HomeActivity.class
            );

            startActivity(intent);
            finish();      // Login-Seite schließen
        }
    }

    // Benutzer bei Firebase anmelden
    private void login() {

        // Eingaben aus den EditText-Feldern holen
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString();

        // Prüfen, ob alle Felder ausgefüllt wurden
        if (email.isEmpty() || password.isEmpty()) {

            Toast.makeText(
                    this,
                    "Bitte alle Felder ausfüllen",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Login mit Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)

                // Login erfolgreich
                .addOnSuccessListener(result -> {

                    Toast.makeText(
                            this,
                            "Login erfolgreich",
                            Toast.LENGTH_SHORT
                    ).show();

                    // Nach erfolgreichem Login Home öffnen
                    Intent intent = new Intent(
                            MainActivity.this,
                            HomeActivity.class
                    );

                    startActivity(intent);
                    finish();      // Login-Seite schließen
                })

                // Login fehlgeschlagen
                .addOnFailureListener(error ->

                        Toast.makeText(
                                this,
                                "Login fehlgeschlagen",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}
