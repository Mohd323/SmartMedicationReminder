package de.hwr.smartmedicationreminder;

import android.os.Bundle;                       // Android-Objekt zum Speichern und Übergeben von Daten zwischen Activity
import android.content.Intent;                  // Zwischen Activity wechseln können
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;           // damit die App den gesamten Bildschirm nutzen kann
import androidx.appcompat.app.AppCompatActivity; // Android-Basisklasse für Activities. (Unsere Klasse als Bildschirm in der App anzeigen)
import androidx.core.graphics.Insets;            // Abstände der Systemleisten zu speichern
import androidx.core.view.ViewCompat;           // ViewCompat für zusätzliche Funktionen bei Views
import androidx.core.view.WindowInsetsCompat;   // für Informationen über Systemleisten
import com.google.android.material.bottomnavigation.BottomNavigationView;   // Navigation am unteren Bildschirmrand
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore; // für das Speichern und Lesen von Daten

import java.text.SimpleDateFormat;                      // um Datum und Uhrzeit zu formatieren
import java.util.Date;                                  // um die aktuelle Zeit und das aktuelle Datum zu bekommen
import java.util.Locale;                                // damit das Zeitformat zur Sprache und Region des Geräts passt

public class HomeActivity extends AppCompatActivity {   // erbt alle Funktionen

    Button buttonAddMedication,  buttonShowMedications, buttonStock, buttonHistory, buttonSettings;

    BottomNavigationView bottomNavigation;

    TextView textNextMedication, textStock;
    FirebaseFirestore db;
    FirebaseAuth auth;
    
    @Override                                                   // Wir überschreiben die Methode der Elternklasse
    protected void onCreate(Bundle savedInstanceState) {        // wird automatisch beim Öffnen der Activity aufgerufen, initialisieren die Benutzeroberfläche und unsere Variablen
        super.onCreate(savedInstanceState);                     // Elternklasse initialisieren

        // automatisch erstellt, sorgt dafür, dass die Benutzeroberfläche nicht von den Systemleisten verdeckt wird
        EdgeToEdge.enable(this);            // Die Benutzeroberfläche darf den ganzen Bildschirm benutzen
        setContentView(R.layout.activity_home);                  // Zeigt das Layout activity_home.xml an
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {  // Verhindert, dass Inhalte hinter Systemleisten verschwinden
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());             // Abstände der oberen und unteren Systemleisten holen
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);      // Padding passend zu den Systemleisten setzen
            return insets;
        });

        // TextViews-Elemente aus dem XML finden (Java mit XML verbinden)
        textNextMedication = findViewById(R.id.textNextMedication);
        textStock = findViewById(R.id.textStock);

        db = FirebaseFirestore.getInstance();                 // Firestore-Instanz holen
        auth = FirebaseAuth.getInstance();                    // Verbindung zu Firebase Authentication

        // Buttons aus XML mit Java-Variablen verbinden
        buttonAddMedication = findViewById(R.id.buttonAddMedication);
        buttonShowMedications = findViewById(R.id.buttonShowMedications);
        buttonStock = findViewById(R.id.buttonStock);
        buttonHistory = findViewById(R.id.buttonHistory);
        buttonSettings = findViewById(R.id.buttonSettings);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        // Formular-ADD öffnen
        buttonAddMedication.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddMedicationActivity.class);
            startActivity(intent);
        });

        // Medikamentenliste öffnen
        buttonShowMedications.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MedicationListActivity.class);
            startActivity(intent);
        });

        // Bestand-Screen öffnen
        buttonStock.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, StockActivity.class);
            startActivity(intent);
        });

        // Verlauf öffnen
        buttonHistory.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // Einstellungen öffnen
        buttonSettings.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Navigation aktivieren und Klicks erkennen
        bottomNavigation.setOnItemSelectedListener(item -> {

            // Variable deklarieren
            Intent intent;

            // Prüfen, welches Menüelement ausgewählt wurde
            if (item.getItemId() == R.id.navHome) {
                return true;                    // Bereits auf Home → nichts machen

            } else if (item.getItemId() == R.id.navMedications) {
                intent = new Intent(this, MedicationListActivity.class);

            } else if (item.getItemId() == R.id.navHistory) {
                intent = new Intent(this, HistoryActivity.class);

            } else if (item.getItemId() == R.id.navStock) {
                intent = new Intent(this, StockActivity.class);

            } else {
                intent = new Intent(this, SettingsActivity.class);
            }

            startActivity(intent);           // Ausgewählte Activity öffnen
            return true;                     // Ereignis wurde erfolgreich verarbeitet
        });


    }

    @Override
    protected void onResume() {   // (Vordefinierte) jedes Mal, wenn die HomeActivity wieder sichtbar wird,
                                  // loadDashboard() wird aufgerufen und die Daten aktualisiert werden
        super.onResume();
        // Prüfen, ob db und auth vorhanden sind
        if (db != null && auth != null) {
            loadDashboard();                    // Unsere Methode zum Aktualisieren der Home-Daten
        }
    }

    // Daten für den Home-Screen laden
