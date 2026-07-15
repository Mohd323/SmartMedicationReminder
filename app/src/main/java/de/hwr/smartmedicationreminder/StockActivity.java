package de.hwr.smartmedicationreminder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

//  Benutzer öffnet AddMedicationActivity
public class StockActivity extends AppCompatActivity {

    ListView listStock;
    Button buttonCheck, buttonOrder, buttonBack;
    BottomNavigationView bottomNavigation;

    ArrayList<String> stockList;
    ArrayAdapter<String> adapter;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock); // Layout und Eingabefelder werden geladen

        // Oberflächenelemente aus dem XML-Layout verbinden
        listStock = findViewById(R.id.listStock);
        buttonCheck = findViewById(R.id.buttonCheck);
        buttonOrder = findViewById(R.id.buttonOrder);
        buttonBack = findViewById(R.id.buttonBack);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        stockList = new ArrayList<>(); // Leere Liste für die Medikamentenbestände erstellen
        // Adapter erstellen und mit der Liste verbinden
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                stockList
        );

        listStock.setAdapter(adapter); // Adapter mit der ListView verbinden

        db = FirebaseFirestore.getInstance(); // Verbindung zur Firestore-Datenbank herstellen
        auth = FirebaseAuth.getInstance(); // Verbindung zur Firebase-Authentifizierung herstellen

        // Alle Medikamente mit Bestand anzeigen
        loadStock(false);

        // Nur niedrigen Bestand anzeigen
        buttonCheck.setOnClickListener(v -> loadStock(true));

        // Online-Apotheke im Browser öffnen
        buttonOrder.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.shop-apotheke.com/")
            );
            startActivity(intent);
        });

        buttonBack.setOnClickListener(v -> finish());

        // Navigation zwischen den Hauptscreens
        bottomNavigation.setOnItemSelectedListener(item -> {

            Intent intent;

            if (item.getItemId() == R.id.navHome) {
                intent = new Intent(this, HomeActivity.class);

            } else if (item.getItemId() == R.id.navMedications) {
                intent = new Intent(this, MedicationListActivity.class);

            } else if (item.getItemId() == R.id.navHistory) {
                intent = new Intent(this, HistoryActivity.class);

            } else if (item.getItemId() == R.id.navStock) {
                return true;

            } else {
                intent = new Intent(this, SettingsActivity.class);
            }

            startActivity(intent);
            return true;
        });

    }

    // Bestand aus Firestore laden
    private void loadStock(boolean onlyLow) {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Benutzer nicht angemeldet", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("medications")
                .get()
                .addOnSuccessListener(result -> { //Wird ausgeführt, wenn die Daten erfolgreich geladen wurden

                    stockList.clear();
                    boolean lowStock = false;

                    for (var document : result) {
                        String name = document.getString("name");
                        Long stock = document.getLong("stock");

                        if (stock == null) {
                            continue;
                        }

                        // Bei der Prüfung nur Bestand bis 5 anzeigen
                        if (!onlyLow || stock <= 5) {
                            stockList.add(name + " | Bestand: " + stock);
                        }

                        if (stock <= 5) {
                            lowStock = true;
                        }
                    }

                    if (stockList.isEmpty()) {
                        if (onlyLow) {
                            stockList.add("Kein niedriger Bestand");
                        } else {
                            stockList.add("Keine Medikamente vorhanden");
                        }
                    }

                    // Nachbestellen nur bei niedrigem Bestand anzeigen
                    if (onlyLow && lowStock) {
                        buttonOrder.setVisibility(View.VISIBLE);
                    } else {
                        buttonOrder.setVisibility(View.GONE);
                    }
                    // Adapter informieren, dass sich stockList geändert hat
                    //Dadurch wird die ListView auf dem Bildschirm aktualisiert
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(error ->
                        Toast.makeText(
                                this,
                                "Bestand konnte nicht geladen werden",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}
