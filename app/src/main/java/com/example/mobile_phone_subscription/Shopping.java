package com.example.mobile_phone_subscription;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
                import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
                import android.widget.Button;
                import android.widget.Toast;

                import androidx.activity.EdgeToEdge;
                import androidx.annotation.NonNull;
                import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
                import androidx.core.view.ViewCompat;
                import androidx.core.view.WindowInsetsCompat;
                import androidx.recyclerview.widget.LinearLayoutManager;
                import androidx.recyclerview.widget.RecyclerView;

                import com.google.firebase.auth.FirebaseAuth;
                import com.google.firebase.auth.FirebaseUser;

                import java.util.ArrayList;
                import java.util.List;
                public class Shopping extends AppCompatActivity {
                    private static final String LOG_TAG = Shopping.class.getName();
                    private FirebaseUser user;
                    private RecyclerView recyclerViewPlans;
                    private Button buttonPurchase;
                    private List<Plan> planList;
                    private PlanAdapter planAdapter;

                    @Override
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                        EdgeToEdge.enable(this);
                        setContentView(R.layout.activity_shopping);
                        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

                            user = FirebaseAuth.getInstance().getCurrentUser();
                            buttonPurchase = findViewById(R.id.buttonPurchase);

                            if (user != null) {
                                Log.d(LOG_TAG, "Authenticated user: " + user.getEmail());
                                if (user.isAnonymous()) {
                                    buttonPurchase.setVisibility(View.GONE);
                                }
                            } else {
                                Log.d(LOG_TAG, "Unauthenticated user");
                                finish();
                            }

                            recyclerViewPlans = findViewById(R.id.recyclerViewPlans);

                            planList = new ArrayList<>();
                            planList.add(new Plan("Alap csomag", "10GB adat, 100 perc", 10.0, ""));
                            planList.add(new Plan("Standard csomag", "20GB adat, 200 perc", 20.0, ""));
                            planList.add(new Plan("Prémium csomag", "50GB adat, korlátlan perc", 50.0, ""));

                            planAdapter = new PlanAdapter(planList, user.isAnonymous());
                            recyclerViewPlans.setLayoutManager(new LinearLayoutManager(this));
                            recyclerViewPlans.setAdapter(planAdapter);

                            buttonPurchase.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Plan selectedPlan = planAdapter.getSelectedPlan();
                                    if (selectedPlan != null) {
                                        Toast.makeText(Shopping.this, "Megvásárolva: " + selectedPlan.getName(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(Shopping.this, "Nincs kiválasztott csomag", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            Toolbar toolbar = findViewById(R.id.toolbar);
                            setSupportActionBar(toolbar);

                            return insets;
                        });
                    }

                    public void Logout(View view) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(this, MainActivity.class);
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Shopping.this);
                        startActivity(intent, options.toBundle());
                        finish();
                    }

                    public void openLoginPage(View view) {
                        Intent intent = new Intent(this, MainActivity.class);
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Shopping.this);
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
                }