package com.example.mobile_phone_subscription;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Shopping extends AppCompatActivity {
    private static final String LOG_TAG = Shopping.class.getName();
    private FirebaseUser user;
    private boolean isAdmin = false;
    private RecyclerView recyclerViewPlans;
    private Button buttonPurchase;
    private Button buttonAddNewPlan;
    private List<Plan> planList;
    private PlanAdapter planAdapter;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping);

        // Toolbar inicializálása
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View linearLayout = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(linearLayout, (v, insets) -> {
            Insets systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            layoutParams.topMargin = systemBarsInsets.top;
            v.setLayoutParams(layoutParams);
            return insets;
        });

        // Nézetek inicializálása
        recyclerViewPlans = findViewById(R.id.recyclerViewPlans);
        buttonPurchase = findViewById(R.id.buttonPurchase);
        buttonAddNewPlan = findViewById(R.id.buttonAddNewPlan);

        // Firebase inicializálása
        firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Események kezelése
        buttonPurchase.setOnClickListener(view -> purchaseSelectedPlan());
        buttonAddNewPlan.setOnClickListener(view -> openPlanEditor());

        // Alapértelmezett állapot beállítása
        planList = new ArrayList<>();
        boolean isAnonymous = (user == null || user.isAnonymous());
        planAdapter = new PlanAdapter(planList, isAnonymous, isAdmin);
        recyclerViewPlans.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPlans.setAdapter(planAdapter);

        // Admin státusz ellenőrzése
        checkUserStatus();

        // Adatok betöltése
        loadPlansFromFirestore();
    }

    public void Logout(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Shopping.this);
        startActivity(intent, options.toBundle());
        finish();
    }

    public void openLoginPage(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Shopping.this);
        startActivity(intent, options.toBundle());
    }

    public void openProfilePage(View view) {
        Intent intent = new Intent(this, Profile.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Shopping.this);
        startActivity(intent, options.toBundle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shop_list_menu, menu);
        menu.findItem(R.id.shop_button).setVisible(false);
        if (user != null && user.isAnonymous()) {
            menu.findItem(R.id.profile_button).setVisible(false);
            menu.findItem(R.id.logout_button).setVisible(false);
        } else {
            menu.findItem(R.id.to_login_button).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout_button) {
            Logout(null);
            return true;
        } else if (id == R.id.profile_button) {
            openProfilePage(null);
            return true;
        } else if (id == R.id.to_login_button) {
            openLoginPage(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        if (user != null && !user.isAnonymous()) {
            // Admin jogosultság ellenőrzése
            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User userProfile = documentSnapshot.toObject(User.class);
                            if (userProfile != null) {
                                isAdmin = userProfile.admin;
                            }
                        }
                        updateUIForUserRole();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(LOG_TAG, "Hiba a felhasználói adatok lekérésekor", e);
                        updateUIForUserRole();
                    });
        } else {
            isAdmin = false;
            updateUIForUserRole();
        }
    }

    private void updateUIForUserRole() {
        // "Új csomag hozzáadása" gomb csak adminoknak
        buttonAddNewPlan.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        // "Vásárlás" gomb csak nem-admin, bejelentkezett felhasználóknak
        boolean isAnonymous = (user == null || user.isAnonymous());
        buttonPurchase.setVisibility(!isAdmin && !isAnonymous ? View.VISIBLE : View.GONE);

        // Adapter frissítése a jogosultságok alapján
        planAdapter = new PlanAdapter(planList, isAnonymous, isAdmin);
        recyclerViewPlans.setAdapter(planAdapter);
    }

    private void loadPlansFromFirestore() {
        firestore.collection("plans")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    planList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(LOG_TAG, "Nincsenek termékek a Firestore-ban");
                        addDefaultPlansToFirestore();
                    } else {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Plan plan = document.toObject(Plan.class);
                            plan.setId(document.getId());
                            Log.d(LOG_TAG, "Plan betöltve ID-val: " + plan.getId());
                            planList.add(plan);
                        }
                        planAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Error loading plans", e);
                    Toast.makeText(Shopping.this, "Hiba történt a termékek betöltésekor: "
                            + e.getMessage(), Toast.LENGTH_SHORT).show();
                    addDefaultPlansToAdapter();
                });
    }
    private void addDefaultPlansToFirestore() {
        List<Plan> defaultPlans = getDefaultPlans();

        for (Plan plan : defaultPlans) {
            firestore.collection("plans")
                    .add(plan)
                    .addOnSuccessListener(documentReference -> {
                        plan.setId(documentReference.getId());
                        documentReference.update("id", plan.getId());
                        planList.add(plan);
                        planAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(LOG_TAG, "Error adding default plan", e);
                    });
        }
    }
    // Alapértelmezett termékek hozzáadása az adapterhez (Firestore hiba esetén)
    private void addDefaultPlansToAdapter() {
        planList.clear();
        List<Plan> defaultPlans = getDefaultPlans();
        for (int i = 0; i < defaultPlans.size(); i++) {
            Plan plan = defaultPlans.get(i);
            plan.setId("local_" + i);
            planList.add(plan);
        }
        planAdapter.notifyDataSetChanged();
    }

    // Alapértelmezett termékek létrehozása
    private List<Plan> getDefaultPlans() {
        List<Plan> defaultPlans = new ArrayList<>();
        defaultPlans.add(new Plan("Alap internet+telefon csomag", "10GB adat, 100 perc telefonbeszélgetés", 4500, ""));
        defaultPlans.add(new Plan("Standard internet+telefon csomag", "20GB adat, 200 perc telefonbeszélgetés", 7000, ""));
        defaultPlans.add(new Plan("Prémium internet+telefon csomag", "50GB adat, korlátlan telefonbeszélgetés", 9000, ""));
        defaultPlans.add(new Plan("Korlátlan internet+telefon csomag", "Korlátlan adat, korlátlan telefonbeszélgetés", 11000, ""));
        defaultPlans.add(new Plan("Csak telefon alap csomag", "60 perc telefonbeszélgetés", 2000, ""));
        defaultPlans.add(new Plan("Csak telefon standard csomag", "10 óra telefonbeszélgetés", 4000, ""));
        defaultPlans.add(new Plan("Csak telefon prémium csomag", "40 óra telefonbeszélgetés", 5000, ""));
        defaultPlans.add(new Plan("Csak telefon korlátlan csomag", "korlátlan telefonbeszélgetés", 6500, ""));
        return defaultPlans;
    }

    private void checkAdminStatus() {
        if (user != null && !user.isAnonymous()) {
            firestore.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User userProfile = documentSnapshot.toObject(User.class);
                            if (userProfile != null) {
                                isAdmin = userProfile.admin;
                                setupRecyclerView();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(LOG_TAG, "Admin státusz lekérési hiba", e);
                        setupRecyclerView();
                    });
        } else {
            setupRecyclerView();
        }
    }

    private void setupRecyclerView() {
        recyclerViewPlans = findViewById(R.id.recyclerViewPlans);
        planList = new ArrayList<>();
        planAdapter = new PlanAdapter(planList, user.isAnonymous(), isAdmin);
        recyclerViewPlans.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPlans.setAdapter(planAdapter);
        loadPlansFromFirestore();
    }
    private void openPlanEditor() {
        Intent intent = new Intent(this, PlanEditActivity.class);
        intent.putExtra("IS_NEW_PLAN", true);
        startActivity(intent);
    }
    private void purchaseSelectedPlan() {
        Plan selectedPlan = planAdapter.getSelectedPlan();
        if (selectedPlan != null) {
            Intent intent = new Intent(this, PlanInfoActivity.class);
            intent.putExtra("PLAN_ID", selectedPlan.getId());
            intent.putExtra("PLAN_NAME", selectedPlan.getName());
            intent.putExtra("PLAN_DETAILS", selectedPlan.getDetails());
            intent.putExtra("PLAN_PRICE", selectedPlan.getPrice());
            intent.putExtra("PLAN_DESCRIPTION", selectedPlan.getDescription());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Kérlek válassz ki egy csomagot előbb!", Toast.LENGTH_SHORT).show();
        }
    }
}