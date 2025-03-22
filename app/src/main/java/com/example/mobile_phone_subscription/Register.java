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

        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.auth.AuthResult;
        import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {
            private static final String LOG_TAG = Register.class.getName();
            private static final String PEF_KEY = MainActivity.class.getPackage().toString();
            private SharedPreferences preferences;
            private FirebaseAuth mAuth;
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

                    return insets;
                });
                int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

                if (secret_key != 99) {
                    finish();
                }
            }

            @Override
            protected void onPause() {
                super.onPause();
                Log.i(LOG_TAG, "onPause");
            }

            @Override
            protected void onResume() {
                super.onResume();
                Log.i(LOG_TAG, "onResume");
            }

            @Override
            protected void onDestroy() {
                super.onDestroy();
                Log.i(LOG_TAG, "onDestroy");
            }

            @Override
            protected void onStart() {
                super.onStart();
                Log.i(LOG_TAG, "onStart");
            }

            @Override
            protected void onStop() {
                super.onStop();
                Log.i(LOG_TAG, "onStop");
            }

            @Override
            protected void onRestart() {
                super.onRestart();
                Log.i(LOG_TAG, "onRestart");
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

            }

            public void startShopping() {
                Intent intent = new Intent(this, Shopping.class);
                startActivity(intent);
            }
        }