package com.example.mobile_phone_subscription;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Profil Activity osztály, amely lehetővé teszi a felhasználók számára, hogy megtekintsék és szerkesszék profiladataikat.
 * Az adatok mentése a Firestore adatbázisba történik.
 */
public class Profile extends AppCompatActivity {
    private EditText editTextName, editTextPhone, editTextPassword, editTextPasswordAgain;
    private TextView TextEmail;
    private Button buttonSave, buttonReset;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private LinearLayout subscriptionContainer;
    private TextView textViewSubscriptionName, textViewSubscriptionDetails, textViewSubscriptionPrice, textViewNoSubscription;
    private Button buttonCancelSubscription;
    private String subscriptionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firestore = FirebaseFirestore.getInstance();
        initializeViews();
        setupToolbar();
        ViewInsetsHelper.setupScrollableLayoutInsets(findViewById(R.id.main));
        loadUserData();

        buttonSave.setOnClickListener(v -> saveUserData());
        buttonReset.setOnClickListener(v -> loadUserData());
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Ha a felhasználó kijelentkezett, irányítsuk át a bejelentkező oldalra
        if (currentUser == null) {
            Intent intent = new Intent(this, MainActivity.class);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
            startActivity(intent, options.toBundle());
            finish();
            return;
        }

        // Ha a felhasználói azonosító megváltozott, irányítsuk át a bejelentkező oldalra
        if (user != null && !user.getUid().equals(currentUser.getUid())) {
            Intent intent = new Intent(this, MainActivity.class);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
            startActivity(intent, options.toBundle());
            finish();
            return;
        }

        user = currentUser;
        loadUserData();

