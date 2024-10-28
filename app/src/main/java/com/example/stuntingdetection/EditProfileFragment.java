package com.example.stuntingdetection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private EditText usernameEditText;
    private EditText heightEditText;
    private EditText ageEditText;
    private EditText statusEditText;
    private EditText genderEditText;
    private Button saveButton;

    private String profileId;

    public EditProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            profileId = getArguments().getString("profileId");
            Toast.makeText(getContext(), "Profile data avaible", Toast.LENGTH_SHORT).show();// Retrieve the profileId safely
        } else {
            Toast.makeText(getContext(), "Profile data missing", Toast.LENGTH_SHORT).show();
            // Optionally, return or handle the missing profileId case to avoid null pointer issues.
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        usernameEditText = view.findViewById(R.id.et_username);
        heightEditText = view.findViewById(R.id.et_height);
        ageEditText = view.findViewById(R.id.et_age);
        genderEditText = view.findViewById(R.id.et_gender);// Add this line if statusEditText exists
        saveButton = view.findViewById(R.id.btn_submit);

        // Load existing profile data
        if (profileId != null) {
            DocumentReference profileRef = db.collection("users")
                    .document(auth.getCurrentUser().getUid())
                    .collection("profiles")
                    .document(profileId);

            profileRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        usernameEditText.setText(document.getString("username"));
                        heightEditText.setText(document.getString("height"));
                        ageEditText.setText(document.getString("age"));
                        genderEditText.setText(document.getString("gender"));
                        statusEditText.setText(document.getString("status")); // Add this line if statusEditText exists
                    } else {
                        Toast.makeText(getContext(), "No such profile!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Profile ID is missing", Toast.LENGTH_SHORT).show();
        }

        // Save button click listener
        saveButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String height = heightEditText.getText().toString();
            String age = ageEditText.getText().toString();
            String gender = genderEditText.getText().toString();
            String status = statusEditText.getText().toString(); // Add this line if statusEditText exists

            if (!username.isEmpty() && !height.isEmpty() && !age.isEmpty() && !gender.isEmpty() && !status.isEmpty()) {
                DocumentReference profileRef = db.collection("users")
                        .document(auth.getCurrentUser().getUid())
                        .collection("profiles")
                        .document(profileId);

                profileRef.update("username", username,
                                "height", height,
                                "age", age,
                                "gender", gender,
                                "status", status) // Add this line if statusEditText exists
                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Error updating profile", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getContext(), "All fields must be filled out", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
