package de.hwr.smartmedicationreminder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;

public class AddMedicationActivity extends AppCompatActivity {

    EditText editName, editDose, editTime, editStock;
    Button buttonSave, buttonBack;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_medication);

        // Benachrichtigungen erlauben
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
        
            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    100
            );
        }

        // Eingabefelder und Buttons finden
        editName = findViewById(R.id.editName);
        editDose = findViewById(R.id.editDose);
        editTime = findViewById(R.id.editTime);
        editStock = findViewById(R.id.editStock);

        buttonSave = findViewById(R.id.buttonSave);
        buttonBack = findViewById(R.id.buttonBack);

        // Firebase verbinden
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Medikament speichern
        buttonSave.setOnClickListener(v -> saveMedication());

        // Zur Home-Seite zurück
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(AddMedicationActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Medikament in Firestore speichern
    private void saveMedication() {

        String name = editName.getText().toString().trim();
        String dose = editDose.getText().toString().trim();
        String time = editTime.getText().toString().trim();
        String stockText = editStock.getText().toString().trim();

        // Prüfen ob alle Felder ausgefüllt sind
        if (name.isEmpty() || dose.isEmpty() || time.isEmpty() || stockText.isEmpty()) {
            Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
            return;
        }

        // Uhrzeit muss zum Beispiel 08:30 sein
        if (!time.matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            Toast.makeText(
                    this,
                    "Uhrzeit bitte als HH:mm eingeben",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        int stock = Integer.parseInt(stockText);

        Map<String, Object> medication = new HashMap<>();
        medication.put("name", name);
        medication.put("dose", dose);
        medication.put("time", time);
        medication.put("stock", stock);

        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("medications")
                .add(medication)
                .addOnSuccessListener(documentReference -> {

                    // Tägliche Erinnerung aktivieren
                    scheduleReminder(name, time);
                    Toast.makeText(this, "Medikament gespeichert", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Speichern fehlgeschlagen", Toast.LENGTH_SHORT).show()
                );
    }

    // Tägliche Erinnerung einstellen
private void scheduleReminder(String name, String time) {

    String[] parts = time.split(":");

    int hour = Integer.parseInt(parts[0]);
    int minute = Integer.parseInt(parts[1]);

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, hour);
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.SECOND, 0);

    // Wenn die Uhrzeit heute vorbei ist: morgen starten
    if (calendar.before(Calendar.getInstance())) {
        calendar.add(Calendar.DAY_OF_MONTH, 1);
    }

    Intent intent = new Intent(
            this,
            ReminderReceiver.class
    );

    intent.putExtra("name", name);

    PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this,
            name.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
                    | PendingIntent.FLAG_IMMUTABLE
    );

    AlarmManager alarmManager =
            (AlarmManager) getSystemService(ALARM_SERVICE);

    // Erinnerung jeden Tag wiederholen
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        && !alarmManager.canScheduleExactAlarms()) {

    startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
    Toast.makeText(this,
            "Alarme erlauben und Medikament danach erneut speichern",
            Toast.LENGTH_LONG).show();
    return;
    }
    
    try {
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    } catch (SecurityException e) {
        Toast.makeText(
                this,
                "Bitte Alarme und Erinnerungen erlauben",
                Toast.LENGTH_LONG
        ).show();
    }
    
}
}
