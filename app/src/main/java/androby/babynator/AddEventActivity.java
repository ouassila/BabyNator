package androby.babynator;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddEventActivity extends AppCompatActivity {

    private Date date;
    private TextView lbl_date;
    private EditText hour, mTitle, mDescription;
    private int id_user, id_event, hours, minutes;
    private AddEventActivity.EventTask mEventTask = null;
    private AddEventActivity.GetEventTask mGetEventTask = null;
    private AddEventActivity.SetEventTask mSetEventTask = null;
    private JSONObject current_event;

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        id_user = getIntent().getIntExtra("ID_USER", 0);
        id_event = getIntent().getIntExtra("ID_EVENT", 0);
        Log.d("TMP", id_event+"");

        mTitle = (EditText) findViewById(R.id.title);
        mDescription = (EditText) findViewById(R.id.description);
        hour = (EditText) findViewById(R.id.hour);
        lbl_date = (TextView) findViewById(R.id.lbl_date);

        if(id_event > 0)
            getCurrentEventById(id_event);
        else{
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
            String new_date = sdf.format((Date)getIntent().getSerializableExtra("DATE"));
            lbl_date.setText(new_date);
            hours = 0;
            minutes = 0;
        }

        hour.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        AddEventActivity.this.hour.setText(selectedHour + ":" + selectedMinute);
                        hours = selectedHour;
                        minutes = selectedMinute;
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Séléctionnez l'heure ");
                mTimePicker.show();
            }
        });

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        myFab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(attemptEvent() && current_event != null && current_event.length() > 0){
                    sendSetRequest();
                }
                else if(attemptEvent()){
                    sendAddRequest();
                }
            }
        });
       FloatingActionButton myFab_Cancel = (FloatingActionButton) findViewById(R.id.floatingActionButton_Cancel);
       myFab_Cancel.setOnClickListener(new OnClickListener() {
           @Override
           public void onClick(View view) {
           Intent myIntent = new Intent(AddEventActivity.this, CalendarActivity.class);
           myIntent.putExtra("ID_USER", id_user);
           startActivity(myIntent);
           }
       });
    }

    private void getCurrentEventById(int id_event) {
        mGetEventTask = new AddEventActivity.GetEventTask(false, id_event);
        mGetEventTask.execute((Void) null);
    }

    private void sendAddRequest(){
        try {
            date = (Date) getIntent().getSerializableExtra("DATE");

            date.setHours(hours);
            date.setMinutes(minutes);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String new_date = sdf.format(date);

            mEventTask = new AddEventActivity.EventTask(mTitle.getText().toString(),
                    mDescription.getText().toString(), false, new_date, id_user);
            mEventTask.execute((Void) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendSetRequest(){
        try {
            String strDate = lbl_date.getText().toString()+ " "+hours+":"+minutes;
            SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy HH:mm");
            SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date date = format.parse(strDate);

            mSetEventTask = new AddEventActivity.SetEventTask(mTitle.getText().toString(),
                    mDescription.getText().toString(), false, format2.format(date), id_event, id_user);
            mSetEventTask.execute((Void) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Boolean attemptEvent() {
        boolean cancel = false;
        View focusView = null;

        if (mEventTask != null) {
            return false;
        }
        if (TextUtils.isEmpty(hour.getText().toString())) {
            hour.setError(getString(R.string.error_field_required));
            focusView = hour;
            cancel = true;
        }
        if (TextUtils.isEmpty(mTitle.getText().toString())) {
            mTitle.setError("Ce champ est obligatoire");
            focusView = mTitle;
            cancel = true;
        }
        if (TextUtils.isEmpty(mDescription.getText().toString())) {
            mDescription.setError("Ce champ est obligatoire");
            focusView = mDescription;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
            return false;
        }
        return true;
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

    public class GetEventTask extends AsyncTask<Void, Void, Boolean> {

        private boolean mConnection;
        private int mId;

        GetEventTask(boolean connection, int id) {
            mConnection = connection;
            mId = id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL("http://"+LoginActivity.IP_SERVER+"/RestServer/babyNator/events/get");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Language", "fr-FR");
                connection.setRequestProperty("Content-Type","application/json");
                connection.setUseCaches (false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                //Send request
                DataOutputStream wr = new DataOutputStream (
                        connection.getOutputStream ());
                mConnection = true;

                wr.writeBytes (id_event+"");
                wr.flush ();
                wr.close ();

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
                    current_event = new JSONObject(response.toString());
                    Log.e("event",current_event.toString());
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
                Log.e("httptest3",Log.getStackTraceString(ex));
                return false;
            }
            return true;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            if(success){
                try {
                    mTitle.setText(current_event.getString("title"));
                    mDescription.setText(current_event.getString("description"));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = sdf.parse(current_event.getString("current_date"));

                    SimpleDateFormat sdf_out = new SimpleDateFormat("dd MMMM yyyy");
                    String new_date = sdf_out.format(date);
                    lbl_date.setText(new_date);

                    sdf_out = new SimpleDateFormat("HH:mm");
                    new_date = sdf_out.format(date);
                    hour.setText(new_date);
                    String[] datas = new_date.split(":");
                    hours = Integer.parseInt(datas[0]);
                    minutes = Integer.parseInt(datas[1]);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public class SetEventTask extends AsyncTask<Void, Void, Boolean> {

        private final String mTitle;
        private final String mDescription;
        private final String mDate;
        private boolean mConnection;
        private int mId, mId_User;

        SetEventTask(String title, String description, boolean connection, String date, int id, int id_user) {
            mTitle = title;
            mDescription = description;
            mConnection = connection;
            mDate = date;
            mId = id;
            mId_User = id_user;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                URL url = new URL("http://" + LoginActivity.IP_SERVER + "/RestServer/babyNator/events/set");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Language", "fr-FR");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                //Send request
                DataOutputStream wr = new DataOutputStream(
                        connection.getOutputStream());
                JSONObject eventToSet = new JSONObject();
                mConnection = true;
                try {
                    eventToSet.put("id", this.mId);
                    eventToSet.put("current_date", this.mDate);
                    eventToSet.put("title", this.mTitle);
                    eventToSet.put("description", this.mDescription);
                    eventToSet.put("id_user", this.mId_User);
                } catch (Exception e) {
                    return false;
                }
                wr.writeBytes(eventToSet.toString());
                wr.flush();
                wr.close();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                try {
                    JSONObject eventAdded = new JSONObject(response.toString());
                    Log.e("event_set", eventAdded.toString());
                } catch (Exception e) {
                    return false;
                }
            } catch (MalformedURLException ex) {
                Log.e("httptest", Log.getStackTraceString(ex));
                return false;
            } catch (ConnectException ce) {
                Log.e("httptest2", Log.getStackTraceString(ce));
                return false;
            } catch (IOException ex) {
                Log.e("httptest3", Log.getStackTraceString(ex));
                return false;
            }
            Intent myIntent = new Intent(AddEventActivity.this, CalendarActivity.class);
            myIntent.putExtra("ID_USER", this.mId_User);
            startActivity(myIntent);
            return true;
        }
    }

    public class EventTask extends AsyncTask<Void, Void, Boolean> {

        private final String mTitle;
        private final String mDescription;
        private final String mDate;
        private boolean mConnection;
        private int mId_user;

        EventTask(String title, String description, boolean connection, String date, int id_user) {
            mTitle = title;
            mDescription = description;
            mConnection = connection;
            mDate = date;
            mId_user = id_user;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                URL url = new URL("http://"+LoginActivity.IP_SERVER+"/RestServer/babyNator/events/add");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Language", "fr-FR");
                connection.setRequestProperty("Content-Type","application/json");
                connection.setUseCaches (false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                //Send request
                DataOutputStream wr = new DataOutputStream (
                        connection.getOutputStream ());
                JSONObject eventToAdd = new JSONObject();
                mConnection = true;
                try {
                    eventToAdd.put("id", 0);
                    eventToAdd.put("current_date", this.mDate);
                    eventToAdd.put("title", this.mTitle);
                    eventToAdd.put("description", this.mDescription);
                    eventToAdd.put("id_user", this.mId_user);
                } catch (Exception e){
                    return false;
                }
                wr.writeBytes (eventToAdd.toString());
                wr.flush ();
                wr.close ();

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
                    JSONObject eventAdded = new JSONObject(response.toString());
                    Log.e("event_added",eventAdded.toString());
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
                Log.e("httptest3",Log.getStackTraceString(ex));
                return false;
            }
            Intent myIntent = new Intent(AddEventActivity.this, ListActivity.class);
            myIntent.putExtra("ID_USER", mId_user);
            startActivity(myIntent);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success){
                Intent myIntent = new Intent(AddEventActivity.this, CalendarActivity.class);
                myIntent.putExtra("ID_USER", id_user);
                finish();
                startActivity(myIntent);
            }
        }

        @Override
        protected void onCancelled() {

        }
    }
}
