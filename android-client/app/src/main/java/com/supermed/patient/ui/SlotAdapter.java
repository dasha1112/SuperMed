// app/src/main/java/com/supermed/patient/ui/SlotAdapter.java
package com.supermed.patient.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.supermed.patient.R;
import com.supermed.patient.model.TimeSlot;
import java.util.ArrayList;

public class SlotAdapter extends ArrayAdapter<TimeSlot> {

    public SlotAdapter(@NonNull Context context, int resource) {
        super(context, resource, new ArrayList<>());
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_slot, parent, false);
        }

        TimeSlot slot = getItem(position);
        if (slot != null) {
            TextView tvDateTime = convertView.findViewById(R.id.tv_slot_datetime);
            tvDateTime.setText(slot.date + " в " + slot.time);
        }

        return convertView;
    }
}