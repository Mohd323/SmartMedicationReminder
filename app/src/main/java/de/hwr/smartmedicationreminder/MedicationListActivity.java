package de.hwr.smartmedicationreminder;

import android.app.AlertDialog;                 // Dialogfenster für Bestätigungen anzeigen
import android.content.Intent;                  // Wechsel zwischen Activities
import android.os.Bundle;                       // Speichert und übergibt Daten beim Start der Activity
import android.widget.ArrayAdapter;            // Verbindet die ArrayList mit der ListView
import android.widget.Button;                  // Button für Benutzeraktionen
import android.widget.ListView;                // Zeigt eine Liste von Einträgen an
import android.widget.Toast;                   // Zeigt eine kurze Meldung auf dem Bildschirm

import androidx.activity.EdgeToEdge;           // Nutzt den gesamten Bildschirm
import androidx.appcompat.app.AppCompatActivity; // Basisklasse für Activities
import androidx.core.graphics.Insets;          // Enthält Abstände der Systemleisten
import androidx.core.view.ViewCompat;          // Hilfsmethoden für Views
import androidx.core.view.WindowInsetsCompat;  // Informationen über Systemleisten

import com.google.firebase.auth.FirebaseAuth;      // Firebase-Authentifizierung
import com.google.firebase.firestore.FirebaseFirestore; // Firestore-Datenbank
import com.google.android.material.bottomnavigation.BottomNavigationView; // Navigationsleiste unten

import java.util.ArrayList;                   // Dynamische Liste
import java.util.HashMap;                     // Speichert Daten als Schlüssel-Wert-Paare
import java.util.Map;                         // Oberklasse für Schlüssel-Wert-Paare
import java.text.SimpleDateFormat;            // Formatiert Datum und Uhrzeit
import java.util.Date;                        // Aktuelles Datum und Uhrzeit
import java.util.Locale;                      // Sprache und Länderformat


public class MedicationListActivity extends AppCompatActivity {     // erbt alle Funktionen

    ListView listMedications;
    Button buttonBack;
    BottomNavigationView bottomNavigation;

    ArrayList<String> medications;
    ArrayList<String> medicationIds;
    ArrayList<String> names, doses, times, stocks;

    ArrayAdapter<String> adapter;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override                                               // Wir überschreiben die Methode der Elternklasse
    protected void onCreate(Bundle savedInstanceState) {    // wird automatisch beim Öffnen der Activity aufgerufen,
        super.onCreate(savedInstanceState);                 // Elternklasse initialisieren
        EdgeToEdge.enable(this);        // Die Benutzeroberfläche darf den ganzen Bildschirm benutzen
        setContentView(R.layout.activity_medication_list);     // Zeigt das Layout activity_medication.xml an

        // Elemente finden (Java mit XML verbinden)
        listMedications = findViewById(R.id.listMedications);
        buttonBack = findViewById(R.id.buttonBack);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Listen vorbereiten (Leer)
        medications = new ArrayList<>();
        medicationIds = new ArrayList<>();
        names = new ArrayList<>();
        doses = new ArrayList<>();
        times = new ArrayList<>();
        stocks = new ArrayList<>();

        //ArrayList mit der ListView vorbereiten (this: aktuelle Activity, simple_list_item_1: eine Zeile pro Element, medications: ArrayList Daten)
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                medications
        );

        // Verbindet die ListView mit dem Adapter
        listMedications.setAdapter(adapter);

