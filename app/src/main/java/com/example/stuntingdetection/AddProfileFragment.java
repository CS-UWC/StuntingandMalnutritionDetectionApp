package com.example.stuntingdetection;

import static android.content.ContentValues.TAG;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddProfileFragment extends Fragment {

    private static final String BASE_URL = "http://172.25.51.8:41777/predict";
    ;

    // Use http if https is not configured
    // Update with your actual Flask API URL
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private EditText edUsername, edHeight, edAge, edGender;
    private Button btnSubmit;
    private String userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_profile, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        edUsername = view.findViewById(R.id.et_username);
        edHeight = view.findViewById(R.id.et_height);
        edAge = view.findViewById(R.id.et_age);
        edGender = view.findViewById(R.id.et_gender);
        btnSubmit = view.findViewById(R.id.btn_submit);

        // Set onClickListener for the button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edUsername.getText().toString().trim();
                String height = edHeight.getText().toString().trim();
                String age = edAge.getText().toString().trim();
                String gender = edGender.getText().toString().trim();

                if (name.isEmpty() || height.isEmpty() || age.isEmpty() || gender.isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if height is numeric
                if (!height.matches("\\d+(\\.\\d+)?")) {
                    Toast.makeText(getActivity(), "Please enter a valid height in number format", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if age is numeric
                if (!age.matches("\\d+")) {
                    Toast.makeText(getActivity(), "Please enter a valid age in number format", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate gender
                if (!gender.equals("male") && !gender.equals("female")) {
                    Toast.makeText(getActivity(), "Please enter 'male' or 'female' for gender", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (name.isEmpty() || height.isEmpty() || age.isEmpty() || gender.isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser != null) {
                    userID = currentUser.getUid();
                } else {
                    Log.e(TAG, "No authenticated user found");
                    Toast.makeText(getActivity(), "User not authenticated", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Prepare the profile data for the API request
                sendApiRequest(name, height, age, gender);
            }
        });

        return view;
    }
    private void sendApiRequest(String name, String height, String age, String gender) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, BASE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "API Response: " + response);
                        Toast.makeText(getActivity(), "API Response received: " + response, Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int predictedStatusCode = jsonObject.getInt("prediction");
                            String predictedStatus = mapPredictionToStatus(predictedStatusCode);
                            saveProfileToFirestore(name, height, age, gender, predictedStatus);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "Error parsing prediction response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "API call failed: " + error.getMessage());
                        if (error.networkResponse != null) {
                            Log.e(TAG, "Error Status Code: " + error.networkResponse.statusCode);
                            Log.e(TAG, "Error Data: " + new String(error.networkResponse.data));
                        }
                        Toast.makeText(getActivity(), "API call failed", Toast.LENGTH_SHORT).show();
                    }

                }) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                // Create the JSON body
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("age", age);
                    jsonBody.put("height", height);
                    jsonBody.put("gender", gender);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonBody.toString().getBytes();
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(stringRequest);
    }

    private String mapPredictionToStatus(int predictionCode) {
        switch (predictionCode) {
            case 0:
                return "Normal";
            case 1:
                return "Severely Stunted";
            case 2:
                return "Stunted";
            case 3:
                return "Tall";
            default:
                return "Unknown";
        }
    }



    private void saveProfileToFirestore(String name, String height, String age, String gender, String predictedStatus) {
        DocumentReference profileRef = db.collection("users")
                .document(userID)
                .collection("profiles")
                .document(); // Auto-generated ID for the profile document

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("Username", name);
        userProfile.put("Height", height);
        userProfile.put("Age", age);
        userProfile.put("Gender", gender);
        userProfile.put("Status", predictedStatus);

        profileRef.set(userProfile).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Profile successfully written with ID: " + profileRef.getId());
            Toast.makeText(getActivity(), "Profile added successfully!", Toast.LENGTH_SHORT).show();
            // Load ProfileFragment after successfully saving the data
            loadFragment(new ProfileFragment());
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error adding profile", e);
            Toast.makeText(getActivity(), "Failed to add profile. Please try again.", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        Toast.makeText(getActivity(), "Fragment loaded", Toast.LENGTH_SHORT).show();
    }
}

