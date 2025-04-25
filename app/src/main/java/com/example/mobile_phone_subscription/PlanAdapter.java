// File: app/src/main/java/com/example/mobile_phone_subscription/PlanAdapter.java
    package com.example.mobile_phone_subscription;

    import android.annotation.SuppressLint;
    import android.content.Context;
    import android.content.Intent;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Button;
    import android.widget.ImageButton;
    import android.widget.RadioButton;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import com.google.firebase.firestore.FirebaseFirestore;

    import java.util.List;

    public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {
        private List<Plan> planList;
        private int selectedPosition = -1;
        private boolean isAnonymous;
        private boolean isAdmin;
        private FirebaseFirestore firestore;
        private Context context;


        public PlanAdapter(List<Plan> planList, boolean isAnonymous, boolean isAdmin) {
            this.planList = planList;
            this.isAnonymous = isAnonymous;
            this.isAdmin = isAdmin;
            this.firestore = FirebaseFirestore.getInstance();
        }

        @NonNull
        @Override
        public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            context = parent.getContext();
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan, parent, false);
            return new PlanViewHolder(view);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
            Plan plan = planList.get(position);
            holder.textViewName.setText(plan.getName());
            holder.textViewDetails.setText(plan.getDetails());
            holder.textViewPrice.setText(String.format("%d Ft/hó", plan.getPrice()));
            holder.radioButton.setChecked(position == selectedPosition);

            // Egész elem kattintási eseménykezelése
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PlanInfoActivity.class);
                intent.putExtra("PLAN_ID", plan.getId());
                intent.putExtra("PLAN_NAME", plan.getName());
                intent.putExtra("PLAN_DETAILS", plan.getDetails());
                intent.putExtra("PLAN_PRICE", plan.getPrice());
                intent.putExtra("PLAN_DESCRIPTION", plan.getDescription());
                context.startActivity(intent);
            });

            if (isAdmin) {
                holder.buttonDelete.setVisibility(View.VISIBLE);
                holder.buttonEdit.setVisibility(View.VISIBLE);
            } else {
                holder.buttonDelete.setVisibility(View.GONE);
                holder.buttonEdit.setVisibility(View.GONE);
            }

            if (isAnonymous) {
                holder.radioButton.setVisibility(View.GONE);
            } else {
                holder.radioButton.setVisibility(View.VISIBLE);
                holder.radioButton.setOnClickListener(v -> {
                    selectedPosition = holder.getAdapterPosition();
                    notifyDataSetChanged();
                });
            }

            // Törlés gomb
            holder.buttonDelete.setOnClickListener(v -> {
                deletePlan(plan, position);
            });

            // Szerkesztés gomb
            holder.buttonEdit.setOnClickListener(v -> {
                editPlan(plan);
            });
        }

        @Override
        public int getItemCount() {
            return planList.size();
        }

        public Plan getSelectedPlan() {
            if (selectedPosition != -1) {
                return planList.get(selectedPosition);
            }
            return null;
        }

        private void deletePlan(Plan plan, int position) {
            if (plan.getId() == null || plan.getId().isEmpty()) {
                Toast.makeText(context, "Csomag azonosító hiányzik!", Toast.LENGTH_SHORT).show();
                Log.e("PlanAdapter", "Hiányzó plan ID törléskor: " + plan.getName());
                return;
            }

            // Ha lokális ID-val rendelkezik, egyszerűen töröljük a listából
            if (plan.getId().startsWith("local_")) {
                planList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Helyi tétel törölve", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ha van valós Firestore ID, próbáljuk törölni az adatbázisból
            firestore.collection("plans").document(plan.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    planList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Csomag sikeresen törölve!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("PlanAdapter", "Hiba a plan törlésekor", e);
                    Toast.makeText(context, "Firestore hiba: " + e.getMessage() +
                            ". Csak helyi törlés történt.", Toast.LENGTH_SHORT).show();

                    // Firestore hiba esetén is törölhetjük a helyi adatokat
                    planList.remove(position);
                    notifyItemRemoved(position);
                });
        }

        private void editPlan(Plan plan) {
            Intent intent = new Intent(context, PlanEditActivity.class);
            intent.putExtra("PLAN_ID", plan.getId());
            intent.putExtra("PLAN_NAME", plan.getName());
            intent.putExtra("PLAN_DETAILS", plan.getDetails());
            intent.putExtra("PLAN_PRICE", plan.getPrice());
            intent.putExtra("PLAN_IMAGE_URL", plan.getImageUrl());
            context.startActivity(intent);
        }

        private android.content.Context getContext() {
            return context;
        }

        // Inner class for view holder with new buttons for CRUD operations.
        public static class PlanViewHolder extends RecyclerView.ViewHolder {
            TextView textViewName;
            TextView textViewDetails;
            TextView textViewPrice;
            RadioButton radioButton;
            ImageButton buttonDelete;
            ImageButton buttonEdit;

            public PlanViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewName = itemView.findViewById(R.id.textViewName);
                textViewDetails = itemView.findViewById(R.id.textViewDetails);
                textViewPrice = itemView.findViewById(R.id.textViewPrice);
                radioButton = itemView.findViewById(R.id.radioButton);
                buttonDelete = itemView.findViewById(R.id.buttonDelete);
                buttonEdit = itemView.findViewById(R.id.buttonEdit);
            }
        }
    }