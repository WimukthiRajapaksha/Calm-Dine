package com.example.calmdine;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    ProgressBar loginProgress;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private static boolean s_persistenceInitialized = false;

    EditText username;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        username = findViewById(R.id.txtUserName);
        password = findViewById(R.id.txtPassword);

        mDatabase = FirebaseDatabase.getInstance();

        if (!s_persistenceInitialized) {
            mDatabase.setPersistenceEnabled(true);
            s_persistenceInitialized = true;
        }

        mDatabase.setLogLevel(Logger.Level.DEBUG);

        loginProgress = findViewById(R.id.progressBar);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        long endAt = 100L; // Fixed value: CRASH on third app restart
//        //  long endAt = new Date().getTime(); // Dynamic value: NO CRASH
//        getGoal("min_per_day", endAt, "some_uid");
//    }
//
//    private void getGoal(String p_goalId, long p_endAt, String p_uid) {
//        Query ref = mDatabase.getReference("v0/data/meditation/goals").child(p_goalId).child(p_uid)
//                .orderByChild("time").endAt(p_endAt).limitToLast(1);
//
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.i("FB", "Snapshot: " + dataSnapshot);
//            }
//            @Override
//            public void onCancelled(DatabaseError error) {
//                Log.e("FB", "Error: " + error);
//            }
//        });
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    public void onLogin(View view) {
        loginProgress.setVisibility(View.VISIBLE);
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
            Toast.makeText(this, "DB", Toast.LENGTH_LONG).show();
        } else {
            Log.i("Ok", "Done");
        }
        mAuth.signOut();
        mAuth.signInWithEmailAndPassword(username.getText().toString(), password.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    loginProgress.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    Log.i("Ok", "SignIn Done");
                } else {
                    Log.i("Ok", "SignIn Error");
                    mAuth.createUserWithEmailAndPassword(username.getText().toString(), password.getText().toString()).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            loginProgress.setVisibility(View.INVISIBLE);
                            if (task.isSuccessful()) {
                                Log.i("Ok", "Create User done");
                                Toast.makeText(MainActivity.this, "Created User", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                            } else {
//                                Log.i("Ok", "Create User error");
//                                Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_LONG).show();
//                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
//                                startActivity(intent);
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    Toast.makeText(MainActivity.this, "Weak Password", Toast.LENGTH_LONG).show();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    Toast.makeText(MainActivity.this, "Invalid Email", Toast.LENGTH_LONG).show();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    Toast.makeText(MainActivity.this, "User Exists", Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    public void onSignUpLogin(View view) {
    }
}
