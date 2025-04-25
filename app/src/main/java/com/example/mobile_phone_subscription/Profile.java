package com.example.mobile_phone_subscription;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends AppCompatActivity {

    private EditText editTextName, editTextPhone, editTextPassword, editTextPasswordAgain;
    private TextView TextEmail;
    private Button buttonSave, buttonReset;
    private FirebaseUser user;
    private FirebaseFirestore firestore;

    private View linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firestore = FirebaseFirestore.getInstance();
        initializeViews();
        setupToolbar();
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

        // Frissítsük a felhasználói adatokat, hogy mindig a legfrissebb adatokat mutassuk
        user = currentUser;
        loadUserData();

        // Frissítsük a menüt a felhasználói jogosultságok alapján
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
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        linearLayout = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(linearLayout, (v, insets) -> {
            Insets systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            layoutParams.topMargin = systemBarsInsets.top;
            v.setLayoutParams(layoutParams);
            return insets;
        });
    }

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

                            editTextName.setText(name != null ? name : "");
                            editTextPhone.setText(phone != null ? phone : "");
                        } else {
                            showToast("Nem találtam betöltendő adatot!");
                        }
                    })
                    .addOnFailureListener(e -> showToast("Hiba történt az adatok betöltése közben!"));
        }
    }

    private void saveUserData() {
        String name = editTextName.getText().toString();
        String email = TextEmail.getText().toString();
        String phone = editTextPhone.getText().toString();
        String password = editTextPassword.getText().toString();
        String passwordAgain = editTextPasswordAgain.getText().toString();

        if (user != null) {
            if (!password.isEmpty() && password.equals(passwordAgain)) {
                user.updatePassword(password)
                    .addOnSuccessListener(aVoid -> showToast("Jelszó sikeresen frissítve!"))
                    .addOnFailureListener(e -> showToast("Hiba történt a jelszó frissítése során!"));
            } else if (!password.isEmpty()) {
                showToast("A jelszavak nem egyeznek!");
                return;
            }

            firestore.collection("users").document(user.getUid())
                .update("name", name, "phone", phone)
                .addOnSuccessListener(aVoid -> showToast("Adatok sikeresen mentve!"))
                .addOnFailureListener(e -> showToast("Hiba történt az adatok mentése során!"));
        }
    }

    private void showToast(String message) {
        Toast.makeText(Profile.this, message, Toast.LENGTH_SHORT).show();
    }

    public void Logout(View view) {
        FirebaseAuth.getInstance().signOut();
        navigateTo(MainActivity.class);
        finish();
    }

    public void ToShopping(View view) {
        navigateTo(Shopping.class);
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Profile.this);
        startActivity(intent, options.toBundle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shop_list_menu, menu);
        menu.findItem(R.id.to_login_button).setVisible(false);
        menu.findItem(R.id.profile_button).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout_button) {
            Logout(null);
            return true;
        } else if (id == R.id.shop_button) {
            ToShopping(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}