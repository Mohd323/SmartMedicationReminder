package de.hwr.smartmedicationreminder;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HistoryActivity extends AppCompatActivity {

    ListView listHistory;
    Button buttonBack;
    BottomNavigationView bottomNavigation;

    ArrayList<String> history;
    ArrayAdapter<String> adapter;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        listHistory = findViewById(R.id.listHistory);
        buttonBack = findViewById(R.id.buttonBack);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        history = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                history
        );

        listHistory.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadHistory();

        buttonBack.setOnClickListener(v -> finish());

        // Navigation zwischen den Hauptscreens
        bottomNavigation.setOnItemSelectedListener(item -> {

            Intent intent;

            if (item.getItemId() == R.id.navHome) {
                intent = new Intent(this, HomeActivity.class);

            } else if (item.getItemId() == R.id.navMedications) {
                intent = new Intent(this, MedicationListActivity.class);

            } else if (item.getItemId() == R.id.navHistory) {
                return true;

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

        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("history")
                .get()
                .addOnSuccessListener(result -> {

                    history.clear();

                    result.forEach(document -> {

                        String name = document.getString("name");
                        String date = document.getString("date");

                        history.add(name + " | " + date);
                    });

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(error ->
                        Toast.makeText(
                                this,
                                "Verlauf konnte nicht geladen werden",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}