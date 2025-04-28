package com.example.mobile_phone_subscription;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Csomag információs Activity osztály.
 * Megjeleníti a csomag részleteit és lehetőséget biztosít a vásárlásra.
 */
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

        ViewInsetsHelper.setupScrollableLayoutInsets(findViewById(R.id.main));
    }

    /**
     * Ellenőrzi a felhasználó státuszát (anonim vagy regisztrált, vagy admin)
     * és ennek megfelelően kezeli a vásárlás gomb láthatóságát.
     * Ha a felhasználó anonim, vagy admin, akkor elrejti a vásárlás gombot, egyébként megjeleníti.
     */
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

    /**
     * Vásárlás gomb eseménykezelője
     */
    private void purchase() {
        DialogHelper.showPurchaseConfirmationDialog(this, planName, () -> {
            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null && !user.isAnonymous()) {
                int price = getIntent().getIntExtra("PLAN_PRICE", 0);

                FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                        .update(
                                "subscriptionId", planId,
                                "subscriptionName", planName,
                                "subscriptionPrice", price
                        )
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(PlanInfoActivity.this,
                                    "Sikeresen megvásároltad: " + planName,
                                    Toast.LENGTH_SHORT).show();

                            if (NotificationHelper.hasNotificationPermission(this)) {
                                NotificationHelper.sendPurchaseNotification(this, planName);

                                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                                        alarmManager.canScheduleExactAlarms()) {
                                    SubscriptionReminder.setReminderAlarm(this, planName);
                                } else {
                                    Log.d(LOG_TAG, "Exact alarm permission needed");
                                }

                                goToProfile();
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                        NOTIFICATION_PERMISSION_CODE);
                            }
                            FirebaseFirestore.getInstance().collection("plans").document(planId)
                                    .update("subscribers", FieldValue.increment(1));
                        })
                        .addOnFailureListener(e -> {
                            Log.e(LOG_TAG, "Hiba az előfizetés mentésekor", e);
                            Toast.makeText(PlanInfoActivity.this,
                                    "Hiba történt a vásárlás mentésekor: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(PlanInfoActivity.this,
                        "Kérlek jelentkezz be a vásárláshoz!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Visszalép a főoldalra
     */
    private void goToShopping() {
        NavigationHelper.toShoppingWithFade(PlanInfoActivity.this);
        finish();
    }

    /**
     * Navigál a profil oldalra
     */
    private void goToProfile() {
        NavigationHelper.toProfile(PlanInfoActivity.this);
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
    private void getDataFromIntent(Intent intent) {
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