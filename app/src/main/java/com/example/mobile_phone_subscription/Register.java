package com.example.mobile_phone_subscription;

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

        import com.google.android.gms.auth.api.signin.GoogleSignIn;
        import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
        import com.google.android.gms.auth.api.signin.GoogleSignInClient;
        import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
        import com.google.android.gms.common.api.ApiException;
        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.auth.AuthCredential;
        import com.google.firebase.auth.AuthResult;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.GoogleAuthProvider;

        import java.util.Objects;

public class Register extends AppCompatActivity {
            private static final String LOG_TAG = Register.class.getName();
            private static final String PEF_KEY = Objects.requireNonNull(MainActivity.class.getPackage()).toString();
            private SharedPreferences preferences;
            private FirebaseAuth mAuth;
            private static final int RC_SIGN_IN = 123;
            private GoogleSignInClient mGoogleSignInClient;
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
                    newUsernameET.setText(username);
                    passwordET.setText(password);
                    passwordAgainET.setText(password);

                    mAuth = FirebaseAuth.getInstance();

                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build();

                    mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

                    return insets;
                });
                int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

                if (secret_key != 99) {
                    finish();
                }
            }
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);

                if (requestCode == RC_SIGN_IN) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.d(LOG_TAG, "firebaseAuthWithGoogle:" + account.getId());
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Log.w(LOG_TAG, "Google sign in failed", e);
                    }
                }
            }

            private void firebaseAuthWithGoogle(String idToken) {
                AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
                mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(LOG_TAG, "signInWithCredential:success");
                            startShopping();
                        } else {
                            Log.w(LOG_TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            public void register(View view) {
                String newUsername = newUsernameET.getText().toString();
                String password = passwordET.getText().toString();
                String passwordAgain = passwordAgainET.getText().toString();
                String email = emailET.getText().toString();
                String phone = phoneET.getText().toString();

                if (!password.equals(passwordAgain)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i(LOG_TAG, "New Username: " + newUsername + " Password: " + password + " Password Again: " + passwordAgain + " Email: " + email + " Phone: " + phone);

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d(LOG_TAG, "createUserWithEmail:success");
                            startShopping();
                        }else
                        {
                            Log.d(LOG_TAG, "createUserWithEmail:failed");
                            Toast.makeText(Register.this, "Authentication failed." + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            public void back(View view) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }

            public void toRegisterWithGoogle(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }

            public void startShopping() {
                Intent intent = new Intent(this, Shopping.class);
                startActivity(intent);
            }
        }