package com.example.mobile_phone_subscription;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PlanInfoActivity extends AppCompatActivity {
    private static final String LOG_TAG = PlanInfoActivity.class.getName();
    private TextView textViewPlanName;
    private TextView textViewPlanDetails;
    private TextView textViewPlanPrice;
    private TextView textViewPlanDescription;
    private Button buttonBack;
    private Button buttonPurchase;
    private FirebaseAuth mAuth;
    private String planName;
    private String planId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_plan_info);

        // Inicializáljuk a nézeteket
        textViewPlanName = findViewById(R.id.textViewPlanName);
        textViewPlanDetails = findViewById(R.id.textViewPlanDetails);
        textViewPlanPrice = findViewById(R.id.textViewPlanPrice);
        textViewPlanDescription = findViewById(R.id.textViewPlanDescription);
        buttonBack = findViewById(R.id.buttonBack);
        buttonPurchase = findViewById(R.id.buttonPurchase);

        mAuth = FirebaseAuth.getInstance();

        // Kezeljük a felhasználói státuszt
        checkUserStatus();

        // Adatok lekérése az intentből
        Intent intent = getIntent();
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
            Toast.makeText(PlanInfoActivity.this,
                    "Sikeresen megvásároltad: " + planName,
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(PlanInfoActivity.this,
                    "Kérlek jelentkezz be a vásárláshoz!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void goToShopping() {
        Intent intent = new Intent(this, Shopping.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(PlanInfoActivity.this);
        startActivity(intent, options.toBundle());
        finish();
    }
}