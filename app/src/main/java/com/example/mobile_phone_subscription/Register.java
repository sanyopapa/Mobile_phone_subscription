package com.example.mobile_phone_subscription;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

/**
 * Regisztrációs Activity osztály, amely lehetővé teszi új felhasználók regisztrálását.
 * Az adatok mentése a Firestore adatbázisba történik.
 */
public class Register extends AppCompatActivity {
    private static final String LOG_TAG = Register.class.getName();
    private static final String PEF_KEY = Objects.requireNonNull(MainActivity.class.getPackage()).toString();
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private CheckBox checkBoxAdmin;
    EditText newUsernameET, passwordET, passwordAgainET, emailET, phoneET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewInsetsHelper.setupScrollableLayoutInsets(findViewById(R.id.main));

        initializeViews();

        mAuth = FirebaseAuth.getInstance();

        firestore = FirebaseFirestore.getInstance();

        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if (secret_key != 99) {
            finish();
        }
    }

    /**
     * Regisztrációs folyamatot indítja el a felhasználó által megadott adatokkal.
     * @param view Az a View, amely a metódust meghívta.
     */
    public void register(View view) {
        String newUsername = newUsernameET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();
        String passwordAgain = passwordAgainET.getText().toString().trim();
        String email = emailET.getText().toString().trim();
        String phone = phoneET.getText().toString().trim();

        if (newUsername.isEmpty() || password.isEmpty() || passwordAgain.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Kérlek töltsd ki az összes mezőt!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(passwordAgain)) {
            Toast.makeText(this, "A jelszavak nem egyeznek!", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i(LOG_TAG, "New Username: " + newUsername + " Password: " + password + " Email: " + email + " Phone: " + phone);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "createUserWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        writeNewUser(user.getUid(), newUsername, email, phone);
                    }
                    NavigationHelper.toShopping(Register.this);
                } else {
                    Log.d(LOG_TAG, "createUserWithEmail:failed");
                    Toast.makeText(Register.this, "Hiba történt: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Új felhasználó adatainak mentése a Firestore adatbázisba.
     * @param userId A felhasználó egyedi azonosítója.
     * @param name A felhasználó neve.
     * @param email A felhasználó e-mail címe.
     * @param phone A felhasználó telefonszáma.
     */
    private void writeNewUser(String userId, String name, String email, String phone) {
        boolean isAdmin = checkBoxAdmin.isChecked();
        User user = new User(name, phone, isAdmin);
        firestore.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d(LOG_TAG, "Felhasználó sikeresen mentve!"))
                .addOnFailureListener(e -> Log.e(LOG_TAG, "Hiba történt a mentés során: ", e));
    }

    /**
     * Visszalép az előző oldalra, bezárja a regisztrációs Activity-t.
     * @param view Az a View, amely a metódust meghívta.
     */
    public void back(View view) {
        NavigationHelper.toMainWithFade(Register.this);
    }

    /**
     * Inicializálja az oldalon lévő elemeket és betölti a SharedPreferences-ben tárolt adatokat.
     */
    private void initializeViews() {
        newUsernameET = findViewById(R.id.editTextNewUsername);
        passwordET = findViewById(R.id.editTextPassword);
        passwordAgainET = findViewById(R.id.editTextPasswordAgain);
        emailET = findViewById(R.id.editTextTextEmailAddress);
        phoneET = findViewById(R.id.editTextPhone);
        checkBoxAdmin = findViewById(R.id.checkBoxAdmin);

        // Bejelentkezési adatok betöltése a SharedPreferences-ből, ha vannak
        SharedPreferences preferences = getSharedPreferences(PEF_KEY, MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");
        emailET.setText(username);
        passwordET.setText(password);
        passwordAgainET.setText(password);
    }
}
