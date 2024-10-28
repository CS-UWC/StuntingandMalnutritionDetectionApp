package com.example.stuntingdetection;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;

public class UploadPictureFragment extends Fragment {

    private static final int IMAGE_PICK_CODE = 1003;
    private static final int PERMISSION_REQUEST_CODE = 1004;
    private ImageView mImageView;
    private Button mChooseBtn, mUploadBtn;
    private Uri imageUri;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private String userId;
    private String profileId;
    private String profileStatus;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_picture, container, false);

        mImageView = view.findViewById(R.id.image_view);
        mChooseBtn = view.findViewById(R.id.choose_image);
        mUploadBtn = view.findViewById(R.id.upload_image);


        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (getArguments() != null) {
            profileId = getArguments().getString("profileId");
        }
        if(profileId ==null){
            redirectToSelectProfile();
        }


        mChooseBtn.setOnClickListener(v -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Request permissions for Android 14 and higher
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_DENIED ||
                        ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_DENIED ||
                        ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_DENIED) {

                    requestPermissions(new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    }, PERMISSION_REQUEST_CODE);
                } else {
                    pickImage();
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_DENIED ||
                        ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_DENIED) {

                    requestPermissions(new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO
                    }, PERMISSION_REQUEST_CODE);
                } else {
                    pickImage();
                }
            } else {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                } else {
                    pickImage();
                }
            }
        });

        mUploadBtn.setOnClickListener(v -> {
            if (profileId == null) {
                redirectToSelectProfile();  // If no profile, redirect
            } else {
                uploadImage();
                Toast.makeText(getContext(), "Image upload successful!", Toast.LENGTH_SHORT).show();
                // Profile selected, upload image
            }
        });

        return view;
    }

    private void redirectToSelectProfile() {
        getParentFragmentManager().setFragmentResultListener("profileSelected", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                profileId = result.getString("profileId");
                if (profileId != null) {
                    db.collection("profiles").document(profileId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    profileStatus = documentSnapshot.getString("Status");
                                    Toast.makeText(getContext(), profileStatus, Toast.LENGTH_SHORT).show();
                                    uploadImage(); // Proceed with image upload
                                } else {
                                    Log.e("UploadPictureFragment", "Profile does not exist");
                                    Toast.makeText(getContext(), "Profile does not exist", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("UploadPictureFragment", "Error retrieving profile", e);
                                Toast.makeText(getContext(), "Error retrieving profile", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });

        Fragment selectProfileFragment = new SelectProfileFragment();
        Bundle bundle = new Bundle();

        selectProfileFragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_layout, selectProfileFragment)
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                pickImage();
            } else {
                Toast.makeText(getContext(), "Permission denied. Please allow access to choose an image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            imageUri = data.getData();
            mImageView.setImageURI(imageUri);
        }
    }

    private void uploadImage() {
        if (imageUri != null && profileId != null) {
            StorageReference storageRef = storage.getReference()
                    .child("users/" + userId + "/profiles/" + profileId + ("/"+userId+"_"+profileId) + ".jpg");

            // Log the path where the image is being uploaded
            Log.d("UploadPictureFragment", "Image is being uploaded to: " + storageRef.getPath());

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        Log.d("UploadPictureFragment", "Image URL: " + imageUrl); // Log the image URL
                        redirectToResultsFragment(imageUrl);
                        Toast.makeText(getContext(), "Image upload successful!", Toast.LENGTH_SHORT).show();
                    }))
                    .addOnFailureListener(e -> {
                        Log.e("UploadPictureFragment", "Image upload failed", e);
                        Toast.makeText(getContext(), "Image upload failed!", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "No image or profile selected!", Toast.LENGTH_SHORT).show();
        }
    }




    private void redirectToResultsFragment(String imageUrl) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Ensure profileId and profileStatus are properly initialized
        if (profileId == null) {
            Log.e("UploadPictureFragment", "Profile ID is null. Cannot proceed.");
            return;
        }

        // Create a Bundle to pass data to the ResultsFragment
        Bundle bundle = new Bundle();
        bundle.putString("profileId", profileId);
        bundle.putString("imageUrl", imageUrl);

        // Create the ResultsFragment instance and set the arguments
        Results resultsFragment = new Results();
        resultsFragment.setArguments(bundle);

        // Navigate to the ResultsFragment
        fragmentTransaction.replace(R.id.fragment_layout, resultsFragment)
                .addToBackStack(null)
                .commit();
    }


    // Add methods to get profileId and profileStatus if needed
    private String getProfileId() {
        // Implement logic to retrieve profileId
        return profileStatus; // Replace with actual implementation
    }

    private String getProfileStatus() {
        // Implement logic to retrieve profileStatus
        return profileStatus; // Replace with actual implementation
    }

}

