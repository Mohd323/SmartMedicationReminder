package de.hwr.smartmedicationreminder;

import android.app.AlertDialog;
import android.content.Intent;
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
    ArrayList<String> medicationIds;
    ArrayList<String> names, doses, times, stocks;

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

        // Listen vorbereiten
        medications = new ArrayList<>();
        medicationIds = new ArrayList<>();
        names = new ArrayList<>();
        doses = new ArrayList<>();
        times = new ArrayList<>();
        stocks = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                medications
        );

        listMedications.setAdapter(adapter);

        // Firebase verbinden
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Medikament durch kurzen Klick bearbeiten
        listMedications.setOnItemClickListener((parent, view, position, id) -> {

            Intent intent = new Intent(
                    MedicationListActivity.this,
                    EditMedicationActivity.class
            );

            intent.putExtra("id", medicationIds.get(position));
            intent.putExtra("name", names.get(position));
            intent.putExtra("dose", doses.get(position));
            intent.putExtra("time", times.get(position));
            intent.putExtra("stock", stocks.get(position));

            startActivity(intent);
        });

        // Medikament durch langen Klick löschen
        listMedications.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position);
            return true;
        });

        buttonBack.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Liste nach dem Bearbeiten neu laden
        if (db != null && auth != null) {
            loadMedications();
        }
    }

    // Medikamente aus Firestore laden
    private void loadMedications() {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(
                    this,
                    "Benutzer nicht angemeldet",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("medications")
                .get()
                .addOnSuccessListener(result -> {

                    medications.clear();
                    medicationIds.clear();
                    names.clear();
                    doses.clear();
                    times.clear();
                    stocks.clear();

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
                        medicationIds.add(document.getId());

                        names.add(name);
                        doses.add(dose);
                        times.add(time);
                        stocks.add(String.valueOf(stock));
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

    // Sicherheitsfrage anzeigen
    private void showDeleteDialog(int position) {

        new AlertDialog.Builder(this)
                .setTitle("Medikament löschen")
                .setMessage("Möchten Sie dieses Medikament wirklich löschen?")
                .setPositiveButton(
                        "Ja",
                        (dialog, which) -> deleteMedication(position)
                )
                .setNegativeButton("Nein", null)
                .show();
    }

    // Medikament aus Firestore löschen
    private void deleteMedication(int position) {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String medicationId = medicationIds.get(position);

        db.collection("users")
                .document(userId)
                .collection("medications")
                .document(medicationId)
                .delete()
                .addOnSuccessListener(result -> {

                    medications.remove(position);
                    medicationIds.remove(position);
                    names.remove(position);
                    doses.remove(position);
                    times.remove(position);
                    stocks.remove(position);

                    adapter.notifyDataSetChanged();

                    Toast.makeText(
                            this,
                            "Medikament gelöscht",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(error ->
                        Toast.makeText(
                                this,
                                "Löschen fehlgeschlagen",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}