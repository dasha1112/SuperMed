package com.supermed.patient.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.supermed.patient.R;
import com.supermed.patient.model.Appointment;

import java.util.ArrayList;

public class AppointmentAdapter extends ArrayAdapter<Appointment> {

    public AppointmentAdapter(@NonNull Context context, int resource) {
        super(context, resource, new ArrayList<>());
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_appointment, parent, false);
        }

        Appointment appointment = getItem(position);
        if (appointment != null) {
            TextView tvDoctor = convertView.findViewById(R.id.tv_doctor);
            TextView tvDateTime = convertView.findViewById(R.id.tv_datetime);
            TextView tvStatus = convertView.findViewById(R.id.tv_status);

            tvDoctor.setText("Врач: " + appointment.getDoctorName());
            tvDateTime.setText(appointment.getAppointmentDate() + " в " + appointment.getStartTime());
            tvStatus.setText("Статус: " + appointment.getStatus());
        }

        return convertView;
    }
}