private void loadDashboard() {

    if (auth.getCurrentUser() == null) {        // Prüfen, ob ein Benutzer angemeldet ist
        return;                                  // Methode beenden, wenn niemand angemeldet ist
    }

    // Aktuelle Benutzer-ID aus Firebase holen
    String userId = auth.getCurrentUser().getUid();


    // Firestore-Pfad: users -> userId -> medications
    db.collection("users")
            .document(userId)
            .collection("medications")
            .get()                                  // Alle Medikamente des Benutzers laden
            .addOnSuccessListener(result -> {   // wenn das Laden erfolgreich ist

                // Aktuelle Uhrzeit im Format HH:mm erstellen
                String currentTime = new SimpleDateFormat(  // Formatieren der Zeit
                        "HH:mm",                             // Format: Stunde:Minute
                        Locale.getDefault()                  // Sprache des Geräts
                ).format(new Date());                       // Aktuelle Uhrzeit holen

                // Variablen (werden später gefüllt)
                String nextName = null;
                String nextTime = null;

                String firstName = null;
                String firstTime = null;

                int lowStockCount = 0;

                // for-Schleife für alle Medikamente aus Firestore (result)
                // var = Datentyp wird automatisch bestimmt
                for (var document : result) {

                    String name = document.getString("name");               // Name aus Firestore lesen
                    String time = document.getString("time");                   // Uhrzeit aus Firestore lesen
                    Long stock = document.getLong("stock");                 // Bestand aus Firestore lesen

                    // Prüfen, ob das Medikament eine Uhrzeit hat
                    if (time != null) {

                        // Wenn firstTime leer ist oder eine frühere Uhrzeit gefunden wurde
                        if (firstTime == null || time.compareTo(firstTime) < 0) {
                            firstTime = time;
                            firstName = name;
                        }

                        // Nächste noch kommende Einnahme heute
                        if (time.compareTo(currentTime) >= 0
                                && (nextTime == null || time.compareTo(nextTime) < 0)) {

                            nextTime = time;
                            nextName = name;
                        }
                    }
                    // Prüfen, ob der Bestand niedrig ist
                    if (stock != null && stock <= 5) {
                        lowStockCount++;        // Zähler um 1 erhöhen
                    }
                }

                // Nächste Einnahme anzeigen, setText vordefinierte Methode von TextView
                if (nextTime != null) {
                    textNextMedication.setText(
                            "Nächste Einnahme: " + nextName + " - " + nextTime
                    );

                } else if (firstTime != null) {                 // Sonst erste Einnahme morgen anzeigen
                    textNextMedication.setText(
                            "Nächste Einnahme: " + firstName
                                    + " - " + firstTime + " (morgen)"
                    );
                } else {                                        // Keine Daten vorhanden
                    textNextMedication.setText(
                            "Nächste Einnahme: Keine Daten"
                    );
                }

                // Bestandswarnungen anzeigen
                if (lowStockCount == 0) {
                    textStock.setText("Bestandswarnungen: Keine");
                } else {
                    textStock.setText(
                            "Bestandswarnungen: "
                                    + lowStockCount + " Medikament(e)"
                    );
                }
            });
}

}
