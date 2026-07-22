package de.hwr.smartmedicationreminder;

import android.content.Intent;                      // Zwischen Activity wechseln können
import android.os.Bundle;                           // Android-Objekt zum Speichern und Übergeben von Daten zwischen Activity
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;   // Android-Basisklasse für Activities. (Unsere Klasse als Bildschirm in der App anzeigen)
import com.google.firebase.auth.FirebaseAuth;      // Firebase Authentication


public class MainActivity extends AppCompatActivity {  // erbt alle Funktionen

    EditText editEmail, editPassword;
    Button buttonLogin, buttonRegister;
    FirebaseAuth auth;


    @Override                                                   // Wir überschreiben die Methode der Elternklasse
    protected void onCreate(Bundle savedInstanceState) {        // wird automatisch beim Öffnen der Activity aufgerufen, initialisieren die Benutzeroberfläche und unsere Variablen
        super.onCreate(savedInstanceState);                     // Elternklasse initialisieren
        setContentView(R.layout.activity_main);                  // XML-Layout laden (zeigt das Layout activity_main.xml auf dem Bildschirm an)

        // Elemente aus dem XML finden (Java mit XML verbinden)
        editEmail = findViewById(R.id.editEmail);               // findViewById = sucht Element anhand der ID
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Verbindung zu Firebase Authentication
        auth = FirebaseAuth.getInstance();

        // Prüfen ob Benutzer bereits angemeldet ist
        if (auth.getCurrentUser() != null) {

            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }

        // Beim Klick Login-Methode ausführen
        buttonLogin.setOnClickListener(v -> login());

        // Zur Registrieren-Seite wechseln
        buttonRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);    //zwischen Activity wechslen
            startActivity(intent);                             // neue Activity öffnen
        });
    }

    private void login() {
        String email = editEmail.getText().toString().trim();  //Text holen (Editable), in String umwandeln und Leerzeichen entfernen
        String password = editPassword.getText().toString();

        // Eingaben prüfen
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
            return;
        }

        // Benutzer bei Firebase anmelden
        auth.signInWithEmailAndPassword(email, password)

                // wird ausgeführt, wenn Login erfolgreich ist
                .addOnSuccessListener(result -> {
                    Toast.makeText(this, "Login erfolgreich", Toast.LENGTH_SHORT).show();

                    // Nach erfolgreichem Login Home öffnen
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class); //zwischen Activity wechslen
                    startActivity(intent);
                    finish();                   // Login-Seite schließen (Back-Taste geht nicht zurück)
                })

                // wird ausgeführt, wenn Login fehlschlägt
                .addOnFailureListener(error ->
                        Toast.makeText(this, "Login fehlgeschlagen", Toast.LENGTH_SHORT).show()
                );
    }
}