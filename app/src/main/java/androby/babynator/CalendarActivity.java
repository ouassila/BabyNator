package androby.babynator;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.DatePicker;

import org.json.JSONArray;
import org.json.JSONException;
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
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private DatePicker mDatePicker;
    private Calendar calendar;
    private int id_user;
    private CalendarActivity.CalendarTask mCalendarTask = null;
    private JSONArray listEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //bouton retour arriere
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        attemptCalendar();

        //mettre en francais
        String languageToLoad  = "fr";
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        this.setContentView(R.layout.activity_calendar);

        mDatePicker = (DatePicker) findViewById(R.id.datePicker);
        calendar = Calendar.getInstance();



        //addCalendarEvent(calendar);

        calendar.setTimeInMillis(System.currentTimeMillis());
        //define min max
        mDatePicker.setMinDate(calendar.getTimeInMillis());
        mDatePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
            Intent myIntent = new Intent(CalendarActivity.this, AddEventActivity.class);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            Date date = calendar.getTime();
/*
            try{
                for(int i=0; i < listEvents.length(); i++){
                    JSONObject row = listEvents.getJSONObject(i);
                    Log.d("ROW", row.toString());
                }
            }
            catch(JSONException e){

            }
            */
            if(eventOnDate(date) != null){
                addCalendarEvent(calendar);
            }
            else{
                myIntent.putExtra("DATE", date);
                myIntent.putExtra("ID_USER", id_user);
                startActivity(myIntent);
            }
            }
        });
    }

    public void addCalendarEvent(Calendar cal){

        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", cal.getTimeInMillis());
        intent.putExtra("allDay", true);
        intent.putExtra("rrule", "FREQ=YEARLY");
        intent.putExtra("endTime", cal.getTimeInMillis()+60*60*1000);
        intent.putExtra("title", "Test Event");
        intent.putExtra("description", "This is a sample description");
        startActivity(intent);
    }
    public JSONObject eventOnDate(Date date){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String new_date = sdf.format(date);
        try{
            for(int i=0; i < listEvents.length(); i++){
                JSONObject row = listEvents.getJSONObject(i);
                Log.d("ROW", row.toString());
                if(new_date.equals(row.getString("current_date"))){
                    return row;
                }
            }
        }
        catch(JSONException e){
            return null;
        }
        return null;
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
    private void attemptCalendar() {
        if (mCalendarTask != null) {
            return;
        }
        try {
            id_user = getIntent().getIntExtra("ID_USER", 0);
            mCalendarTask = new CalendarActivity.CalendarTask(id_user, false);
            mCalendarTask.execute((Void) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class CalendarTask extends AsyncTask<Void, Void, Boolean> {

        private int mId_user;
        private boolean mConnection;

        CalendarTask(int id_user, boolean connection) {
            mConnection = connection;
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
                URL url = new URL("http://"+LoginActivity.IP_SERVER+"/RestServer/babyNator/events/list");
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
                JSONObject idToSend = new JSONObject();
                mConnection = true;

                wr.writeBytes (this.mId_user+"");
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
                    JSONArray list = new JSONArray(response.toString());
                    listEvents = list;
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

        }
        @Override
        protected void onCancelled() {

        }
    }
}
