package com.example.mobile_phone_subscription;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Profile extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPhone;
    private Button buttonSave, buttonReset;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonSave = findViewById(R.id.buttonSave);
        buttonReset = findViewById(R.id.buttonReset);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, systemBarsInsets.bottom);
            return insets;
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            editTextName.setText(user.getDisplayName());
            editTextEmail.setText(user.getEmail());
            editTextPhone.setText(""); // Handle phone number separately if needed
        }

        buttonSave.setOnClickListener(v -> {
            String name = editTextName.getText().toString();
            String email = editTextEmail.getText().toString();
            String phone = editTextPhone.getText().toString();

            Toast.makeText(Profile.this, "Adatok mentve!", Toast.LENGTH_SHORT).show();
        });

        buttonReset.setOnClickListener(v -> {
            if (user != null) {
                editTextName.setText(user.getDisplayName());
                editTextEmail.setText(user.getEmail());
                editTextPhone.setText(""); // Reset phone number
            }
        });
    }

    public void Logout(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Profile.this);
        startActivity(intent, options.toBundle());
        finish();
    }

    public void ToShopping(View view) {
        Intent intent = new Intent(this, Shopping.class);
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
            finish();
            return true;
        } else if (id == R.id.shop_button) {
            ToShopping(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}