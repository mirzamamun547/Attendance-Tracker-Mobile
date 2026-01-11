package com.example.myattendancetracker;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

public class ReasonDialog extends Dialog {

    public interface ReasonListener {
        void onReasonSubmitted(String reason);
    }

    private ReasonListener listener;
    private EditText etReason;
    private Button btnSubmit;

    public ReasonDialog(@NonNull Context context, ReasonListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_reason);

        etReason = findViewById(R.id.etReasonDialog);
        btnSubmit = findViewById(R.id.btnSubmitReasonDialog);

        btnSubmit.setOnClickListener(v -> {
            String reasonText = etReason.getText().toString().trim();
            if (!reasonText.isEmpty() && listener != null) {
                listener.onReasonSubmitted(reasonText);
                dismiss();
            } else {
                etReason.setError("Please enter a reason");
            }
        });
    }
}
