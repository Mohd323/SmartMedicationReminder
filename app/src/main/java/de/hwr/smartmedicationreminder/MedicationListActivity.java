package de.hwr.smartmedicationreminder;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MedicationListActivity extends AppCompatActivity {

    ListView listMedications;
    Button buttonBack;

    ArrayList<String> medications;
    ArrayAdapter<String> adapter;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medication_list);

        // Elemente finden
        listMedications = findViewById(R.id.listMedications);
        buttonBack = findViewById(R.id.buttonBack);

        // Liste vorbereiten
        medications = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                medications
        );

        listMedications.setAdapter(adapter);

        // Firebase verbinden
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Medikamente laden
        loadMedications();

        // Zurück zum vorherigen Screen
        buttonBack.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });
    }

    // Medikamente aus Firestore laden
    private void loadMedications() {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Benutzer nicht angemeldet", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("medications")
                .get()
                .addOnSuccessListener(result -> {

                    medications.clear();

                    result.forEach(document -> {
                        String name = document.getString("name");
                        String dose = document.getString("dose");
                        String time = document.getString("time");
                        Long stock = document.getLong("stock");

                        String medication =
                                name + " | "
                                        + dose + " | "
                                        + time + " | Bestand: "
                                        + stock;

                        medications.add(medication);
                    });

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(error ->
                        Toast.makeText(
                                this,
                                "Medikamente konnten nicht geladen werden",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}