package de.hwr.smartmedicationreminder;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditMedicationActivity extends AppCompatActivity {

    EditText editName, editDose, editTime, editStock;
    Button buttonSave, buttonBack;

    FirebaseFirestore db;
    FirebaseAuth auth;
    String medicationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_medication);

        editName = findViewById(R.id.editName);
        editDose = findViewById(R.id.editDose);
        editTime = findViewById(R.id.editTime);
        editStock = findViewById(R.id.editStock);
        buttonSave = findViewById(R.id.buttonSave);
        buttonBack = findViewById(R.id.buttonBack);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Daten aus der Liste erhalten
        medicationId = getIntent().getStringExtra("id");
        editName.setText(getIntent().getStringExtra("name"));
        editDose.setText(getIntent().getStringExtra("dose"));
        editTime.setText(getIntent().getStringExtra("time"));
        editStock.setText(getIntent().getStringExtra("stock"));

        buttonSave.setOnClickListener(v -> updateMedication());
        buttonBack.setOnClickListener(v -> finish());
    }

    // Medikament aktualisieren
    private void updateMedication() {

        String name = editName.getText().toString().trim();
        String dose = editDose.getText().toString().trim();
        String time = editTime.getText().toString().trim();
        String stockText = editStock.getText().toString().trim();

        if (name.isEmpty() || dose.isEmpty() || time.isEmpty() || stockText.isEmpty()) {
            Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> medication = new HashMap<>();
        medication.put("name", name);
        medication.put("dose", dose);
        medication.put("time", time);
        medication.put("stock", Integer.parseInt(stockText));

        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("medications")
                .document(medicationId)
                .update(medication)
                .addOnSuccessListener(result -> {
                    Toast.makeText(this, "Medikament aktualisiert", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
