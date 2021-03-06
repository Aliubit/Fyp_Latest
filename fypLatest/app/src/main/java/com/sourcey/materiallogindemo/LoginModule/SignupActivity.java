package com.sourcey.materiallogindemo.LoginModule;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sourcey.materiallogindemo.HomeModule.HomeActivity;
import com.sourcey.materiallogindemo.HomeModule.HomeMapActivity;
import com.sourcey.materiallogindemo.Network.ConnectionDetector;
//import com.sourcey.materiallogindemo.Network.ConnnectionDetector;
import com.sourcey.materiallogindemo.Network.HttpHandler;
import com.sourcey.materiallogindemo.R;

import java.util.HashMap;

import com.sourcey.materiallogindemo.Helpers.JsonParser;
import com.sourcey.materiallogindemo.Model.SignupModel;
import com.sourcey.materiallogindemo.Utility.AppConstants;
import com.sourcey.materiallogindemo.Utility.MemorizerUtil;
//import com.sourcey.materiallogindemo.Utility.MemorizerUtils;

import butterknife.ButterKnife;
import butterknife.Bind;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class SignupActivity extends Activity {
    private static final String TAG = "SignupActivity";


    ProgressDialog progressDialog;

    @Bind(R.id.input_name)
    EditText _nameText;
    //@Bind(R.id.input_address) EditText _addressText;
    @Bind(R.id.input_email)
    EditText _emailText;
    @Bind(R.id.input_mobile)
    EditText _mobileText;
    @Bind(R.id.input_password)
    EditText _passwordText;
    @Bind(R.id.input_reEnterPassword)
    EditText _reEnterPasswordText;
    @Bind(R.id.btn_signup)
    Button _signupButton;
    @Bind(R.id.link_login)
    TextView _loginLink;

    @Bind(R.id.signUpLayout)
    LinearLayout signUpLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);


        signUpLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MemorizerUtil.hideSoftInput(v,getApplicationContext());
                return false;
            }
        });






        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        /*final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();*/

        String name = _nameText.getText().toString();
        // String address = _addressText.getText().toString();
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();


        // TODO: Implement your own signup logic here.

        ConnectionDetector connnectionDetector = new ConnectionDetector(getApplicationContext());
        Boolean isInternetPresent = connnectionDetector.isConnectingToInternet();
        if (isInternetPresent) {
            new SignUpRequest(name, email, mobile, password).execute();
        }
        else {
            MemorizerUtil.displayToast(getApplicationContext(),"No Internet Connection");
        }
        onSignupSuccess();

       /* new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        onSignupSuccess();
                        // onSignupFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);*/
    }


    public void onSignupSuccess() {
        _signupButton.setEnabled(true);

        setResult(RESULT_OK, null);
        //finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
       finish();

    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        //String address = _addressText.getText().toString();
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();



        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        /*if (address.isEmpty()) {
            _addressText.setError("Enter Valid Address");
            valid = false;
        } else {
            _addressText.setError(null);
        }*/


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (mobile.isEmpty() || mobile.length() != 10) {
            _mobileText.setError("Enter Valid Mobile Number");
            valid = false;
        } else {
            _mobileText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }

        return valid;
    }


    public class SignUpRequest extends AsyncTask<String, Void, String> {

        private String name, email, mobile, password;

        public SignUpRequest(String name, String email, String mobile, String password) {
            // this.id = id;
            this.name = name;
            this.email = email;
            this.mobile = mobile;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(SignupActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Creating Account...");
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try {
                response = signUp_Api(name, email, mobile, password);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            SignupModel model = JsonParser.getInstance().ParseSignupResponse(s);

            if (model != null) {
                switch (model.getErrorCode()) {
                    case 200:

                        ///

                        Intent intent = new Intent(getApplicationContext(), HomeMapActivity.class);
                        ComponentName cn = intent.getComponent();
                        Intent mainIntent = IntentCompat.makeRestartActivityTask(cn);
                        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("email",email);
                        editor.commit();
                        startActivity(mainIntent);
                        /*
                        ///
                        //Yahan Khulwa de Activity
                        Intent intent = new Intent(SignupActivity.this, HomeMapActivity.class);
                        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("email",email);
                        editor.commit();
                        finish();
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(intent);
                        */
                        break;
                    default:
                        //Error Message
                        Toast.makeText(getApplicationContext(),"Something went wrong",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            else {

            }

            if(progressDialog != null){
                progressDialog.dismiss();
            }

        }

    }

    public String signUp_Api(String name, String email, String mobile, String password) {
        String response = "";
        try {

            HttpHandler httpHandler = new HttpHandler();
            HashMap<String, String> params = new HashMap<>();
            params.put("name", name);
            params.put("email", email);
            params.put("phone_number", mobile);
            params.put("password", password);
            response = httpHandler.performPostCall(AppConstants.API_SIGNUP, params);
            //response = httpHandler.performPostCall(AppConstants.API_SIGNUP, params);

        } catch (Exception e) {
            Log.e("log_tag", "Error in http connection " + e.toString());
        }

        return response;

    }

}
