package com.hfad.phoneauthentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerificationCode extends AppCompatActivity {

    EditText mobileNumber, verificationCode;
    Button done, verify;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String userEnteredCode, smsCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null)
        {
            Toast.makeText(VerificationCode.this,"You are already logged in.",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(VerificationCode.this,HomeActivity.class);
            startActivity(intent);
            finish();
        }
        mobileNumber = findViewById(R.id.mobileNumber);
        verificationCode = findViewById(R.id.verificationCode);
        done = findViewById(R.id.done);
        verify = findViewById(R.id.verify);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mobileNumber.getText().toString().trim().isEmpty() || mobileNumber.getText().toString().length()!=10)
                {
                    mobileNumber.setError("Enter a valid 10 digit mobile number");
                    mobileNumber.requestFocus();
                }
                else
                {
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            "+91"+mobileNumber.getText().toString().trim(),
                            120,
                            TimeUnit.SECONDS,
                            TaskExecutors.MAIN_THREAD,
                            callBack
                    );
                }
            }
        });
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verificationCode.getText().toString().trim().isEmpty()) {
                    verificationCode.setError("Please enter a verification code here.");
                    verificationCode.requestFocus();
                }
                else
                {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(userEnteredCode, verificationCode.getText().toString().trim());
                    firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(VerificationCode.this,HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                Toast.makeText(VerificationCode.this,"Error Occured. Please enter the correct verification code.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        callBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public  void onCodeSent(String s,PhoneAuthProvider.ForceResendingToken forceResendingToken){
                super.onCodeSent(s,forceResendingToken);
                userEnteredCode = s;
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                smsCode = phoneAuthCredential.getSmsCode();
                verificationCode.setText(smsCode);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(VerificationCode.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        };

    }
}
