package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("INSERT_YOUR_FIREBASE_DATABASE_URL");
    EditText emailText, passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
    }

    public void loginBtn(View view) {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Login.this, "Please enter email or password", Toast.LENGTH_SHORT).show();
        } else {
            databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean found = false;
                    boolean isAdmin = false;
                    String userId = "";

                    for (DataSnapshot user : snapshot.getChildren()) {
                        String dbEmail = user.child("email").getValue(String.class);
                        String dbPassword = user.child("password").getValue(String.class);

                        if (email.equals(dbEmail) && password.equals(dbPassword)) {
                            found = true;
                            userId = user.getKey();
                            Boolean adminValue = user.child("isAdmin").getValue(Boolean.class);
                            if (adminValue != null) {
                                isAdmin = adminValue;
                            }
                        }
                    }

                    if (found) {
                        SharedPreferences sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                        sharedPreferences.edit().putBoolean("isAdmin", isAdmin).putString("email", email).putString("userId", userId).apply();
                        Toast.makeText(Login.this, "Successfully Logged in", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Login.this, Home.class));
                        finish();
                    } else {
                        Toast.makeText(Login.this, "Wrong email or password", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    public void signupBtn(View view) {
        Intent intent = new Intent(Login.this, Signup.class);
        startActivity(intent);
    }
}
