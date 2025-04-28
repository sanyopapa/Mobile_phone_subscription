package com.example.mobile_phone_subscription;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Csomag szerkesztéséért felelős Activity osztály.
 * Lehetővé teszi új csomag létrehozását vagy meglévő csomagok szerkesztését.
 */
public class PlanEditActivity extends AppCompatActivity {
    private static final String LOG_TAG = PlanEditActivity.class.getName();
    private EditText editTextName, editTextDetails, editTextPrice, editTextDescription;
    private Button buttonSave, buttonCancel;
    private FirebaseFirestore firestore;
    private String planId;
    private boolean isNewPlan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_plan_edit);

        // ViewInsetsHelper használata az egységes megjelenítéshez
        ViewInsetsHelper.setupScrollableLayoutInsets(findViewById(R.id.main));

        // Firestore inicializálása
        firestore = FirebaseFirestore.getInstance();

        // A nézetek inicializálása
        initializeViews();

        // Adatok betöltése (szerkesztés esetén)
        loadPlanData();

        // Eseménykezelők beállítása
        setupEventListeners();
    }

    /**
     * Inicializálja az oldalon lévő elemeket
     */
    private void initializeViews() {
        editTextName = findViewById(R.id.editTextPlanName);
        editTextDetails = findViewById(R.id.editTextPlanDetails);
        editTextPrice = findViewById(R.id.editTextPlanPrice);
        editTextDescription = findViewById(R.id.editTextPlanDescription);
        buttonSave = findViewById(R.id.buttonSavePlan);
        buttonCancel = findViewById(R.id.buttonCancelEdit);
    }

    /**
     * Betölti a szerkesztendő csomag adatait, ha van ilyen
     */
    private void loadPlanData() {
        Intent intent = getIntent();
        isNewPlan = intent.getBooleanExtra("IS_NEW_PLAN", false);

        if (intent.hasExtra("PLAN_ID")) {
            // A szerkesztendő csomag adatainak betöltése
            planId = intent.getStringExtra("PLAN_ID");
            String planName = intent.getStringExtra("PLAN_NAME");
            String planDetails = intent.getStringExtra("PLAN_DETAILS");
            int planPrice = intent.getIntExtra("PLAN_PRICE", 0);
            String planDescription = intent.getStringExtra("PLAN_DESCRIPTION");

            editTextName.setText(planName);
            editTextDetails.setText(planDetails);
            editTextPrice.setText(String.valueOf(planPrice));
            editTextDescription.setText(planDescription);

            Log.d(LOG_TAG, "Csomag adatai betöltve: " + planId);
        } else {
            Log.d(LOG_TAG, "Új csomag létrehozása");
        }
    }

    /**
     * Beállítja a gombok eseménykezelőit
     */
    private void setupEventListeners() {
        buttonSave.setOnClickListener(view -> {
            Log.d(LOG_TAG, "Mentés gomb megnyomva");
            savePlan();
        });

        buttonCancel.setOnClickListener(view -> {
            Log.d(LOG_TAG, "Mégse gomb megnyomva");
            NavigationHelper.toShoppingWithFade(PlanEditActivity.this);
            finish();
        });
    }

    /**
     * Menti a csomag adatait a Firestore-ba
     */
    private void savePlan() {
        // Adatok összegyűjtése
        String name = editTextName.getText().toString().trim();
        String details = editTextDetails.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        // Ellenőrzés
        if (name.isEmpty() || details.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Kérlek töltsd ki az összes kötelező mezőt!", Toast.LENGTH_SHORT).show();
            return;
        }

        int price;
        try {
            price = Integer.parseInt(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Az ár csak szám lehet!", Toast.LENGTH_SHORT).show();
            return;
        }

        DialogHelper.showSavePlanDialog(this, () -> {
            // Csomag objektum létrehozása
            Plan plan = new Plan(name, details, price, "");
            plan.setDescription(description);

            // Mentés a Firestore-ba
            if (planId != null && !isNewPlan) {
                // Meglévő csomag frissítése
                firestore.collection("plans").document(planId)
                        .update(
                                "name", name,
                                "details", details,
                                "price", price,
                                "description", description
                        )
                        .addOnSuccessListener(aVoid -> {
                            Log.d(LOG_TAG, "Csomag sikeresen frissítve!");
                            Toast.makeText(PlanEditActivity.this, "Csomag sikeresen frissítve!", Toast.LENGTH_SHORT).show();
                            NavigationHelper.toShoppingWithFade(PlanEditActivity.this);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(LOG_TAG, "Hiba a csomag frissítésekor", e);
                            Toast.makeText(PlanEditActivity.this, "Hiba történt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Új csomag hozzáadása
                firestore.collection("plans")
                        .add(plan)
                        .addOnSuccessListener(documentReference -> {
                            String id = documentReference.getId();
                            documentReference.update("id", id);
                            Log.d(LOG_TAG, "Csomag sikeresen hozzáadva ID-val: " + id);
                            Toast.makeText(PlanEditActivity.this, "Csomag sikeresen hozzáadva!", Toast.LENGTH_SHORT).show();
                            NavigationHelper.toShoppingWithFade(PlanEditActivity.this);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(LOG_TAG, "Hiba a csomag hozzáadásakor", e);
                            Toast.makeText(PlanEditActivity.this, "Hiba történt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}