package com.example.stuntingdetection;

import static java.security.AccessController.getContext;

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

public class ScannerFragment extends Fragment {

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private ImageView mImageView;
    private Button mCaptureBtn, mUploadBtn;
    private Uri imageUrl;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private String userId;
    private String profileId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);

        mImageView = view.findViewById(R.id.image_view);
        mCaptureBtn = view.findViewById(R.id.image_capture);
        mUploadBtn = view.findViewById(R.id.upload_image);

        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (getArguments() != null) {
            profileId = getArguments().getString("profileId");
        }
        if (profileId == null) {
            redirectToSelectProfile();
        }

        mCaptureBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                    String[] permissions = {Manifest.permission.CAMERA};
                    requestPermissions(permissions, PERMISSION_CODE);
                } else {
                    openCamera();
                }
            } else {
                openCamera();
            }
        });

        mUploadBtn.setOnClickListener(v -> {
            if (profileId == null) {
                redirectToSelectProfile();
            } else {
                uploadImage();
                Toast.makeText(getContext(), "Image upload successful!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        imageUrl = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUrl);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void redirectToSelectProfile() {
        getParentFragmentManager().setFragmentResultListener("profileSelected", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                profileId = result.getString("profileId");
                if (profileId != null) {
                    uploadImage();  // Proceed with image upload
                }
            }
        });

        Fragment selectProfileFragment = new SelectProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isScanning", false);
        selectProfileFragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_layout, selectProfileFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == getActivity().RESULT_OK) {
            mImageView.setImageURI(imageUrl);
        }
    }

    private void uploadImage() {
        if (imageUrl != null && profileId != null) {
            StorageReference storageRef = storage.getReference()
                    .child("users/" + userId + "/profiles/" + profileId + ("/" + userId + "_" + profileId) + ".jpg");

            // Log the path where the image is being uploaded
            Log.d("UploadPictureFragment", "Image is being uploaded to: " + storageRef.getPath());

            storageRef.putFile(imageUrl)
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


}



