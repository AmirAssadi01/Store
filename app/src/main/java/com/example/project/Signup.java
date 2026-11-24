package com.example.project;

import android.content.Intent;
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

public class Signup extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("INSERT_YOUR_FIREBASE_DATABASE_URL");
    EditText id, usernameText, emailText, passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        id = findViewById(R.id.id);
        usernameText = findViewById(R.id.usernameText);
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
    }

    public void SignupBtn(View view) {
        String userId = id.getText().toString();
        String username = usernameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (userId.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Signup.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else {
            databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(userId)) {
                        Toast.makeText(Signup.this, "ID already registered", Toast.LENGTH_SHORT).show();
                    } else {
                        databaseReference.child("Users").child(userId).child("username").setValue(username);
                        databaseReference.child("Users").child(userId).child("email").setValue(email);
                        databaseReference.child("Users").child(userId).child("password").setValue(password);

                        Toast.makeText(Signup.this, "User registered successfully", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(Signup.this, Login.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    public void LoginBtn(View view) {
        Intent intent = new Intent(Signup.this, Login.class);
        startActivity(intent);
    }
}