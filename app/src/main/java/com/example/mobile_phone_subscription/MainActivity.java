package com.example.mobile_phone_subscription;

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

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;


public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String PEF_KEY = Objects.requireNonNull(MainActivity.class.getPackage()).toString();
    private SharedPreferences preferences;
    private static final int SECRET_KEY = 99;
    private FirebaseAuth mAuth;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    EditText userNameET;
    EditText passwordET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            userNameET = findViewById(R.id.editTextEmail);
            passwordET = findViewById(R.id.editTextPassword);

            preferences = getSharedPreferences(PEF_KEY, MODE_PRIVATE);

            mAuth = FirebaseAuth.getInstance();
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.isAnonymous()) {
            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(LOG_TAG, "Anonymous user profile deleted.");
                    } else {
                        Log.w(LOG_TAG, "Failed to delete anonymous user profile.", task.getException());
                    }
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username", userNameET.getText().toString());
        editor.putString("password", passwordET.getText().toString());
        editor.apply();
        Log.i(LOG_TAG, "onPause");
    }

    public void login(View view) {
        String input = userNameET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        // Ellenőrizd, hogy a mezők nincsenek-e üresen
        if (input.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Kérlek töltsd ki az összes mezőt!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(input, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "signInWithEmail:success");
                    Intent intent = new Intent(MainActivity.this, Shopping.class);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this);
                    startActivity(intent, options.toBundle());
                } else {
                    Log.w(LOG_TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(MainActivity.this, "Hibás bejelentkezési adatok.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void toRegister(View view) {
        Intent intent = new Intent(this, Register.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
        startActivity(intent, options.toBundle());
    }

    public void toLoginAnonymously(View view) {
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "signInAnonymously:success");
                    Intent intent = new Intent(MainActivity.this, Shopping.class);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this);
                    startActivity(intent, options.toBundle());
                } else {
                    Log.w(LOG_TAG, "signInAnonymously:failure", task.getException());
                    Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}