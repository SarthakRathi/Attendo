package com.example.attendo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
// For reading database changes
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    // Buttons
    private Button saveBtn, editBtn, changePassBtn;

    // Read-only TextViews
    private TextView nameTV, emailTV, skillTV, lastEditedTV;

    // EditTexts for editing info & resetting password
    private EditText editName, editEmail, editSkill;
    private EditText editPassword, editRepassword;

    // Optionally, an ImageView for the user photo
    private ImageView profilePhoto;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String userId; // Current logged-in user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Init Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // If no user is logged in, finish
        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        } else {
            userId = mAuth.getCurrentUser().getUid();
        }

        // Find views
        saveBtn = findViewById(R.id.save_profile);
        editBtn = findViewById(R.id.edit_profile);
        changePassBtn = findViewById(R.id.change_password);

        nameTV = findViewById(R.id.profile_name);
        emailTV = findViewById(R.id.profile_email);
        skillTV = findViewById(R.id.profile_skill);
        lastEditedTV = findViewById(R.id.last_edited);

        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editSkill = findViewById(R.id.edit_skill);

        editPassword = findViewById(R.id.edit_password);
        editRepassword = findViewById(R.id.edit_repassword);

        profilePhoto = findViewById(R.id.profile_photo);

        // Fetch user data from Realtime Database
        getUserData(userId);

        // Click Listeners
        saveBtn.setOnClickListener(v -> canSave());
        editBtn.setOnClickListener(v -> canEdit());
        changePassBtn.setOnClickListener(v -> resetPassword());
    }

    /**
     * Read user data from Firebase and display in read-only fields + fill edit fields.
     */
    private void getUserData(String userId) {
        mDatabase.child("users")
                .child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            // Update read-only views
                            nameTV.setText(user.username);
                            emailTV.setText(user.email);
                            skillTV.setText(user.skill);
                            lastEditedTV.setText(user.last_edited);

                            // Update edit fields
                            editName.setText(user.username);
                            editEmail.setText(user.email);
                            editSkill.setText(user.skill);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this,
                                "Failed to load user data: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Switch UI to editing mode:
     * Show EditTexts and change/save password fields; hide read-only TextViews.
     */
    private void canEdit() {
        // Show edit fields
        editName.setVisibility(View.VISIBLE);
        editEmail.setVisibility(View.VISIBLE);
        editSkill.setVisibility(View.VISIBLE);

        // Show password fields if you want user to reset password in this same flow
        editPassword.setVisibility(View.VISIBLE);
        editRepassword.setVisibility(View.VISIBLE);
        changePassBtn.setVisibility(View.VISIBLE);

        // Show Save, hide Edit
        saveBtn.setVisibility(View.VISIBLE);
        editBtn.setVisibility(View.GONE);

        // Hide read-only
        nameTV.setVisibility(View.GONE);
        emailTV.setVisibility(View.GONE);
        skillTV.setVisibility(View.GONE);
        lastEditedTV.setVisibility(View.GONE);
    }

    /**
     * Save the updated user info to the database, then switch back to read-only view.
     */
    private void canSave() {
        saveUserData(userId);

        // Switch back to read-only mode
        editName.setVisibility(View.GONE);
        editEmail.setVisibility(View.GONE);
        editSkill.setVisibility(View.GONE);
        editPassword.setVisibility(View.GONE);
        editRepassword.setVisibility(View.GONE);
        changePassBtn.setVisibility(View.GONE);

        saveBtn.setVisibility(View.GONE);
        editBtn.setVisibility(View.VISIBLE);

        nameTV.setVisibility(View.VISIBLE);
        emailTV.setVisibility(View.VISIBLE);
        skillTV.setVisibility(View.VISIBLE);
        lastEditedTV.setVisibility(View.VISIBLE);
    }

    /**
     * Updates only username, email, skill, and last_edited in your Realtime Database.
     * Password is NOT stored in DB.
     */
    private void saveUserData(String userId) {
        String nameText = editName.getText().toString().trim();
        String emailText = editEmail.getText().toString().trim();
        String skillText = editSkill.getText().toString().trim();

        // Set "last_edited" to current date/time
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", nameText);
        updates.put("email", emailText);
        updates.put("skill", skillText);
        updates.put("last_edited", currentTime);

        mDatabase.child("users")
                .child(userId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(ProfileActivity.this,
                                "Profile updated successfully",
                                Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(ProfileActivity.this,
                                "Failed to update profile",
                                Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Reset user password in Firebase Auth (does NOT store password in your DB).
     */
    private void resetPassword() {
        String newPassword = editPassword.getText().toString().trim();
        String rePassword = editRepassword.getText().toString().trim();

        if (newPassword.isEmpty() || rePassword.isEmpty()) {
            Toast.makeText(this,
                    "Please enter the new password in both fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(rePassword)) {
            Toast.makeText(this,
                    "Passwords do not match",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.getCurrentUser().updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this,
                                "Password updated in Firebase Auth",
                                Toast.LENGTH_SHORT).show();

                        // Clear fields after successful update
                        editPassword.setText("");
                        editRepassword.setText("");
                    } else {
                        Toast.makeText(ProfileActivity.this,
                                "Failed to update password: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
