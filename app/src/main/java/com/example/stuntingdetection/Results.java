package com.example.stuntingdetection;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Results extends Fragment {
    private ImageView imageViewResult;
    private Interpreter interpreter;
    private TextView textViewInferenceResult;
    private FirebaseFirestore firestore; // Firestore instance
    private String profileId; // Profile ID to update

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_results, container, false);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get the ImageView and TextViews
        imageViewResult = view.findViewById(R.id.image_view_result);
        textViewInferenceResult = view.findViewById(R.id.text_view_malnutrition_result);

        // Get the arguments passed to this fragment
        Bundle bundle = getArguments();
        if (bundle != null) {
            String imageUrl = bundle.getString("imageUrl");
            profileId = bundle.getString("profileId");  // Retrieve the profile ID

            if (imageUrl != null) {
                Glide.with(this)
                        .asBitmap()
                        .load(imageUrl)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                imageViewResult.setImageBitmap(resource);
                                try {
                                    runModelInference(resource);
                                } catch (IOException e) {
                                    Toast.makeText(getContext(), "Model Inference failed", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                // Handle the case when the image load is cleared
                            }
                        });
            } else {
                Toast.makeText(getContext(), "No image URL passed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "No arguments passed to Results fragment", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    public void runModelInference(Bitmap bitmap) throws IOException {
        // Download the model from Firebase
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi()
                .build();

        FirebaseModelDownloader.getInstance()
                .getModel("MalnutritionDetection", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
                    @Override
                    public void onSuccess(CustomModel model) {
                        Log.d("Results", "Model downloaded successfully");
                        Toast.makeText(getContext(), "Model downloaded", Toast.LENGTH_SHORT).show();
                        File modelFile = model.getFile();
                        if (modelFile != null) {
                            try {
                                interpreter = new Interpreter(modelFile);
                                Log.d("Results", "Interpreter initialized successfully");
                                Toast.makeText(getContext(), "Interpreter works", Toast.LENGTH_SHORT).show();
                                performInference(bitmap);
                            } catch (Exception e) {
                                Log.e("Results", "Failed to initialize the Interpreter", e);
                                Toast.makeText(getContext(), "Failed to initialize the Interpreter", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("Results", "Model file is null");
                            Toast.makeText(getContext(), "Model file is null", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Results", "Model download failed", e);
                    Toast.makeText(getContext(), "Model download failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void performInference(Bitmap bitmap) {
        try {
            // Resize the bitmap to the input size required by the model (adjust size as necessary)
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 180, 180, false);

            // Prepare ByteBuffer for model input
            ByteBuffer input = ByteBuffer.allocateDirect(180 * 180 * 3 * 4).order(ByteOrder.nativeOrder());
            for (int y = 0; y < 180; y++) {
                for (int x = 0; x < 180; x++) {
                    int px = resizedBitmap.getPixel(x, y);

                    // Get channel values from the pixel value
                    int r = Color.red(px);
                    int g = Color.green(px);
                    int b = Color.blue(px);

                    // Normalize channel values to [-1.0, 1.0]
                    input.putFloat((r - 127) / 255.0f);
                    input.putFloat((g - 127) / 255.0f);
                    input.putFloat((b - 127) / 255.0f);
                }
            }

            // Prepare a float array to store model output
            float[][] output = new float[1][2];  // Adjust this based on your model's output

            // Run inference
            interpreter.run(input, output);
            Log.d("Results", "Inference completed");

            // Get result label based on model output
            String resultLabel = interpretOutput(output);

            // Display the result and save it to Firestore
            displayResult(resultLabel);
            saveResultToFirestore(resultLabel);
        } catch (Exception e) {
            Log.e("Results", "Inference failed", e);
            Toast.makeText(getContext(), "Inference failed", Toast.LENGTH_SHORT).show();
        }
    }

    private String interpretOutput(float[][] output) {
        // Output is [1, 2], so we check both probabilities
        float malnourishedProb = output[0][1];
        float normalProb = output[0][0];
        Toast.makeText(getContext(), "ModelOutput:Malnourished:"  + malnourishedProb + ", Normal: " + normalProb, Toast.LENGTH_LONG).show();
        // Compare probabilities and return the class with the highest probability
        if (malnourishedProb > 
                normalProb) {
            return "Malnourished";
        } else {
            return "Normal";
        }
    }



    private void displayResult(String resultLabel) {
        textViewInferenceResult.setText("Inference Result: " + resultLabel);
        Toast.makeText(getContext(), "Inference Result: " + resultLabel, Toast.LENGTH_LONG).show();
    }

    private void saveResultToFirestore(String resultLabel) {
        if (profileId != null) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                DocumentReference userProfileRef = firestore.collection("users").document(userId).collection("profiles").document(profileId);

                Log.d("Results", "Updating document at: " + userProfileRef.getPath() + " with data: " + resultLabel);

                userProfileRef.update("malnutritionStatus", resultLabel)
                        .addOnSuccessListener(aVoid -> Log.d("Results", "Profile updated successfully"))
                        .addOnFailureListener(e -> {
                            Log.e("Results", "Failed to update profile", e);
                            Toast.makeText(getContext(), "Failed to update profile. Check logs for details.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Log.e("Results", "No authenticated user found");
                Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("Results", "Profile ID is null");
            Toast.makeText(getContext(), "Profile ID is missing", Toast.LENGTH_SHORT).show();
        }
    }
}



