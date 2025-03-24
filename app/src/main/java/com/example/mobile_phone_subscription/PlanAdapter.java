package com.example.mobile_phone_subscription;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    private List<Plan> planList;
    private int selectedPosition = -1;

    public PlanAdapter(List<Plan> planList) {
        this.planList = planList;
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
        holder.textViewPrice.setText("$" + plan.getPrice());
        holder.radioButton.setChecked(position == selectedPosition);
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

    class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewDetails;
        TextView textViewPrice;
        RadioButton radioButton;

        PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDetails = itemView.findViewById(R.id.textViewDetails);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            radioButton = itemView.findViewById(R.id.radioButton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPosition = getAdapterPosition();
                    notifyDataSetChanged();
                }
            });
        }
    }
}