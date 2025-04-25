package com.example.mobile_phone_subscription;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PlanInfoActivity extends AppCompatActivity {
    private static final String LOG_TAG = PlanInfoActivity.class.getName();
    private TextView textViewPlanName, textViewPlanDetails, textViewPlanPrice, textViewPlanDescription;
    private Button buttonBack, buttonPurchase;
    private FirebaseAuth mAuth;
    private String planName, planId;
    private static final int NOTIFICATION_PERMISSION_CODE = 100;


    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_plan_info);

        initializeViews();

        mAuth = FirebaseAuth.getInstance();

        // Kezeljük a felhasználói státuszt
        checkUserStatus();

        // Adatok lekérése az intentből
        Intent intent = getIntent();
        getDataFromIntent(intent);

        // Gomb eseménykezelők beállítása
        buttonBack.setOnClickListener(view -> goToShopping());
        buttonPurchase.setOnClickListener(view -> purchase());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null || user.isAnonymous()) {
            // Anonim felhasználók esetén elrejtjük a vásárlás gombot
            buttonPurchase.setVisibility(View.GONE);
        } else {
            // Regisztrált felhasználók esetén ellenőrizzük, hogy admin-e
            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User userProfile = documentSnapshot.toObject(User.class);
                            if (userProfile != null && userProfile.admin) {
                                // Admin esetén elrejtjük a vásárlás gombot
                                buttonPurchase.setVisibility(View.GONE);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(LOG_TAG, "Hiba a felhasználó adatainak lekérésekor", e);
                    });
        }
    }

    private void purchase() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null && !user.isAnonymous()) {
            // Megvásárlás logika
            Toast.makeText(PlanInfoActivity.this,
                    "Sikeresen megvásároltad: " + planName,
                    Toast.LENGTH_SHORT).show();

            // Értesítés küldése a vásárlásról
            sendPurchaseNotification(planName);
            finish();
        } else {
            Toast.makeText(PlanInfoActivity.this,
                    "Kérlek jelentkezz be a vásárláshoz!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void sendPurchaseNotification(String planName) {
        // Ellenőrizzük és kérjük az engedélyt, ha szükséges
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
                return;
            }
        }

        // Notification csatorna létrehozása (Android 8.0+)
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "purchase_channel",
                    "Vásárlási értesítések",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Értesítés létrehozása
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "purchase_channel")
                .setSmallIcon(R.drawable.ic_shop)
                .setContentTitle("Sikeres előfizetés")
                .setContentText("Sikeresen előfizettél a következő csomagra: " + planName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }

    private void goToShopping() {
        Intent intent = new Intent(this, Shopping.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(PlanInfoActivity.this);
        startActivity(intent, options.toBundle());
        finish();
    }

    /**
     * Inicializálja az oldalon lévő elemeket
     */
    private void initializeViews() {
        textViewPlanName = findViewById(R.id.textViewPlanName);
        textViewPlanDetails = findViewById(R.id.textViewPlanDetails);
        textViewPlanPrice = findViewById(R.id.textViewPlanPrice);
        textViewPlanDescription = findViewById(R.id.textViewPlanDescription);
        buttonBack = findViewById(R.id.buttonBack);
        buttonPurchase = findViewById(R.id.buttonPurchase);
    }

    /**
     * Lekéri az adatokat a paraméterben kapott Intentből
     */
    @SuppressLint("DefaultLocale")
    private void getDataFromIntent(Intent intent){
        if (intent != null) {
            planId = intent.getStringExtra("PLAN_ID");
            planName = intent.getStringExtra("PLAN_NAME");
            String details = intent.getStringExtra("PLAN_DETAILS");
            int price = intent.getIntExtra("PLAN_PRICE", 0);
            String description = intent.getStringExtra("PLAN_DESCRIPTION");

            textViewPlanName.setText(planName);
            textViewPlanDetails.setText(details);
            textViewPlanPrice.setText(String.format("%d Ft/hó", price));
            textViewPlanDescription.setText(description);
        }
    }
}