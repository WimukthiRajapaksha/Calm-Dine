package com.example.calmdine;

import android.app.Activity;
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
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
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
import androidx.constraintlayout.widget.ConstraintLayout;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    ProgressBar loginProgress;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private static boolean s_persistenceInitialized = false;
    private boolean loginBool;

    EditText username;
    EditText password;
    TextView loginOrSignUpSection;
    Button btnLoginSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginBool = true;

        mAuth = FirebaseAuth.getInstance();

        username = findViewById(R.id.txtUserName);
        password = findViewById(R.id.txtPassword);
        loginOrSignUpSection = findViewById(R.id.txtLoginOrSignUpSection);
        btnLoginSignup = findViewById(R.id.btnLoginSignup);
        setupUI(findViewById(R.id.mainActivityLinearLayout));

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
        mAuth.signOut();
        Log.i("Value", String.valueOf(loginBool));

        if (loginBool) {
            mAuth.signInWithEmailAndPassword(username.getText().toString(), password.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    loginProgress.setVisibility(View.INVISIBLE);
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        Log.i("Ok", "Login Done");
                    } else {
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            Toast.makeText(MainActivity.this, "Login Error: There is no corresponding user record.", Toast.LENGTH_LONG).show();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            Toast.makeText(MainActivity.this, "Login Error: The password is invalid.", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        } else {
            mAuth.createUserWithEmailAndPassword(username.getText().toString(), password.getText().toString()).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                loginProgress.setVisibility(View.INVISIBLE);
                if (task.isSuccessful()) {
                    Log.i("Ok", "Create User done");
//                    loginBool = !loginBool;
                    onSignUpLogin(getWindow().getDecorView());
                    Toast.makeText(MainActivity.this, "User Added.", Toast.LENGTH_LONG).show();
                } else {
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

    public void onSignUpLogin(View view) {
        username.setText("");
        password.setText("");
        Log.i("signUp", "pressed");
        if(!loginBool) {
            loginOrSignUpSection.setText("Sign Up");
            btnLoginSignup.setText("Login");
        } else {
            loginOrSignUpSection.setText("Login");
            btnLoginSignup.setText("Sign Up");
        }
        loginBool = !loginBool;
    }

    public void setupUI(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(MainActivity.this);
                    return false;
                }
            });
        }

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService( Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus().getWindowToken() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
