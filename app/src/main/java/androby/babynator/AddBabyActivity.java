package androby.babynator;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static androby.babynator.R.id.nickname;
import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class AddBabyActivity extends AppCompatActivity implements OnClickListener {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;


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
    private int id_user;
    private Button btn_search;
    private static int RESULT_LOAD_IMAGE = 1;
    private ImageView preview;
    private TextView path_preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_baby);
        populateAutoComplete();

        id_user = getIntent().getIntExtra("ID_USER", 0);

        nickName = (EditText) findViewById(nickname);
        birthday = (EditText) findViewById(R.id.birthday);
        length = (EditText) findViewById(R.id.length);
        weight = (EditText) findViewById(R.id.weight);

        dateFormatter = new SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE);
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

        FloatingActionButton myFab_Cancel = (FloatingActionButton) findViewById(R.id.floatingActionButton_Cancel);
        myFab_Cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(AddBabyActivity.this, ListActivity.class);
                myIntent.putExtra("ID_USER", id_user);
                startActivity(myIntent);
            }
        });

        preview = (ImageView) findViewById(R.id.lbl_img);
        path_preview = (TextView)findViewById(R.id.path_preview);

        btn_search = (Button) findViewById(R.id.btn_search);
        btn_search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            preview.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            path_preview.setText(picturePath);
        }
    }

    private void setDateTimeField() {
        birthday.setOnClickListener(this);
        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, new OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                birthday.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    private void attemptAddBaby() {
        if (mAddBabyTask != null) {
            return;
        }
        String sNickname = nickName.getText().toString();
        String sBirthday = birthday.getText().toString();
        String sSexe = sexe.getText().toString();
        String sLength = length.getText().toString();
        String sWeight = weight.getText().toString();
        String path_picture = path_preview.getText().toString();

        weight.setError(null);
        nickName.setError(null);
        birthday.setError(null);
        sexe.setError(null);
        length.setError(null);

        boolean cancel = false;
        View focusView = null;
        if (sNickname.isEmpty() || sNickname == ""){
            nickName.setError("Champs vide");
            focusView = nickName;
            cancel=true;
        }

        if (sBirthday.isEmpty() || sBirthday == ""){
            birthday.setError("Champs vide");
            focusView = birthday;
            cancel=true;
        }

        if (sSexe.isEmpty() || sSexe == ""){
            sexe.setError("Champs vide");
            focusView = sexe;
            cancel=true;
        }

        if (sLength.isEmpty() || sLength == ""){
            length.setError("Champs vide");
            focusView = length;
            cancel=true;

        }
        if (sWeight.isEmpty() || sWeight == ""){
            weight.setError("Champs vide");
            focusView = weight;
            cancel=true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mAddBabyTask = new AddBabyTask(sLength.toString(), sWeight.toString(), sSexe.toString(), sBirthday.toString(), sNickname.toString(), path_picture.toString());
            mAddBabyTask.execute((Void) null);
        }
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
        private final String picture;
        private int id_baby;
        private int responseCode;

        AddBabyTask(String length, String weight, String sexe, String birthday, String nickname, String picture) {
            this.sexe=sexe;
            this.birthday=birthday;
            this.nickname=nickname;
            this.length = length;
            this.weight = weight;
            this.picture = picture;
            id_baby = 0;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
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
                    babyToAdd.put("picture", picture);
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

            Log.e("Baby added",sexe+" " +birthday+" " + nickname + " " + length + " " + weight+ " "+ picture);
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
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAddBabyTask = null;
           // showProgress(false);

            if (success && responseCode == 202) {
                Toast.makeText(getApplicationContext(), "Les données de votre bébé ont bien été enregistrées", Toast.LENGTH_LONG).show();
                Intent myIntent = new Intent(AddBabyActivity.this, ListActivity.class);
                myIntent.putExtra("ID_USER", id_user);
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

