package com.example.mobile_phone_subscription;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

public class PlanEditActivity extends AppCompatActivity {
    private static final String LOG_TAG = PlanEditActivity.class.getName();

    private EditText editTextName;
    private EditText editTextDetails;
    private EditText editTextPrice;
    private EditText editTextDescription;
    private Button buttonSave;
    private Button buttonCancel;
    private FirebaseFirestore firestore;
    private String planId;
    private boolean isNewPlan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_plan_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            // Inicializálás
            editTextName = findViewById(R.id.editTextPlanName);
            editTextDetails = findViewById(R.id.editTextPlanDetails);
            editTextPrice = findViewById(R.id.editTextPlanPrice);
            editTextDescription = findViewById(R.id.editTextPlanDescription);
            buttonSave = findViewById(R.id.buttonSavePlan);
            buttonCancel = findViewById(R.id.buttonCancelEdit);

            firestore = FirebaseFirestore.getInstance();

            // Adatok lekérése az intentből
            Intent intent = getIntent();
            if (intent != null) {
                planId = intent.getStringExtra("PLAN_ID");
                String name = intent.getStringExtra("PLAN_NAME");
                String details = intent.getStringExtra("PLAN_DETAILS");
                int price = intent.getIntExtra("PLAN_PRICE", 0);
                String description = intent.getStringExtra("PLAN_DESCRIPTION");

                isNewPlan = intent.getBooleanExtra("IS_NEW_PLAN", false);

                if (!isNewPlan && name != null) {
                    // Szerkesztési mód
                    editTextName.setText(name);
                    editTextDetails.setText(details);
                    editTextPrice.setText(String.valueOf(price));
                    editTextDescription.setText(description);
                }
            }

            // Gombok eseménykezelése
            buttonSave.setOnClickListener(view -> savePlan());
            buttonCancel.setOnClickListener(view -> goToShopping());

            return insets;
        });
    }

    private void savePlan() {
        String name = editTextName.getText().toString().trim();
        String details = editTextDetails.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (name.isEmpty() || details.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Minden mezőt ki kell tölteni!", Toast.LENGTH_SHORT).show();
            return;
        }

        int price;
        try {
            price = Integer.parseInt(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Érvénytelen ár formátum!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isNewPlan) {
            // Új csomag létrehozása
            Plan newPlan = new Plan(name, details, price, "");  // Üres image URL egyelőre
            newPlan.setDescription(description);

            firestore.collection("plans")
                    .add(newPlan)
                    .addOnSuccessListener(documentReference -> {
                        String id = documentReference.getId();
                        documentReference.update("id", id);

                        Toast.makeText(PlanEditActivity.this, "Új csomag sikeresen létrehozva!", Toast.LENGTH_SHORT).show();
                        goToShopping();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(LOG_TAG, "Hiba az új csomag létrehozásakor", e);
                        Toast.makeText(PlanEditActivity.this, "Nem sikerült létrehozni: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else if (planId != null && !planId.isEmpty()) {
            // Meglévő csomag frissítése
            firestore.collection("plans").document(planId)
                    .update(
                            "name", name,
                            "details", details,
                            "price", price,
                            "description", description
                    )
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(PlanEditActivity.this, "Csomag sikeresen frissítve!", Toast.LENGTH_SHORT).show();
                        goToShopping();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(LOG_TAG, "Hiba a csomag frissítése során", e);
                        Toast.makeText(PlanEditActivity.this, "Nem sikerült frissíteni: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void goToShopping() {
        Intent intent = new Intent(this, Shopping.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(PlanEditActivity.this);
        startActivity(intent, options.toBundle());
        finish();
    }
}