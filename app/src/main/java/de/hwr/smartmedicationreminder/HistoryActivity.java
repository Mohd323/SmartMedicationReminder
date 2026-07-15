package de.hwr.smartmedicationreminder;

import android.os.Bundle;                       // Android-Objekt zum Speichern und Übergeben von Daten zwischen Activity
import android.widget.ArrayAdapter;             // Verbindet die Daten (ArrayList) mit der ListView
import android.widget.Button;
import android.widget.ListView;                  // Zeigt eine Liste von Einträgen an
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;            // Android-Basisklasse für Activities. (Unsere Klasse als Bildschirm in der App anzeigen)

import com.google.firebase.auth.FirebaseAuth;               // Firebase Authentication
import com.google.firebase.firestore.FirebaseFirestore;    // für das Speichern und Lesen von Daten

import java.util.ArrayList;
import android.content.Intent;                             // Zwischen Activity wechseln können
import com.google.android.material.bottomnavigation.BottomNavigationView;       // Navigation am unteren Bildschirmrand

public class HistoryActivity extends AppCompatActivity {    // erbt alle Funktionen

    ListView listHistory;
    Button buttonBack;
    BottomNavigationView bottomNavigation;

    ArrayList<String> history;
    ArrayAdapter<String> adapter;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override                                                       // Wir überschreiben die Methode der Elternklasse
    protected void onCreate(Bundle savedInstanceState) {    // wird automatisch beim Öffnen der Activity aufgerufen, initialisieren die Benutzeroberfläche und unsere Variablen
        super.onCreate(savedInstanceState);                 // Elternklasse initialisieren
        setContentView(R.layout.activity_history);                  // XML-Layout laden (zeigt das Layout activity_history.xml auf dem Bildschirm an)

        // Elemente aus dem XML finden (Java mit XML verbinden)
        listHistory = findViewById(R.id.listHistory);               // findViewById = sucht Element anhand der ID
        buttonBack = findViewById(R.id.buttonBack);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Erstellt eine leere Liste für den Verlauf
        history = new ArrayList<>();

        //ArrayList mit der ListView vorbereiten (this: aktuelle Activity, simple_list_item_1: eine Zeile pro Element, history: ArrayList Daten)
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                history
        );

        // Verbindet die ListView mit dem Adapter
        listHistory.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();           // Firestore-Instanz holen
        auth = FirebaseAuth.getInstance();              // Verbindung zu Firebase Authentication

        loadHistory();                                  // Methode

        buttonBack.setOnClickListener(v -> finish());        // Schließt die aktuelle Activity


        // Navigation zwischen den Hauptscreens
        bottomNavigation.setOnItemSelectedListener(item -> {

            Intent intent;

            // Prüfen, welches Menüelement ausgewählt wurde
            if (item.getItemId() == R.id.navHome) {
                intent = new Intent(this, HomeActivity.class);

            } else if (item.getItemId() == R.id.navMedications) {
                intent = new Intent(this, MedicationListActivity.class);

            } else if (item.getItemId() == R.id.navHistory) {
                return true;                         // Bereits auf History → nichts machen

            } else if (item.getItemId() == R.id.navStock) {
                intent = new Intent(this, StockActivity.class);

            } else {
                intent = new Intent(this, SettingsActivity.class);
            }

            startActivity(intent);
            return true;
        });

    }

    private void loadHistory() {

        if (auth.getCurrentUser() == null) return;      // Prüfen, ob ein Benutzer angemeldet ist - Methode beenden, wenn niemand angemeldet ist

        String userId = auth.getCurrentUser().getUid();   // Aktuelle Benutzer-ID aus Firebase holen

        // Firestore-Pfad: users -> userId -> history
        db.collection("users")
                .document(userId)
                .collection("history")
                .get()                              //  MedikamentHistory (Verlauf) des Benutzers laden
                .addOnSuccessListener(result -> { // wenn das Laden erfolgreich ist

                    history.clear();                                     // Alte Einträge löschen, damit keine Duplikate entstehen

                    result.forEach(document -> {    // Geht durch alle Dokumente im Ergebnis

                        String name = document.getString("name");   // Liest den Medikamentennamen
                        String date = document.getString("date");   // Liest das Datum

                        history.add(name + " | " + date);                // Fügt den Eintrag zur ArrayList hinzu
                    });

                    adapter.notifyDataSetChanged();             // Aktualisiert die ListView nach den Änderungen
                })
                .addOnFailureListener(error ->        // Wird ausgeführt, wenn das Laden fehlschlägt
                        Toast.makeText(
                                this,
                                "Verlauf konnte nicht geladen werden",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}