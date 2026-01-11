package com.example.myattendancetracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ViewAbsenceReasonsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AbsenceAdapter adapter;
    private ArrayList<AbsenceModel> absenceList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_absence_reasons);

        recyclerView = findViewById(R.id.recyclerAbsence);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        absenceList = new ArrayList<>();
        adapter = new AbsenceAdapter(absenceList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadAbsenceReasons();
    }

    private void loadAbsenceReasons() {
        db.collection("absence_reasons")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    absenceList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        String className = doc.getString("className");
                        String reason = doc.getString("reason");
                        String email = doc.getString("studentEmail");
                        long time = doc.getLong("timestamp");

                        absenceList.add(new AbsenceModel(className, email, reason, time));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
                );
    }

    // ================= ADAPTER =================
    static class AbsenceAdapter extends RecyclerView.Adapter<AbsenceAdapter.AbsenceViewHolder> {

        private final ArrayList<AbsenceModel> list;

        AbsenceAdapter(ArrayList<AbsenceModel> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public AbsenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_absence_reason, parent, false);
            return new AbsenceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AbsenceViewHolder holder, int position) {
            AbsenceModel model = list.get(position);

            holder.tvClass.setText("Class: " + model.className);
            holder.tvEmail.setText("Student: " + model.studentEmail);
            holder.tvReason.setText("Reason: " + model.reason);

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            holder.tvDate.setText(sdf.format(new Date(model.timestamp)));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class AbsenceViewHolder extends RecyclerView.ViewHolder {

            TextView tvClass, tvEmail, tvReason, tvDate;

            public AbsenceViewHolder(@NonNull View itemView) {
                super(itemView);
                tvClass = itemView.findViewById(R.id.tvClass);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvReason = itemView.findViewById(R.id.tvReason);
                tvDate = itemView.findViewById(R.id.tvDate);
            }
        }
    }
}
