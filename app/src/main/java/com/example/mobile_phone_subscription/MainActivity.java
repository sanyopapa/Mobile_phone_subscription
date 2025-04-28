package com.example.mobile_phone_subscription;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

/**
 * Az alkalmazás fő Activity-je, amely a bejelentkezési és regisztrációs funkciókat kezeli.
 * A felhasználók bejelentkezhetnek e-mail és jelszó megadásával, vagy biometrikus azonosítással.
 */
public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String PEF_KEY = Objects.requireNonNull(MainActivity.class.getPackage()).toString();
    private SharedPreferences preferences;
    private static final int SECRET_KEY = 99;
    private FirebaseAuth mAuth;
    EditText userNameET, passwordET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            initializeViews();

            preferences = getSharedPreferences(PEF_KEY, MODE_PRIVATE);

            mAuth = FirebaseAuth.getInstance();

            NotificationHelper.createNotificationChannels(this);

            return insets;
        });
    }

    /**
     * Az Activity újraindításakor hívódik meg, például amikor az előtérbe kerül.
     * Ellenőrzi, hogy van-e anonim felhasználó, és törli azt.
     */
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

    /**
     * Az Activity szüneteltetésekor hívódik meg, elmenti a felhasználónév és jelszó mezők tartalmát.
     */
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username", userNameET.getText().toString());
        editor.putString("password", passwordET.getText().toString());
        editor.apply();
        Log.i(LOG_TAG, "onPause");
    }

    /**
     * A bejelentkezési folyamatot indítja el a megadott e-mail és jelszó alapján.
     * @param view Az a View, amely a metódust meghívta.
     */
    public void login(View view) {
        String input = userNameET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        // Ellenőrzés, hogy a mezők nincsenek-e üresen
        if (input.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Kérlek töltsd ki az összes mezőt!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(input, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "signInWithEmail:success");
                    saveCredentialsForBiometric(input, password);
                    NavigationHelper.toShopping(MainActivity.this);
                } else {
                    Log.w(LOG_TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(MainActivity.this, "Hibás bejelentkezési adatok.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Átnavigál a regisztrációs oldalra.
     * @param view Az a View, amely a metódust meghívta.
     */
    public void toRegister(View view) {
        NavigationHelper.toRegisterWithFade(MainActivity.this, SECRET_KEY);
    }

    /**
     * Anonim bejelentkezést hajt végre, majd átnavigál a vásárlási oldalra.
     * @param view Az a View, amely a metódust meghívta.
     */
    public void toLoginAnonymously(View view) {
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "signInAnonymously:success");
                    NavigationHelper.toShopping(MainActivity.this);
                } else {
                    Log.w(LOG_TAG, "signInAnonymously:failure", task.getException());
                    Toast.makeText(MainActivity.this, "Sikertelen belépés", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Megjeleníti a biometrikus azonosítási ablakot, ha elérhető.
     */
    private void showBiometricPrompt() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometrikus bejelentkezés")
                .setSubtitle("Jelentkezz be ujjlenyomattal vagy arcfelismeréssel")
                .setNegativeButtonText("Mégsem")
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(this,
                ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        // Sikeres azonosítás után bejelentkeztetjük a felhasználót
                        signInWithSavedCredentials();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(MainActivity.this, "Sikertelen biometrikus azonosítás",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(MainActivity.this, "Hiba: " + errString,
                                Toast.LENGTH_SHORT).show();
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * A mentett bejelentkezési adatokkal próbál bejelentkezni biometrikus azonosítás után.
     */
    private void signInWithSavedCredentials() {
        // Tárolt bejelentkezési adatok lekérése biztonságos tárolóból
        SharedPreferences securePrefs = getSharedPreferences("secure_prefs", MODE_PRIVATE);
        String email = securePrefs.getString("email", "");
        String password = securePrefs.getString("password", "");

        if (!email.isEmpty() && !password.isEmpty()) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Log.d(LOG_TAG, "Sikeres biometrikus bejelentkezés");
                        NavigationHelper.toShopping(MainActivity.this);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Hiba: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Nincsenek mentett bejelentkezési adatok",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Biometrikus bejelentkezést indít, ha az eszköz támogatja.
     * @param view Az a View, amely a metódust meghívta.
     */
    public void biometricLogin(View view) {
        if (isBiometricAvailable()) {
            showBiometricPrompt();
        } else {
            Toast.makeText(this, "A biometrikus azonosítás nem érhető el ezen az eszközön",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Ellenőrzi, hogy elérhető-e biometrikus azonosítás az eszközön.
     * @return true, ha elérhető, különben false.
     */
    private boolean isBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(this);
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * Elmenti a bejelentkezési adatokat a biometrikus bejelentkezéshez.
     * @param email A felhasználó e-mail címe.
     * @param password A felhasználó jelszava.
     */
    private void saveCredentialsForBiometric(String email, String password) {
        SharedPreferences securePrefs = getSharedPreferences("secure_prefs", MODE_PRIVATE);
        securePrefs.edit()
                .putString("email", email)
                .putString("password", password)
                .apply();
    }

    /**
     * Inicializálja az Activity-hez tartozó elemeket.
     */
    private void initializeViews() {
        userNameET = findViewById(R.id.editTextEmail);
        passwordET = findViewById(R.id.editTextPassword);
    }
}