package com.example.stuntingdetection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private List<Profile> profileList;
    private OnItemClickListener onItemClickListener;

    // Constructor with OnItemClickListener
    public ProfileAdapter(List<Profile> profileList, OnItemClickListener onItemClickListener) {
        this.profileList = profileList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Profile profile = profileList.get(position);
        holder.usernameTextView.setText(profile.getUsername());
        holder.heightTextView.setText("Height: " + profile.getHeight());
        holder.ageTextView.setText("Age: " + profile.getAge());
        holder.genderTextView.setText("Gender: " + profile.getGender());
        holder.statusTextView.setText("Status: " + (profile.getStatus() != null ? profile.getStatus() : "None"));
        holder.malnutritionStatusTextView.setText("Malnutrition Status: " + (profile.getMalnutritionStatus() != null ? profile.getMalnutritionStatus() : "None"));

        holder.cardView.setOnClickListener(v -> onItemClickListener.onItemClick(profile, false)); // Ensure this is properly set
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Profile profile, boolean isScanning);
    }

    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView usernameTextView, heightTextView, ageTextView, statusTextView, genderTextView, malnutritionStatusTextView;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.profile_card_views);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            heightTextView = itemView.findViewById(R.id.height_text_view);
            ageTextView = itemView.findViewById(R.id.age_text_view);
            statusTextView = itemView.findViewById(R.id.status_text_view);
            genderTextView = itemView.findViewById(R.id.gender_text_view);
            malnutritionStatusTextView = itemView.findViewById(R.id.malnutrtion_text_view); // Initialize the new TextView
        }
    }
}

