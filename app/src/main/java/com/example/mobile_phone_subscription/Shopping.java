package com.example.mobile_phone_subscription;

import android.content.Intent;
import android.os.Bundle;
                import android.util.Log;
                import android.view.View;
                import android.widget.Button;
                import android.widget.Toast;

                import androidx.activity.EdgeToEdge;
                import androidx.annotation.NonNull;
                import androidx.appcompat.app.AppCompatActivity;
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
                            if (user != null){
                                Log.d(LOG_TAG, "Authenticated user: " + user.getEmail());
                                if (user.isAnonymous()) {
                                    buttonPurchase.setVisibility(View.GONE);
                                }
                            }else {
                                Log.d(LOG_TAG, "Unauthenticated user");
                                finish();
                            }

                            return insets;
                        });

                        recyclerViewPlans = findViewById(R.id.recyclerViewPlans);
                        buttonPurchase = findViewById(R.id.buttonPurchase);

                        planList = new ArrayList<>();
                        planList.add(new Plan("Alap csomag", "10GB adat, 100 perc", 10.0));
                        planList.add(new Plan("Standard csomag", "20GB adat, 200 perc", 20.0));
                        planList.add(new Plan("Prémium csomag", "50GB adat, korlátlan perc", 50.0));

                        planAdapter = new PlanAdapter(planList);
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
                    }

                    public void openLoginPage(View view) {
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                    }
                }