        // Firebase verbinden
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Medikament durch kurzen Klick bearbeiten
        listMedications.setOnItemClickListener((parent, view, position, id) -> { // parent: ListView, view: angeklicktes Element, position: Position des Elements, id: ID des Elements

            new AlertDialog.Builder(this)                            // Erstellt ein Dialogfenster
                    .setTitle("Einnahme")                                   // Titel des Dialogs
                    .setMessage("Medikament eingenommen?")                  // Nachricht anzeigen
                    .setPositiveButton("Ja", (dialog, which) -> // Ja-Button,  dialog: Dialogfenster, which: gedrückter Button
                            saveHistory(position))                                     // Speichert das Medikament im Verlauf

                    .setNegativeButton("Bearbeiten", (dialog, which) -> {   // Bearbeiten-Button

                        Intent intent = new Intent(                                               // Intent für EditMedicationActivity
                                MedicationListActivity.this,
                                EditMedicationActivity.class
                        );

                        intent.putExtra("id", medicationIds.get(position));         // Übergibt die ID an die EditActivity
                        intent.putExtra("name", names.get(position));
                        intent.putExtra("dose", doses.get(position));
                        intent.putExtra("time", times.get(position));
                        intent.putExtra("stock", stocks.get(position));

                        startActivity(intent);                                             // Öffnet EditMedicationActivity
                    })
                    .show();                                                                // Zeigt den Dialog an
        });

        // Medikament durch langen Klick löschen
        listMedications.setOnItemLongClickListener((parent, view, position, id) -> { // Reagiert auf einen langen Klick
            showDeleteDialog(position);         // Öffnet den Löschdialog
            return true;                        // Ereignis wurde verarbeitet
        });

        buttonBack.setOnClickListener(v -> finish()); // Schließt die aktuelle Activity

