package com.example.mobile_phone_subscription;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Shopping extends AppCompatActivity {
    private static final String LOG_TAG = Shopping.class.getName();
    private FirebaseUser user;
    private boolean isAdmin = false;
    private RecyclerView recyclerViewPlans;
    private Button buttonPurchase, buttonAddNewPlan;
    private List<Plan> planList;
    private PlanAdapter planAdapter;
    private EditText editTextSearch, editTextMinPrice, editTextMaxPrice;
    private Spinner spinnerType;
    private Button buttonApplyFilters;
    private FirebaseFirestore firestore;
    private boolean firstResume = true;
    private PlanFilterHelper planFilterHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping);

        // Toolbar inicializálása
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewInsetsHelper.setupScrollableLayoutInsets(findViewById(R.id.main));

        initializeViews();

        // Firebase inicializálása
        firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Események kezelése
        buttonPurchase.setOnClickListener(view -> purchaseSelectedPlan());
        buttonAddNewPlan.setOnClickListener(view -> openPlanEditor());

        setupRecyclerView();

        // Admin státusz ellenőrzése
        checkUserStatus();

        // Adatok betöltése
        loadPlansFromFirestore();

        buttonApplyFilters.setOnClickListener(v -> applyFilters());
        spinnerType = findViewById(R.id.spinnerType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.plan_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        planFilterHelper = new PlanFilterHelper();

        firstResume = true;
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (firstResume) {
            firstResume = false;
            return;
        }
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && user != null) {
            if (currentUser.getUid().equals(user.getUid())) {
                // Ugyanaz a felhasználó, csak frissítjük az adatokat
                loadPlansFromFirestore();
            } else {
                // Más felhasználó jelentkezett be, frissítsük a teljes állapotot
                user = currentUser;
                checkUserStatus();
            }
        } else if (currentUser == null && user != null) {
            // Kijelentkezés történt, frissítsük az állapotot
            user = null;
            isAdmin = false;
            updateUIForUserRole();
            loadPlansFromFirestore();
        } else {
            loadPlansFromFirestore();
        }
        invalidateOptionsMenu();
    }
    public void Logout(View view) {
        FirebaseAuth.getInstance().signOut();
        openLoginPage(view);
        finish();
    }
    public void openLoginPage(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        ActivityOptions options = ActivityOptions.makeCustomAnimation(
                this, R.anim.fade_in, R.anim.fade_out);
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
    private void setupRecyclerView() {
        recyclerViewPlans = findViewById(R.id.recyclerViewPlans);
        planList = new ArrayList<>();
        planAdapter = new PlanAdapter(planList, user.isAnonymous(), isAdmin);
        recyclerViewPlans.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPlans.setAdapter(planAdapter);
    }
    private void openPlanEditor() {
        Intent intent = new Intent(this, PlanEditActivity.class);
        intent.putExtra("IS_NEW_PLAN", true);
        startActivity(intent);
    }

    /**
     * Inicializálja az oldalon lévő elemeket
     */
    private void initializeViews(){
        recyclerViewPlans = findViewById(R.id.recyclerViewPlans);
        buttonPurchase = findViewById(R.id.buttonPurchase);
        buttonAddNewPlan = findViewById(R.id.buttonAddNewPlan);
        editTextSearch = findViewById(R.id.editTextSearch);
        editTextMinPrice = findViewById(R.id.editTextMinPrice);
        editTextMaxPrice = findViewById(R.id.editTextMaxPrice);
        spinnerType = findViewById(R.id.spinnerType);
        buttonApplyFilters = findViewById(R.id.buttonApplyFilters);
    }

    /**
     * Szűrők alkalmazása a csomagok listájára
     */

    private void applyFilters() {
        String searchQuery = editTextSearch.getText().toString().trim();
        String selectedType = spinnerType.getSelectedItem() != null ? spinnerType.getSelectedItem().toString() : "Népszerűek";
        String minPriceText = editTextMinPrice.getText().toString().trim();
        String maxPriceText = editTextMaxPrice.getText().toString().trim();

        int minPrice = minPriceText.isEmpty() ? 0 : Integer.parseInt(minPriceText);
        int maxPrice = maxPriceText.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(maxPriceText);

        Query query;

        if (!searchQuery.isEmpty()) {
            query = planFilterHelper.filterByNameWithLimit(searchQuery, 50);
        } else if (!minPriceText.isEmpty() || !maxPriceText.isEmpty()) {
            query = planFilterHelper.filterByPriceRangeWithLimit(minPrice, maxPrice, 50);
        } else if (selectedType.equals("Népszerűek")) {
            query = planFilterHelper.sortByPopularity(50);
        } else if (selectedType.equals("Drágák")) {
            query = planFilterHelper.sortByPriceDescending(50);
        } else if (selectedType.equals("Olcsók")) {
            query = planFilterHelper.sortByPriceAscending(50);
        } else {
            query = firestore.collection("plans").limit(50);
        }

        loadPlans(query);
    }

    private void loadPlans(Query query) {
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            planList.clear();
            queryDocumentSnapshots.forEach(document -> {
                Plan plan = document.toObject(Plan.class);
                plan.setId(document.getId());
                planList.add(plan);
            });
            planAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e(LOG_TAG, "Error loading plans", e);
            Toast.makeText(this, "Hiba történt a csomagok betöltésekor.", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * A vásárlás oldalra továbbító metódus
     */
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