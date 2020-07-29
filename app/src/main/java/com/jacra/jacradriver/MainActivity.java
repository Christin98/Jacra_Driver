package com.jacra.jacradriver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jacra.jacradriver.model.User;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class MainActivity extends AppCompatActivity {

    private Button btnSignIn;
    private Button btnRegister;
    private RelativeLayout rootLayout;

    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference users;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        btnSignIn = findViewById(R.id.btnSignIn);
        btnRegister = findViewById(R.id.btnRegister);
        rootLayout = findViewById(R.id.rootLayout);

        btnRegister.setOnClickListener(v -> showRegisterDialog());
        btnSignIn.setOnClickListener(v -> showLoginDialog());
    }

    private void showLoginDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("Please use email to Sign in");

        LayoutInflater inflater = LayoutInflater.from(this);
        View loginLayout = inflater.inflate(R.layout.layout_login, null);

        MaterialEditText edtEmail = loginLayout.findViewById(R.id.edtEmail);
        MaterialEditText edtPassword = loginLayout.findViewById(R.id.edtPassword);

        dialog.setView(loginLayout);

        dialog.setPositiveButton("SIGN IN", (dialog12, which) -> {

            dialog12.dismiss();

            btnSignIn.setEnabled(false);

            if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                Snackbar.make(rootLayout, "Please enter email address.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                Snackbar.make(rootLayout, "Please enter password.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (edtPassword.getText().toString().length() < 6) {
                Snackbar.make(rootLayout, "Password too short!!!.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(MainActivity.this).build();
            waitingDialog.show();

            auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                    .addOnSuccessListener(authResult -> {
                        waitingDialog.dismiss();
                        startActivity(new Intent(MainActivity.this, DriverHomeActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        waitingDialog.dismiss();
                        Snackbar.make(rootLayout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        btnSignIn.setEnabled(true);
                    });

        });

        dialog.setNegativeButton("CANCEL", (dialog1, which) -> dialog1.dismiss());
        dialog.show();
    }

    private void showRegisterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage("Please use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View registerLayout = inflater.inflate(R.layout.layout_register, null);

        MaterialEditText edtEmail = registerLayout.findViewById(R.id.edtEmail);
        MaterialEditText edtPassword = registerLayout.findViewById(R.id.edtPassword);
        MaterialEditText edtName = registerLayout.findViewById(R.id.edtName);
        MaterialEditText edtPhone = registerLayout.findViewById(R.id.edtPhone);

        dialog.setView(registerLayout);

        dialog.setPositiveButton("REGISTER", (dialog12, which) -> {

            dialog12.dismiss();

            if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                Snackbar.make(rootLayout, "Please enter email address.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                Snackbar.make(rootLayout, "Please enter password.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (edtPassword.getText().toString().length() < 6) {
                Snackbar.make(rootLayout, "Password too short!!!.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(edtName.getText().toString())) {
                Snackbar.make(rootLayout, "Please enter name.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(edtPhone.getText().toString())) {
                Snackbar.make(rootLayout, "Please enter phone number.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                    .addOnSuccessListener(authResult -> {
                        User user = new User(edtEmail.getText().toString(), edtPassword.getText().toString(), edtName.getText().toString(), edtPhone.getText().toString());
                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user)
                                .addOnSuccessListener(aVoid -> Snackbar.make(rootLayout, "Register Successfully!!!", Snackbar.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Snackbar.make(rootLayout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Snackbar.make(rootLayout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT).show());
        });

        dialog.setNegativeButton("CANCEL", (dialog1, which) -> dialog1.dismiss());
        dialog.show();
    }
}