package com.example.mobile_phone_subscription;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {
    private List<Plan> planList;
    private int selectedPosition = -1;
    private boolean isAnonymous;

    public PlanAdapter(List<Plan> planList, boolean isAnonymous) {
        this.planList = planList;
        this.isAnonymous = isAnonymous;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        Plan plan = planList.get(position);
        holder.textViewName.setText(plan.getName());
        holder.textViewDetails.setText(plan.getDetails());
        holder.textViewPrice.setText(String.valueOf(plan.getPrice()));
        holder.radioButton.setChecked(position == selectedPosition);


        if (isAnonymous) {
            holder.radioButton.setVisibility(View.GONE);
        } else {
            holder.radioButton.setVisibility(View.VISIBLE);
            holder.radioButton.setOnClickListener(v -> {
                selectedPosition = holder.getAdapterPosition();
                notifyDataSetChanged();
            });
        }
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

    public static class PlanViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioButton;
        TextView textViewName;
        TextView textViewDetails;
        TextView textViewPrice;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.radioButton);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDetails = itemView.findViewById(R.id.textViewDetails);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
        }
    }
}