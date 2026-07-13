package de.hwr.smartmedicationreminder;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    Button buttonAddMedication,  buttonShowMedications, buttonStock, buttonHistory, buttonSettings;

    BottomNavigationView bottomNavigation;

    TextView textNextMedication, textStock;
    FirebaseFirestore db;
    FirebaseAuth auth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textNextMedication = findViewById(R.id.textNextMedication);
        textStock = findViewById(R.id.textStock);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Button finden
        buttonAddMedication = findViewById(R.id.buttonAddMedication);
        buttonShowMedications = findViewById(R.id.buttonShowMedications);
        buttonStock = findViewById(R.id.buttonStock);
        buttonHistory = findViewById(R.id.buttonHistory);
        buttonSettings = findViewById(R.id.buttonSettings);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        // Formular öffnen
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

        bottomNavigation.setOnItemSelectedListener(item -> {

            Intent intent;

            if (item.getItemId() == R.id.navHome) {
                return true;

            } else if (item.getItemId() == R.id.navMedications) {
                intent = new Intent(this, MedicationListActivity.class);

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
    protected void onResume() {
        super.onResume();

        if (db != null && auth != null) {
            loadDashboard();
        }
    }

    // Daten für den Home-Screen laden
private void loadDashboard() {

    if (auth.getCurrentUser() == null) {
        return;
    }

    String userId = auth.getCurrentUser().getUid();

    db.collection("users")
            .document(userId)
            .collection("medications")
            .get()
            .addOnSuccessListener(result -> {

                String currentTime = new SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                ).format(new Date());

                String nextName = null;
                String nextTime = null;

                String firstName = null;
                String firstTime = null;

                int lowStockCount = 0;

                for (var document : result) {

                    String name = document.getString("name");
                    String time = document.getString("time");
                    Long stock = document.getLong("stock");

                    if (time != null) {

                        // Früheste Einnahme insgesamt merken
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

                    if (stock != null && stock <= 5) {
                        lowStockCount++;
                    }
                }

                if (nextTime != null) {
                    textNextMedication.setText(
                            "Nächste Einnahme: " + nextName + " - " + nextTime
                    );
                } else if (firstTime != null) {
                    textNextMedication.setText(
                            "Nächste Einnahme: " + firstName
                                    + " - " + firstTime + " (morgen)"
                    );
                } else {
                    textNextMedication.setText(
                            "Nächste Einnahme: Keine Daten"
                    );
                }

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
