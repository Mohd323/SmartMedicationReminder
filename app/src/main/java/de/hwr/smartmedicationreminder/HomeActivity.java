package de.hwr.smartmedicationreminder;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    Button buttonAddMedication,  buttonShowMedications, buttonStock, buttonHistory, buttonSettings;

    BottomNavigationView bottomNavigation;
    
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
}
