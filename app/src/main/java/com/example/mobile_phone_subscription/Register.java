package com.example.mobile_phone_subscription;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class Register extends AppCompatActivity {
    private static final String LOG_TAG = Register.class.getName();
    private static final String PEF_KEY = Objects.requireNonNull(MainActivity.class.getPackage()).toString();
    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseFirestore firestore;
    private static final int RC_SIGN_IN = 123;
    EditText newUsernameET;
    EditText passwordET;
    EditText passwordAgainET;
    EditText emailET;
    EditText phoneET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            newUsernameET = findViewById(R.id.editTextNewUsername);
            passwordET = findViewById(R.id.editTextPassword);
            passwordAgainET = findViewById(R.id.editTextPasswordAgain);
            emailET = findViewById(R.id.editTextTextEmailAddress);
            phoneET = findViewById(R.id.editTextPhone);

            preferences = getSharedPreferences(PEF_KEY, MODE_PRIVATE);
            String username = preferences.getString("username", "");
            String password = preferences.getString("password", "");
            emailET.setText(username);
            passwordET.setText(password);
            passwordAgainET.setText(password);

            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();

            firestore = FirebaseFirestore.getInstance();

            return insets;
        });
        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if (secret_key != 99) {
            finish();
        }
    }

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
                    startShopping();
                } else {
                    Log.d(LOG_TAG, "createUserWithEmail:failed");
                    Toast.makeText(Register.this, "Hiba történt: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void writeNewUser(String userId, String name, String email, String phone) {
        User user = new User(name, phone);
        firestore.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d(LOG_TAG, "Felhasználó sikeresen mentve!"))
                .addOnFailureListener(e -> Log.e(LOG_TAG, "Hiba történt a mentés során: ", e));
    }

    public void back(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Register.this);
        startActivity(intent, options.toBundle());
    }

    public void startShopping() {
        Intent intent = new Intent(Register.this, Shopping.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Register.this);
        startActivity(intent, options.toBundle());
    }    
}
