package com.example.stuntingdetection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView usernameTextView, heightTextView, ageTextView, statusTextView, genderTextView, malnutritionStatusTextView;
    private Button addProfileButton;
    private Button selectProfileButton;
    private Button editProfileButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        usernameTextView = view.findViewById(R.id.username);
        heightTextView = view.findViewById(R.id.height);
        ageTextView = view.findViewById(R.id.age);
        statusTextView = view.findViewById(R.id.status);
        genderTextView = view.findViewById(R.id.gender);
        malnutritionStatusTextView = view.findViewById(R.id.malnutrition_status); // Initialize the new TextView
        addProfileButton = view.findViewById(R.id.add_profile_button);
        selectProfileButton = view.findViewById(R.id.select_profile_button);
        editProfileButton = view.findViewById(R.id.edit_profile_button);

        // Set OnClickListener for selectProfileButton to navigate to SelectProfilesFragment
        selectProfileButton.setOnClickListener(v -> loadFragment(new SelectProfileFragment()));

        // Set OnClickListener for the button to navigate to AddProfileFragment
        addProfileButton.setOnClickListener(v -> loadFragment(new AddProfileFragment()));

        // Set OnClickListener for the button to navigate to EditProfileFragment
        editProfileButton.setOnClickListener(v -> loadFragment(new EditProfileFragment()));

        // Set OnClickListener for the status TextView
        statusTextView.setOnClickListener(v -> {
            if (statusTextView.getText().toString().equals("None")) {
                Toast.makeText(getActivity(), "Begin scan to view status", Toast.LENGTH_SHORT).show();
            }
        });

        // Check if the fragment was created with arguments
        if (getArguments() != null) {
            String username = getArguments().getString("username");
            String height = getArguments().getString("height");
            String age = getArguments().getString("age");
            String status = getArguments().getString("status");
            String gender = getArguments().getString("gender");
            String malnutritionStatus = getArguments().getString("malnutritionStatus");

            // Set the profile data from the arguments
            usernameTextView.setText(username);
            heightTextView.setText("Height: " + height);
            ageTextView.setText("Age: " + age);
            genderTextView.setText("Gender: " + gender);
            statusTextView.setText("Status: " + (status != null ? status : "None"));
            malnutritionStatusTextView.setText("malnutritionStatus: " + (malnutritionStatus != null ? malnutritionStatus : "None"));
        } else {
            // Fetch user profile data from Firestore if no arguments are provided
            fetchUserProfile();
        }

        return view;
    }

    private void fetchUserProfile() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();
            CollectionReference profilesRef = db.collection("users").document(userID).collection("profiles");

            profilesRef.get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            String username = documentSnapshot.getString("Username");
                            String height = documentSnapshot.getString("Height");
                            String age = documentSnapshot.getString("Age");
                            String status = documentSnapshot.getString("Status");
                            String gender = documentSnapshot.getString("Gender");
                            String malnutritionStatus = documentSnapshot.getString("malnutritionStatus");

                            // Default to "None" if malnutrition status is not available
                            if (malnutritionStatus == null || malnutritionStatus.isEmpty()) {
                                malnutritionStatus = "None";
                            }

                            usernameTextView.setText(username);
                            heightTextView.setText("Height: " + height);
                            ageTextView.setText("Age: " + age);
                            genderTextView.setText("Gender: " + gender);
                            statusTextView.setText("Status: " + (status != null ? status : "None"));
                            malnutritionStatusTextView.setText("Malnutrition Status: " + malnutritionStatus);
                        } else {
                            Toast.makeText(getActivity(), "No profile found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Error fetching profile", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getActivity(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_layout, fragment)
                .addToBackStack(null)
                .commit();
    }
}



