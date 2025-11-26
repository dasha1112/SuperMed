package com.supermed.patient.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.supermed.patient.R;
import com.supermed.patient.model.Doctor;

import java.util.ArrayList;

public class DoctorAdapter extends ArrayAdapter<Doctor> {

    public DoctorAdapter(@NonNull Context context, int resource) {
        super(context, resource, new ArrayList<>());
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_doctor, parent, false);
        }

        Doctor doctor = getItem(position);
        if (doctor != null) {
            TextView tvName = convertView.findViewById(R.id.tv_doctor_name);
            TextView tvSpecialization = convertView.findViewById(R.id.tv_doctor_specialization);
            TextView tvBranch = convertView.findViewById(R.id.tv_doctor_branch);

            tvName.setText(doctor.getName());
            tvSpecialization.setText(doctor.getSpecialization());
            tvBranch.setText("Филиал: " + doctor.getBranchName());
        }

        return convertView;
    }
}
