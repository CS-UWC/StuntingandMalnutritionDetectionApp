package com.example.stuntingdetection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SelectProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ProfileAdapter profileAdapter;
    private List<Profile> profileList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_profile, container, false);

        recyclerView = view.findViewById(R.id.profiles_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        profileList = new ArrayList<>();
        profileAdapter = new ProfileAdapter(profileList, this::onProfileClick);
        recyclerView.setAdapter(profileAdapter);

        db = FirebaseFirestore.getInstance();
        fetchProfiles();

        return view;
    }

    private void fetchProfiles() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();

            db.collection("users").document(userID)
                    .collection("profiles")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            profileList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String profileId = document.getId();
                                String username = document.getString("Username");
                                String height = document.getString("Height");
                                String age = document.getString("Age");
                                String status = document.getString("Status");
                                String gender = document.getString("Gender");
                                String malnutritionStatus = document.getString("malnutritionStatus");

                                Profile profile = new Profile(username, height, age, status, profileId, gender, malnutritionStatus);
                                profileList.add(profile);
                            }
                            profileAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "Error getting profiles.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }

    private Fragment getCurrentFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        return fragmentManager.findFragmentById(R.id.fragment_layout); // Adjust ID to match your container
    }

    private void onProfileClick(Profile profile, boolean isScanning) {
        Bundle bundle = new Bundle();
        bundle.putString("profileId", profile.getProfileId());
        bundle.putString("username", profile.getUsername());
        bundle.putString("height", profile.getHeight());
        bundle.putString("age", profile.getAge());
        bundle.putString("status", profile.getStatus() != null ? profile.getStatus() : "None");
        bundle.putString("malnutritionStatus", profile.getMalnutritionStatus() != null ? profile.getMalnutritionStatus() : "None");
        Fragment currentFragment = getCurrentFragment();

        if (isScanning) {
            redirectToScanner(bundle);
        } else {
            redirectUpload(bundle);
        }
    }

    private void redirectToScanner(Bundle bundle) {
        ScannerFragment scannerFragment = new ScannerFragment();
        scannerFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_layout, scannerFragment)
                .addToBackStack(null)
                .commit();
    }

    private void redirectUpload(Bundle bundle) {
        UploadPictureFragment uploadFragment = new UploadPictureFragment();
        uploadFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_layout, uploadFragment)
                .addToBackStack(null)
                .commit();
    }


}