        // Passt den Abstand an die Systemleisten an (automatisch von Android Studio erstellt(
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars()); // Größe der Systemleisten

                    v.setPadding(                                    // Setzt die Abstände
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;                                 // Gibt die Insets zurück
                }
        );

        // Navigation zwischen den Hauptscreens
        bottomNavigation.setOnItemSelectedListener(item -> {

            Intent intent;

            // Prüfen, welches Menüelement ausgewählt wurde
            if (item.getItemId() == R.id.navHome) {
                intent = new Intent(this, HomeActivity.class);

            } else if (item.getItemId() == R.id.navMedications) {
                return true;            // Bereits auf MedicationsList → nichts machen

            } else if (item.getItemId() == R.id.navHistory) {
                intent = new Intent(this, HistoryActivity.class);

            } else if (item.getItemId() == R.id.navStock) {
                intent = new Intent(this, StockActivity.class);

            } else {
                intent = new Intent(this, SettingsActivity.class);
            }

            startActivity(intent);
            return true;
        });

    }

    @Override
    protected void onResume() {                         // (Vordefinierte) jedes Mal, wenn die MedicationList wieder sichtbar wird,
                                                        // loadMedications() wird aufgerufen
        super.onResume();

        // Liste nach dem Bearbeiten neu laden
        if (db != null && auth != null) {                // Prüfen, ob db und auth vorhanden sind
            loadMedications();
        }
    }

    // Medikamente aus Firestore laden
    private void loadMedications() {

        if (auth.getCurrentUser() == null) {                // Prüft, ob ein Benutzer angemeldet ist
            Toast.makeText(
                    this,
                    "Benutzer nicht angemeldet",
                    Toast.LENGTH_SHORT
            ).show();
            return;                                         // Methode beenden
        }

        String userId = auth.getCurrentUser().getUid();     // Benutzer-ID holen

        // Firestore-Pfad: users -> userId -> medications
        db.collection("users")
                .document(userId)
                .collection("medications")
                .get()                   // Liest die Medikamente aus Firestore
                .addOnSuccessListener(result -> {       // Wird bei erfolgreichem Laden ausgeführt

                    medications.clear();                 // Alte Medikamente löschen
                    medicationIds.clear();
                    names.clear();
                    doses.clear();
                    times.clear();
                    stocks.clear();

                    result.forEach(document -> {        // Geht durch alle Dokumente

                        String name = document.getString("name");       // Namen lesen
                        String dose = document.getString("dose");
                        String time = document.getString("time");
                        Long stock = document.getLong("stock");

                        // Erstellt den Text für die ListView bzw. Anzeige
                        String medication =
                                name + " | "
                                        + dose + " | "
                                        + time + " | Bestand: "
                                        + stock;

                        medications.add(medication);                // Zur Medikamentenliste hinzufügen
                        medicationIds.add(document.getId());        // Dokument-ID speichern

                        names.add(name);                            // Namen speichern
                        doses.add(dose);
                        times.add(time);
                        stocks.add(String.valueOf(stock));
                    });

                    adapter.notifyDataSetChanged();                 // Aktualisiert die ListView
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

        new AlertDialog.Builder(this)                    // Erstellt ein Dialogfenster
                .setTitle("Medikament löschen")
                .setMessage("Möchten Sie dieses Medikament wirklich löschen?")
                .setPositiveButton(                             // Ja-Button
                        "Ja",
                        (dialog, which) -> deleteMedication(position)  // Löscht das Medikament
                )
                .setNegativeButton("Nein", null)         // Nein-Button, Dialog schließen
                .show();                                             // Zeigt den Dialog an
    }

    // Medikament aus Firestore löschen
    private void deleteMedication(int position) {

        if (auth.getCurrentUser() == null) {        // Prüft, ob ein Benutzer angemeldet ist
            return;                                  // Methode beenden
        }

        // Benutzer-ID holen, Dokument-ID holen
        String userId = auth.getCurrentUser().getUid();
        String medicationId = medicationIds.get(position);

        //Pfad
        db.collection("users")
                .document(userId)
                .collection("medications")
                .document(medicationId)
                .delete()                                    // Löscht das Dokument aus Firestore

                .addOnSuccessListener(result -> {       // Wird nach erfolgreichem Löschen ausgeführt

                    medications.remove(position);           // Medikament aus der Liste löschen (ArrayList-Methode)
                    medicationIds.remove(position);
                    names.remove(position);
                    doses.remove(position);
                    times.remove(position);
                    stocks.remove(position);

                    adapter.notifyDataSetChanged();          // Aktualisiert die ListView

                    // Erfolgsmeldung anzeigen
                    Toast.makeText(
                            this,
                            "Medikament gelöscht",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                // Fehlermeldung anzeigen
                .addOnFailureListener(error ->
                        Toast.makeText(
                                this,
                                "Löschen fehlgeschlagen",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    // Einnahme im Verlauf speichern
    private void saveHistory(int position) {        // Parameter

        if (auth.getCurrentUser() == null) {        // Prüft, ob ein Benutzer angemeldet ist
            return;
        }

        String userId = auth.getCurrentUser().getUid();     // Benutzer-ID holen

        int oldStock = Integer.parseInt(stocks.get(position));  // Aktuellen Bestand lesen

        // Prüft, ob noch Bestand vorhanden ist
        if (oldStock <= 0) {
            Toast.makeText(
                    this,
                    "Kein Bestand mehr vorhanden",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        int newStock = oldStock - 1;                         // Bestand um 1 reduzieren

        // Erstellt eine Map für die Verlaufsdaten (Daten als Schlüssel-Wert-Paare zu speichern)
        Map<String, Object> history = new HashMap<>();             // (String = Schlüssel, Object = Wert)

        history.put("name", names.get(position));                   // Medikamentenname speichern

        // Aktuelles Datum und Uhrzeit erstellen
        String date = new SimpleDateFormat(
                "dd.MM.yyyy HH:mm",
                Locale.getDefault()
        ).format(new Date());

        history.put("date", date);                                  // Datum speichern

        // Zuerst Bestand reduzieren
        db.collection("users")
                .document(userId)
                .collection("medications")
                .document(medicationIds.get(position))
                .update("stock", newStock)          // Bestand in Firestore aktualisieren

                .addOnSuccessListener(result -> {    // Wird nach erfolgreichem Update ausgeführt

                    // Danach Einnahme im Verlauf speichern
                    db.collection("users")
                            .document(userId)
                            .collection("history")
                            .add(history)                  // Verlauf in Firestore speichern
                            .addOnSuccessListener(historyResult -> {        // Erfolgsmeldung anzeigen
                                Toast.makeText(
                                        this,
                                        "Einnahme gespeichert",
                                        Toast.LENGTH_SHORT
                                ).show();

                                loadMedications();                                              // Medikamente neu laden
                            });
                })
                // Fehlermeldung anzeigen
                .addOnFailureListener(error ->
                        Toast.makeText(
                                this,
                                "Bestand konnte nicht geändert werden",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

}