package androby.babynator;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
import java.util.TimeZone;

public class AddEventActivity extends AppCompatActivity {

    private Date date;
    private TextView lbl_date;
    private EditText hour, mTitle, mDescription;
    private int id_user, hours, minutes;
    private AddEventActivity.EventTask mEventTask = null;

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        //bouton retour arriere
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTitle = (EditText) findViewById(R.id.title);
        mDescription = (EditText) findViewById(R.id.description);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+01:00"));
            String new_date = sdf.format((Date)getIntent().getSerializableExtra("DATE"));

            lbl_date = (TextView) findViewById(R.id.lbl_date);
            lbl_date.setText(new_date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        hours = 0;
        minutes = 0;

        hour = (EditText) findViewById(R.id.hour);
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
                attemptEvent();
            }
        });

    }

    //Méthode qui se déclenchera au clic sur un item
    public boolean onOptionsItemSelected(MenuItem item) {
        //On regarde quel item a été cliqué grâce à son id et on déclenche une action
        Intent myIntent;
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return false;
    }
    private void attemptEvent() {
        if (mEventTask != null) {
            return;
        }
        try {
            date = (Date)getIntent().getSerializableExtra("DATE");
            date.setHours(hours);
            date.setMinutes(minutes);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String new_date = sdf.format(date);
            Log.e("DATE", new_date);

            id_user = getIntent().getIntExtra("ID_USER", 0);
            mEventTask = new AddEventActivity.EventTask(mTitle.getText().toString(),
                    mDescription.getText().toString(), false, new_date, id_user );
            mEventTask.execute((Void) null);
        } catch (Exception e) {
            e.printStackTrace();
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
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }
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

        }

        @Override
        protected void onCancelled() {

        }
    }
}
