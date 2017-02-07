package androby.babynator;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static androby.babynator.R.id.nickname;
import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class AddBabyActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, OnClickListener {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;


    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private AddBabyTask mAddBabyTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText nickName;
    private RadioGroup radioSexGroup;
    private EditText age;
    private EditText birthday;
    private EditText length;
    private EditText weight;
    private Switch sexe;
    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat dateFormatter;
   // private View mProgressView;
    //private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_baby);
        // Set up the login form.
        //age = (EditText) findViewById(R.id.age);
       // radioSexGroup = (RadioGroup) findViewById(R.id.radioGroupSexe);
        populateAutoComplete();

        nickName = (EditText) findViewById(nickname);
        birthday = (EditText) findViewById(R.id.birthday);
        length = (EditText) findViewById(R.id.length);
        weight = (EditText) findViewById(R.id.weight);

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        sexe = (Switch)  findViewById(R.id.sexe);
        sexe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("Switch State=", ""+isChecked);
                if (isChecked){
                    sexe.setText("Fille");
                }
                else {
                    sexe.setText("Garçon");
                }
            }

        });
        birthday.setInputType(InputType.TYPE_NULL);
        birthday.requestFocus();
        setDateTimeField();
        FloatingActionButton mAddBabyButton = (FloatingActionButton) findViewById(R.id.add_baby_button);
        mAddBabyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptAddBaby();
            }
        });

        //mLoginFormView = findViewById(R.id.baby_form);
       // mProgressView = findViewById(R.id.baby_progress);
    }

    private void setDateTimeField() {
        birthday.setOnClickListener(this);
      //  toDateEtxt.setOnClickListener(this);

        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, new OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                birthday.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        /*toDatePickerDialog = new DatePickerDialog(this, new OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                toDateEtxt.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));*/
    }
    @Override
    public void onClick(View view) {
        if(view == birthday) {
            datePickerDialog.show();
        }
        else {
            Log.e("**donnéed add baby ***","ko");
        }
    }
    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptAddBaby() {
        if (mAddBabyTask != null) {
            return;
        }


        String sNickname = nickName.getText().toString();
        String sBirthday = birthday.getText().toString();
        String sSexe = sexe.getText().toString();
        String sLength = length.getText().toString();
        String sWeight = weight.getText().toString();


        // Reset errors.
      /*  mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAddBabyTask = new AddBabyTask(email, password);
            mAddBabyTask.execute((Void) null);
        }*/
        mAddBabyTask = new AddBabyTask(sLength.toString(), sWeight.toString(), sSexe.toString(), sBirthday.toString(), sNickname.toString());
        mAddBabyTask.execute((Void) null);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
  /*  @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }*/

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
       /* ArrayAdapter<String> adapter =
                new ArrayAdapter<>(AddBabyActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);*/
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class AddBabyTask extends AsyncTask<Void, Void, Boolean> {

        private final String sexe;
        private final String birthday;
        private final String nickname;
        private final String length;
        private final String weight;
        private int id_baby;
        private int responseCode;

        AddBabyTask(String length, String weight, String sexe, String birthday, String nickname) {
            this.sexe=sexe;
            this.birthday=birthday;
            this.nickname=nickname;
            this.length = length;
            this.weight = weight;
            id_baby = 0;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

         /*   try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }
String age, String sexe, String birthday, String nickname
            // TODO: register the new account here.*/
            if (addBaby()){
                return addData();
            }
            else
                return false;
        }

        private boolean addBaby(){
            try {
                URL url = new URL("http://"+LoginActivity.IP_SERVER+"/RestServer/babyNator/babies/add");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Language", "en-US");
                connection.setRequestProperty("Content-Type","application/json");
                connection.setUseCaches (false);
                connection.setDoInput(true);
                connection.setDoOutput(true);


                //Send request
                DataOutputStream wr = new DataOutputStream (
                        connection.getOutputStream ());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));
                JSONObject babyToAdd = new JSONObject();
                JSONObject dataToAdd = new JSONObject();
                //  mConnection = true;
                try {
                    babyToAdd.put("id", 0);
                    babyToAdd.put("birthday", birthday.toString());
                    babyToAdd.put("name", nickname);
                    babyToAdd.put("gender", sexe);
                    babyToAdd.put("id_user", getIntent().getIntExtra("ID_USER", 0));
                } catch (Exception e){
                    return false;
                }
                writer.write(babyToAdd.toString());
                //   wr.writeBytes (dataToAdd.toString());
                writer.flush ();
                writer.close ();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                try {
                    JSONObject babyCreated = new JSONObject(response.toString());
                    Log.e("babyCreated",babyCreated.toString());
                    id_baby = babyCreated.getInt("id");
                }
                catch(Exception e){
                    return false;
                }

            }
            catch (MalformedURLException ex) {
                Log.e("httptest",Log.getStackTraceString(ex));
                return false;
            }
            catch (ConnectException ce){
                Log.e("httptest2",Log.getStackTraceString(ce));
                return false;
            }
            catch (IOException ex) {
                Log.e("httptest2",Log.getStackTraceString(ex));
                return false;
            }

            Log.e("**donnéed add baby ***",sexe+" " +birthday+" " + nickname + " " + length + " " + weight);
            return true;
        }

        private boolean addData(){
            try {
                URL url = new URL("http://"+LoginActivity.IP_SERVER+"/RestServer/babyNator/datas/addBirthday");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Language", "en-US");
                connection.setRequestProperty("Content-Type","application/json");
                connection.setUseCaches (false);
                connection.setDoInput(true);
                connection.setDoOutput(true);


                //Send request
                DataOutputStream wr = new DataOutputStream (
                        connection.getOutputStream ());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));
                JSONObject babyToAdd = new JSONObject();
                JSONObject dataToAdd = new JSONObject();
                //  mConnection = true;
                try {
                    dataToAdd.put("length", Integer.parseInt(length));
                    dataToAdd.put("weight", Integer.parseInt(weight));
                    dataToAdd.put("id_baby", id_baby);
                    dataToAdd.put("current_date", birthday.toString());


                } catch (Exception e){
                    return false;
                }
                writer.write(dataToAdd.toString());
                //   wr.writeBytes (dataToAdd.toString());
                writer.flush ();
                writer.close ();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                responseCode = connection.getResponseCode();

            }
            catch (MalformedURLException ex) {
                Log.e("httptest",Log.getStackTraceString(ex));
                return false;
            }
            catch (ConnectException ce){
                Log.e("httptest2",Log.getStackTraceString(ce));
                return false;
            }
            catch (IOException ex) {
                Log.e("httptest2",Log.getStackTraceString(ex));
                return false;
            }

            Log.e("**donnéed add baby ***",sexe+" " +birthday+" " + nickname + " " + length + " " + weight);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAddBabyTask = null;
           // showProgress(false);

            if (success && responseCode == 202) {
                Toast.makeText(getApplicationContext(), "Votre bébé a bien été enregitré", Toast.LENGTH_LONG).show();
                Intent myIntent = new Intent(AddBabyActivity.this, ListActivity.class);
                startActivity(myIntent);
            } else {
                Toast.makeText(getApplicationContext(), "Un problème a été rencontré lors de l'enregistrement de votre bébé", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAddBabyTask = null;
           // showProgress(false);
        }
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view != null && view instanceof EditText) {
                Rect r = new Rect();
                view.getGlobalVisibleRect(r);
                int rawX = (int)ev.getRawX();
                int rawY = (int)ev.getRawY();
                if (!r.contains(rawX, rawY)) {
                    view.clearFocus();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}

