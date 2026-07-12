package de.hwr.smartmedicationreminder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    EditText editName, editEmail, editPassword;
    Button buttonRegister, buttonBack;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonBack = findViewById(R.id.buttonBack);

        // Verbindung zu Firebase Authentication
        auth = FirebaseAuth.getInstance();

        buttonRegister.setOnClickListener(v -> register());

        // Zur Login-Seite zurück
        buttonBack.setOnClickListener(v -> finish());

    }

    private void register() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString();

        // Eingaben prüfen
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Passwort muss mindestens 6 Zeichen haben",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Neues Firebase-Konto erstellen
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    Toast.makeText(this, "Registrierung erfolgreich",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(error ->
                        Toast.makeText(this, "Registrierung fehlgeschlagen",
                                Toast.LENGTH_SHORT).show()
                );
    }

}