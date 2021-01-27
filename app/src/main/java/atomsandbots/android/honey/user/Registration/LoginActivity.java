package atomsandbots.android.honey.user.Registration;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import atomsandbots.android.honey.user.AdminPanel.AdminMainActivity;
import atomsandbots.android.honey.user.R;
import atomsandbots.android.honey.user.RoomDatabase.DataBaseAdapter;
import atomsandbots.android.honey.user.UI.ForgotPasswordActivity;
import atomsandbots.android.honey.user.UI.IntroActivity;
import atomsandbots.android.honey.user.UI.MainActivity;
import atomsandbots.android.honey.user.databinding.ActivityLoginBinding;

public class
LoginActivity extends AppCompatActivity {


    private String email, password;

    private FirebaseAuth mAuth;

    private GoogleSignInClient googleSignInClient;
    private final String TAG = "GoogleMessage";
    private static final int RC_SIGN_IN = 9009;

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View v = binding.getRoot();
        setContentView(v);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login");

        mAuth = FirebaseAuth.getInstance();

        GotoSignUp();

        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });

        binding.textAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textAdminClickFun();
            }
        });

        binding.textNotAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textAdminNotClickFun();
            }
        });


        binding.textForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

        GoogleSignIn();
    }

    private void textAdminNotClickFun() {
        binding.textForgotPassword.setVisibility(View.VISIBLE);
        binding.textAdmin.setVisibility(View.VISIBLE);
        binding.textNotAdmin.setVisibility(View.INVISIBLE);
        binding.googleSignIn.setVisibility(View.VISIBLE);
    }

    private void textAdminClickFun() {
        binding.textForgotPassword.setVisibility(View.INVISIBLE);
        binding.textAdmin.setVisibility(View.INVISIBLE);
        binding.textNotAdmin.setVisibility(View.VISIBLE);
        binding.googleSignIn.setVisibility(View.INVISIBLE);
    }

    private void Login() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait while we check your credential");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (binding.textAdmin.getVisibility() == View.VISIBLE) {
            if (Validation()) {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            checkIfEmailVerified();
                        } else {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        progressDialog.dismiss();
                    }
                });
            } else {
                progressDialog.dismiss();
            }
        } else {
            if (Validation()) {
                // check whatever email is from admin or users
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Admin");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String adminEmail = (String) snapshot.child("email").getValue();
                        if (email.equalsIgnoreCase(adminEmail)) {
                            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        SharedPreferences preferences = getApplicationContext().getSharedPreferences("LoginDetails", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putBoolean("isLogin", true);
                                        editor.putBoolean("isAdmin", true);
                                        editor.apply();
                                        Intent intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    progressDialog.dismiss();
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Incorrect email", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else {
                progressDialog.dismiss();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    //Send user to SignUp activity if they don't have an account
    private void GotoSignUp() {
        String text1 = "Don't have an account? ";
        String text2 = "Sign Up";
        SpannableString spannableString = new SpannableString(text1 + text2);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                finish();
            }
        };
        spannableString.setSpan(clickableSpan, text1.length(), text1.length() + text2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.textSignUp.setText(spannableString);
        binding.textSignUp.setLinkTextColor(Color.parseColor(getString(R.string.have_or_not_have_account_txtcolor)));
        binding.textSignUp.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private boolean Validation() {
        boolean valid = true;
        email = binding.editTextEmailAddress.getText().toString().trim();
        password = binding.editTextPassword.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmailAddress.setError("invalid");
            binding.editTextEmailAddress.requestFocus();
            valid = false;
        }
        if (email.isEmpty()) {
            binding.editTextEmailAddress.setError("empty");
            binding.editTextEmailAddress.requestFocus();
            valid = false;
        }
        if (password.length() < 6) {
            binding.editTextPassword.setError("short");
            binding.editTextPassword.requestFocus();
            valid = false;
        }
        if (password.isEmpty()) {
            binding.editTextPassword.requestFocus();
            binding.editTextPassword.setError("missing");
            valid = false;
        }
        return valid;
    }

    //Start Google signIn Auth here
    private void GoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);


        //Google sign-in in create account activity
        binding.googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContinueGoogle();
            }
        });

    }

    ProgressDialog pd;

    private void ContinueGoogle() {
        pd = new ProgressDialog(LoginActivity.this);
        pd.setMessage("Please Wait");
        pd.show();
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == RC_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            }
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }
    }


    // //Handle Google signIn Auth here
    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            FirebaseGoogleAuth(account);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("SignInError", e.toString());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }

    }


    //Firebase Authentication
    private void FirebaseGoogleAuth(final GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    boolean newUser = task.getResult().getAdditionalUserInfo().isNewUser();
                    SharedPreferences preferences = getApplicationContext().getSharedPreferences("LoginDetails", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("isLogin", true);
                    editor.putBoolean("isAdmin", false);
                    editor.apply();
                    if (newUser) {
                        //If the user is new execute this block
                        FirebaseUser user = mAuth.getCurrentUser();
                        DataBaseAdapter adapter = new DataBaseAdapter(LoginActivity.this);
                        assert user != null;
                        long id = adapter.insert(user.getDisplayName(), user.getEmail(), "", "", "", "", "");
                        if (id < 0) {
                            Toast.makeText(LoginActivity.this, "Data was not saved", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Successfully saved", Toast.LENGTH_SHORT).show();
                        }
                        updateUI(user);
                    } else {
                        //Continue with Sign up
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                    pd.dismiss();
                } else {
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        pd.dismiss();
    }

    private void updateUI(FirebaseUser user) {
        //Update user info
        Map<String, Object> map = new HashMap<>();
        map.put("Name", user.getDisplayName());
        map.put("Email", user.getEmail());
        map.put("Phone", "null");
        map.put("Postcode", "null");
        map.put("Country", "null");
        map.put("Address", "null");
        map.put("Image", "null");
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.child(user.getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(LoginActivity.this, IntroActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkIfEmailVerified() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified())
        {
            SharedPreferences preferences = getApplicationContext().getSharedPreferences("LoginDetails", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isLogin", true);
            editor.putBoolean("isAdmin", false);
            editor.apply();

            Intent intent = new Intent(LoginActivity.this, IntroActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else
        {
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.
//            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(LoginActivity.this, VerificationActivity.class));
            //restart this activity

        }
    }
}