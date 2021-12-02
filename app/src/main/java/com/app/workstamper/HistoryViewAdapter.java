package com.app.workstamper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryViewAdapter extends RecyclerView.Adapter<HistoryViewAdapter.ViewHolder>
{
    public ArrayList<Stamper.StampData> stampData;

    public HistoryViewAdapter(ArrayList<Stamper.StampData> data)
    {
        this.stampData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        int pos = holder.getAdapterPosition();
        holder.dateLabel.setText(DatetimeHelper.Date.toStringFormat(stampData.get(pos).startDateTime));
        holder.startTimeButton.setText(DatetimeHelper.Time.toStringFormat(stampData.get(pos).startDateTime));
        holder.endTimeButton.setText(DatetimeHelper.Time.toStringFormat(stampData.get(pos).endDateTime));
        holder.foodBreakCheckBox.setChecked(stampData.get(pos).hadFoodBreak);

        holder.startTimeButton.setOnClickListener(v ->
                DatetimeHelper.Time.timePickerDialog(holder.startTimeButton.getContext(), holder.startTimeButton, stampData.get(pos).startDateTime, false));

        holder.endTimeButton.setOnClickListener(v ->
                DatetimeHelper.Time.timePickerDialog(holder.endTimeButton.getContext(), holder.endTimeButton, stampData.get(pos).endDateTime, false));

        holder.foodBreakCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                stampData.get(pos).hadFoodBreak = isChecked);

        holder.deleteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                stampData.remove(stampData.get(pos));
                stampData.trimToSize();
                notifyItemRemoved(pos);
                notifyItemRangeChanged(0, stampData.size()); // Update item range.
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return this.stampData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView
            dateLabel;
        Button
            startTimeButton,
            endTimeButton;
        CheckBox
            foodBreakCheckBox;
        ImageView
            deleteButton;
        View
            rootView;

        public ViewHolder(View view)
        {
            super(view);
            rootView = view;

            dateLabel = view.findViewById(R.id.dateLabel);
            startTimeButton = view.findViewById(R.id.startTimeBtn);
            endTimeButton = view.findViewById(R.id.endTimeBtn);
            foodBreakCheckBox = view.findViewById(R.id.foodBreakBox);
            deleteButton = view.findViewById(R.id.removeBtn);
        }
    }
}
