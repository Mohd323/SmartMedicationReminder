package de.hwr.smartmedicationreminder;

import android.content.Intent;             // wird nicht benutzt
import android.os.Bundle;                 // Android-Objekt zum Speichern und Übergeben von Daten zwischen Activity
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;        // Android-Basisklasse für Activities. (Unsere Klasse als Bildschirm in der App anzeigen)
import com.google.firebase.auth.FirebaseAuth;           // Firebase Authentication

public class RegisterActivity extends AppCompatActivity {   // erbt alle Funktionen

    EditText editName, editEmail, editPassword;
    Button buttonRegister, buttonBack;
    FirebaseAuth auth;

    @Override                                                   // Wir überschreiben die Methode der Elternklasse
    protected void onCreate(Bundle savedInstanceState) {         // wird automatisch beim Öffnen der Activity aufgerufen, initialisieren die Benutzeroberfläche und unsere Variablen
        super.onCreate(savedInstanceState);                      // Elternklasse initialisieren
        setContentView(R.layout.activity_register);             // XML-Layout laden (zeigt das Layout activity_register.xml auf dem Bildschirm an)

        // Elemente aus dem XML finden (Java mit XML verbinden)
        editName = findViewById(R.id.editName);                 // findViewById = sucht Element anhand der ID
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonBack = findViewById(R.id.buttonBack);

        // Verbindung zu Firebase Authentication
        auth = FirebaseAuth.getInstance();

        // Beim Klick Registrierung starten
        buttonRegister.setOnClickListener(v -> register());

        // Zur Login-Seite zurück
        buttonBack.setOnClickListener(v -> finish());

    }

    private void register() {
        // Texte aus den EditText-Feldern lesen
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString();

        // Eingaben prüfen
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
            return;
        }

        // Passwort muss mindestens 6 Zeichen haben
        if (password.length() < 6) {
            Toast.makeText(this, "Passwort muss mindestens 6 Zeichen haben",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Neues Benutzerkonto in Firebase erstellen
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    Toast.makeText(this, "Registrierung erfolgreich",
                            Toast.LENGTH_SHORT).show();
                    finish();                                      // zurück zur LoginActivity bzw. MainActivity
                })
                .addOnFailureListener(error ->
                        Toast.makeText(this, "Registrierung fehlgeschlagen",
                                Toast.LENGTH_SHORT).show()
                );
    }

}