package com.example.attendo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
// For Realtime Database
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private EditText name, email, password;
    private Button register;
    private TextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // If user is already logged in, skip registration
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
            return;
        }

        name = findViewById(R.id.register_name);
        email = findViewById(R.id.register_email);
        password = findViewById(R.id.register_password);
        register = findViewById(R.id.register_btn);
        login = findViewById(R.id.login_here);

        // Already have an account?
        login.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Register new user
        register.setOnClickListener(v -> {
            String nametext = name.getText().toString().trim();
            String emailtext = email.getText().toString().trim();
            String passtext = password.getText().toString().trim();

            if (nametext.isEmpty() || emailtext.isEmpty() || passtext.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create user in Firebase Auth
            mAuth.createUserWithEmailAndPassword(emailtext, passtext)
                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // If user creation is successful
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    saveUserData(user.getUid(), nametext, emailtext);
                                }
                            } else {
                                // If user creation fails
                                Toast.makeText(RegisterActivity.this,
                                        "Registration Failed: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }

    private void saveUserData(String userId, String name, String email) {
        // Just store name, email, skill = "" & last_edited = "Just Created"
        User user = new User(name, email, "", "Just Created");

        mDatabase.child("users").child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Registration Failed to Save Data", Toast.LENGTH_SHORT).show();
                });
    }
}
