package com.supermed.patient.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.supermed.patient.R;
import com.supermed.patient.api.ApiClient;
import com.supermed.patient.api.ApiService;
import com.supermed.patient.model.Branch;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BranchActivity extends AppCompatActivity {

    private ListView listView;
    private ProgressBar progressBar;
    private ApiService apiService;
    private BranchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = findViewById(R.id.list_view);
        progressBar = findViewById(R.id.progress_bar);
        apiService = ApiClient.getService();

        adapter = new BranchAdapter(this, R.layout.item_branch);
        listView.setAdapter(adapter);

        loadBranches();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Branch branch = adapter.getItem(position);
            if (branch != null) {
                Intent intent = new Intent(BranchActivity.this, DoctorActivity.class);
                intent.putExtra("branchId", branch.getId());
                intent.putExtra("branchName", branch.getName());
                startActivity(intent);
            }
        });
    }

    private void loadBranches() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getBranches().enqueue(new Callback<List<Branch>>() {
            @Override
            public void onResponse(Call<List<Branch>> call, Response<List<Branch>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.clear();
                    adapter.addAll(response.body());
                } else {
                    Toast.makeText(BranchActivity.this, "Не удалось загрузить филиалы", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Branch>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(BranchActivity.this, "Нет подключения к серверу", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // === Вложенный адаптер  ===
    public static class BranchAdapter extends ArrayAdapter<Branch> {

        public BranchAdapter(@NonNull Context context, int resource) {
            super(context, resource, new ArrayList<>());
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_branch, parent, false);
            }

            Branch branch = getItem(position);
            if (branch != null) {
                TextView tvName = convertView.findViewById(R.id.tv_branch_name);
                TextView tvAddress = convertView.findViewById(R.id.tv_branch_address);
                tvName.setText(branch.getName());
                tvAddress.setText(branch.getAddress());
            }
            return convertView;
        }
    }
}