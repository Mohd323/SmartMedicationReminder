package de.hwr.smartmedicationreminder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddMedicationActivity extends AppCompatActivity {

    EditText editName, editDose, editTime, editStock;
    Button buttonSave, buttonBack;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_medication);

        // Eingabefelder und Buttons finden
        editName = findViewById(R.id.editName);
        editDose = findViewById(R.id.editDose);
        editTime = findViewById(R.id.editTime);
        editStock = findViewById(R.id.editStock);

        buttonSave = findViewById(R.id.buttonSave);
        buttonBack = findViewById(R.id.buttonBack);

        // Firebase verbinden
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Medikament speichern
        buttonSave.setOnClickListener(v -> saveMedication());

        // Zur Home-Seite zurück
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(AddMedicationActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Medikament in Firestore speichern
    private void saveMedication() {

        String name = editName.getText().toString().trim();
        String dose = editDose.getText().toString().trim();
        String time = editTime.getText().toString().trim();
        String stockText = editStock.getText().toString().trim();

        // Prüfen ob alle Felder ausgefüllt sind
        if (name.isEmpty() || dose.isEmpty() || time.isEmpty() || stockText.isEmpty()) {
            Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
            return;
        }

        int stock = Integer.parseInt(stockText);

        Map<String, Object> medication = new HashMap<>();
        medication.put("name", name);
        medication.put("dose", dose);
        medication.put("time", time);
        medication.put("stock", stock);

        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("medications")
                .add(medication)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Medikament gespeichert", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Speichern fehlgeschlagen", Toast.LENGTH_SHORT).show()
                );
    }
}