        invalidateOptionsMenu();
    }

    /**
     * Inicializálja az oldalon lévő elemeket
     */
    private void initializeViews() {
        editTextName = findViewById(R.id.editTextName);
        TextEmail = findViewById(R.id.textViewEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPasswordAgain = findViewById(R.id.editTextPasswordAgain);
        buttonSave = findViewById(R.id.buttonSave);
        buttonReset = findViewById(R.id.buttonReset);

        subscriptionContainer = findViewById(R.id.subscriptionContainer);
        textViewSubscriptionName = findViewById(R.id.textViewSubscriptionName);
        textViewSubscriptionDetails = findViewById(R.id.textViewSubscriptionDetails);
        textViewSubscriptionPrice = findViewById(R.id.textViewSubscriptionPrice);
        textViewNoSubscription = findViewById(R.id.textViewNoSubscription);
        buttonCancelSubscription = findViewById(R.id.buttonCancelSubscription);

        buttonCancelSubscription.setOnClickListener(v -> cancelSubscription());
    }

    /**
     * Beállítja a Toolbar-t az Activity-hez
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Betölti a felhasználó adatait a Firestore adatbázisból
     */
    private void loadUserData() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            TextEmail.setText(email != null ? email : "");

            firestore.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String phone = documentSnapshot.getString("phone");
                            subscriptionId = documentSnapshot.getString("subscriptionId");

                            editTextName.setText(name != null ? name : "");
                            editTextPhone.setText(phone != null ? phone : "");

                            // Load subscription details if available
                            if (subscriptionId != null && !subscriptionId.isEmpty()) {
                                loadSubscriptionDetails(subscriptionId);
                            } else {
                                subscriptionContainer.setVisibility(View.GONE);
                                textViewNoSubscription.setVisibility(View.VISIBLE);
                            }
                        } else {
                            showToast("Nem találtam betöltendő adatot!");
                        }
                    })
                    .addOnFailureListener(e -> showToast("Hiba történt az adatok betöltése közben!"));
        }
    }

    /**
     * Mentés gomb eseménykezelője
     */
    private void saveUserData() {
        String name = editTextName.getText().toString();
        String email = TextEmail.getText().toString();
        String phone = editTextPhone.getText().toString();
        String password = editTextPassword.getText().toString();
        String passwordAgain = editTextPasswordAgain.getText().toString();

        if (!password.isEmpty() && !password.equals(passwordAgain)) {
            showToast("A jelszavak nem egyeznek!");
            return;
        }

        DialogHelper.showSaveProfileDialog(this, () -> {
            if (user != null) {
                if (!password.isEmpty()) {
                    user.updatePassword(password)
                            .addOnSuccessListener(aVoid -> showToast("Jelszó sikeresen frissítve!"))
                            .addOnFailureListener(e -> showToast("Hiba történt a jelszó frissítése során!"));
                }

                firestore.collection("users").document(user.getUid())
                        .update("name", name, "phone", phone)
                        .addOnSuccessListener(aVoid -> showToast("Adatok sikeresen mentve!"))
                        .addOnFailureListener(e -> showToast("Hiba történt az adatok mentése során!"));
            }
        });
    }

    /**
     * Megjelenít egy rövid üzenetet a felhasználónak
     *
     * @param message Az üzenet szövege
     */
    private void showToast(String message) {
        Toast.makeText(Profile.this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Kijelentkezik a felhasználó és visszatér a bejelentkező oldalra
     *
     * @param view A nézet, amely a metódust meghívta
     */
    public void Logout(View view) {
        FirebaseAuth.getInstance().signOut();
        NavigationHelper.toMain(Profile.this);
        finish();
    }

    /**
     * Megjeleníti a menüt az Activity-hez
     *
     * @param menu A menü, amelyet meg kell jeleníteni
     * @return true, ha a menü sikeresen megjelenik
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shop_list_menu, menu);
        menu.findItem(R.id.to_login_button).setVisible(false);
        menu.findItem(R.id.profile_button).setVisible(false);
        return true;
    }

    /**
     * Kezeli a menüelemek kiválasztását
     *
     * @param item A kiválasztott menüelem
     * @return true, ha a menüelem sikeresen kezelve lett
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout_button) {
            Logout(null);
            return true;
        } else if (id == R.id.shop_button) {
            NavigationHelper.toShopping(Profile.this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Betölti az előfizetés részleteit a Firestore adatbázisból
     *
     * @param planId Az előfizetés azonosítója
     */
    @SuppressLint("DefaultLocale")
    private void loadSubscriptionDetails(String planId) {
        firestore.collection("plans").document(planId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Plan plan = documentSnapshot.toObject(Plan.class);
                        if (plan != null) {
                            // Display subscription details
                            textViewSubscriptionName.setText(plan.getName());
                            textViewSubscriptionDetails.setText(plan.getDetails());
                            textViewSubscriptionPrice.setText(String.format("%d Ft/hó", plan.getPrice()));

                            subscriptionContainer.setVisibility(View.VISIBLE);
                            textViewNoSubscription.setVisibility(View.GONE);
                        }
                    } else {
                        showToast("Az előfizetés adatai nem találhatók!");
                        subscriptionContainer.setVisibility(View.GONE);
                        textViewNoSubscription.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Hiba történt az előfizetés adatainak betöltésekor!");
                    Log.e("Profile", "Error loading subscription", e);
                });
    }

    /**
     * Lemondja az előfizetést
     */
    private void cancelSubscription() {
        DialogHelper.showCancelSubscriptionDialog(this, () -> {
            if (user != null && subscriptionId != null) {
                // Előfizetés nevének lekérése az értesítéshez
                firestore.collection("plans").document(subscriptionId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            String planName;
                            if (documentSnapshot.exists()) {
                                Plan plan = documentSnapshot.toObject(Plan.class);
                                if (plan != null) {
                                    planName = plan.getName();
                                } else {
                                    planName = "";
                                }
                            } else {
                                planName = "";
                            }

                            // Lemondás végrehajtása
                            firestore.collection("users").document(user.getUid())
                                    .update("subscriptionId", null)
                                    .addOnSuccessListener(aVoid -> {
                                        showToast("Előfizetés sikeresen lemondva!");

                                        firestore.collection("plans").document(subscriptionId)
                                                .update("subscribers", FieldValue.increment(-1));

                                        subscriptionContainer.setVisibility(View.GONE);
                                        textViewNoSubscription.setVisibility(View.VISIBLE);
                                        subscriptionId = null;
                                        SubscriptionReminder.cancelReminderAlarm(this, planName);

                                        // Értesítési jogosultság ellenőrzése és értesítés küldése
                                        if (NotificationHelper.hasNotificationPermission(this)) {
                                            NotificationHelper.sendCancellationNotification(this, planName);
                                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            ActivityCompat.requestPermissions(this,
                                                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                                    100);
                                        }
                                    })
                                    .addOnFailureListener(e -> showToast("Hiba történt az előfizetés lemondásakor!"));
                        })
                        .addOnFailureListener(e -> {
                            // Ha nem sikerült lekérni a tervet, akkor csak egyszerűen töröljük
                            firestore.collection("users").document(user.getUid())
                                    .update("subscriptionId", null)
                                    .addOnSuccessListener(aVoid -> {
                                        showToast("Előfizetés sikeresen lemondva!");
                                        subscriptionContainer.setVisibility(View.GONE);
                                        textViewNoSubscription.setVisibility(View.VISIBLE);
                                        subscriptionId = null;

                                        // Értesítési küldése terv név nélkül
                                        if (NotificationHelper.hasNotificationPermission(Profile.this)) {
                                            NotificationHelper.sendCancellationNotification(Profile.this, "");
                                        }
                                    })
                                    .addOnFailureListener(err -> showToast("Hiba történt az előfizetés lemondásakor!"));
                        });
            }
        });
    }
}