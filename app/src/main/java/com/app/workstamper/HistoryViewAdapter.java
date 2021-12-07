package com.app.workstamper;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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

        TextWatcher timeDateWatcher = new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                holder.hoursLabel.setText(DatetimeHelper.getCountedHours(stampData.get(pos)));
                Stamper.Database.UpdateStamp(stampData.get(pos)); // Send changes to server.
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }
        };

        // Update hours label if any of these changed it.
        holder.startDateButton.addTextChangedListener(timeDateWatcher);
        holder.startTimeButton.addTextChangedListener(timeDateWatcher);
        holder.endTimeButton.addTextChangedListener(timeDateWatcher);
        holder.endDateButton.addTextChangedListener(timeDateWatcher);


        holder.startDateButton.setText(DatetimeHelper.Date.toStringFormat(stampData.get(pos).startDateTime));
        holder.startTimeButton.setText(DatetimeHelper.Time.toStringFormat(stampData.get(pos).startDateTime));
        holder.endTimeButton.setText(DatetimeHelper.Time.toStringFormat(stampData.get(pos).endDateTime));
        holder.endDateButton.setText(DatetimeHelper.Date.toStringFormat(stampData.get(pos).endDateTime));
        holder.idLabel.setText(stampData.get(pos).id);
        holder.typeLabel.setVisibility(stampData.get(pos).type.equals("Normal") ? View.INVISIBLE : View.VISIBLE);
        holder.foodBreakCheckBox.setChecked(stampData.get(pos).hadFoodBreak);

        holder.startTimeButton.setOnClickListener(v ->
                DatetimeHelper.Time.pickerDialog(holder.startTimeButton, stampData.get(pos).startDateTime, false));

        holder.startDateButton.setOnClickListener(v ->
                DatetimeHelper.Date.pickerDialog(holder.startDateButton, stampData.get(pos).startDateTime, false));

        holder.endTimeButton.setOnClickListener(v ->
                DatetimeHelper.Time.pickerDialog(holder.endTimeButton, stampData.get(pos).endDateTime, false));

        holder.endDateButton.setOnClickListener(v ->
                DatetimeHelper.Date.pickerDialog(holder.endDateButton, stampData.get(pos).endDateTime, false));

        holder.foodBreakCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            stampData.get(pos).hadFoodBreak = isChecked;
            holder.hoursLabel.setText(DatetimeHelper.getCountedHours(stampData.get(pos)));
            Stamper.Database.UpdateStamp(stampData.get(pos));
        });


        holder.deleteButton.setOnClickListener(view ->
        {
            Stamper.Database.DeleteStamp(stampData.get(pos));
            Stamper.Database.UpdateDocumentArray();
            stampData.remove(stampData.get(pos));
            stampData.trimToSize();
            notifyItemRemoved(pos);
            notifyItemRangeChanged(0, stampData.size()); // Update item range.
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
            idLabel,
            typeLabel,
            hoursLabel;
        Button
            startTimeButton,
            startDateButton,
            endTimeButton,
            endDateButton;
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

            idLabel = view.findViewById(R.id.stampIdLbl);
            typeLabel = view.findViewById(R.id.stampTypeLbl);
            hoursLabel = view.findViewById(R.id.hoursLbl);

            startTimeButton = view.findViewById(R.id.startTimeBtn);
            startDateButton = view.findViewById(R.id.startDateBtn);
            endTimeButton = view.findViewById(R.id.endTimeBtn);
            endDateButton = view.findViewById(R.id.endDateBtn);
            foodBreakCheckBox = view.findViewById(R.id.foodBreakBox);
            deleteButton = view.findViewById(R.id.removeBtn);
        }
    }
}
