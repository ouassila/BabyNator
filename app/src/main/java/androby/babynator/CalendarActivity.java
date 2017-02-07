package androby.babynator;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static androby.babynator.R.id.date;

public class CalendarActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private int id_user;
    private CalendarActivity.CalendarTask mCalendarTask = null;
    private JSONArray listEvents;
    private CaldroidFragment caldroidFragment;
    private ListView scroller;
    private List<Integer> list_id_events;
    private ImageButton btn_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        id_user = getIntent().getIntExtra("ID_USER", 0);
        list_id_events = new ArrayList<Integer>();

        caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        caldroidFragment.setArguments(args);

        btn_delete = (ImageButton) findViewById(R.id.btn_delete);

        scroller = (ListView) findViewById(R.id.list_events);
        scroller.setOnItemClickListener(this);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendarView, caldroidFragment);
        t.commit();
        caldroidFragment.setCaldroidListener(listener);
    }

    private void attemptCalendar(int id_user) {
        if (mCalendarTask != null) {
            return;
        }
        try {
            mCalendarTask = new CalendarActivity.CalendarTask(id_user, false);
            mCalendarTask.execute((Void) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    final CaldroidListener listener = new CaldroidListener() {

        @Override
        public void onSelectDate(Date date, View view) {
            attemptCalendar(id_user);
            //List<String> titles = new ArrayList<String>();
            List<Event> events = new ArrayList<Event>();
            list_id_events = new ArrayList<Integer>();
            Calendar cal = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf_output = new SimpleDateFormat("dd MMMM à HH'h'mm");
            try{
                for(int i=0; i < listEvents.length(); i++){
                    JSONObject row = listEvents.getJSONObject(i);
                    String dateStr = row.getString("current_date");
                    cal.setTime(sdf.parse(dateStr));
                    cal2.setTime(date);

                    if(cal.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH) && cal.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) && cal.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) {
                        //titles.add(sdf_output.format(cal.getTime()) + " : "+row.getString("title"));
                        events.add(new Event(Integer.parseInt(row.getString("id")), btn_delete, sdf_output.format(date), row.getString("title")));
                        list_id_events.add(Integer.parseInt(row.getString("id")));
                    }
                }
            }
            catch(Exception e){
                Log.e("Error", e.toString());
            }

            if(events.size() > 0){
                EventAdapter adapter = new EventAdapter(CalendarActivity.this, events, CalendarActivity.this, id_user);
                scroller.setAdapter(adapter);
            }
            else {
                Intent myIntent = new Intent(CalendarActivity.this, AddEventActivity.class);
                myIntent.putExtra("DATE", date);
                myIntent.putExtra("ID_USER", id_user);
                startActivity(myIntent);
            }
        }

        @Override
        public void onChangeMonth(int month, int year) {
            attemptCalendar(id_user);
            List<String> titles = new ArrayList<String>();
            List<Event> events = new ArrayList<Event>();
            list_id_events = new ArrayList<Integer>();
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf_output = new SimpleDateFormat("dd MMMM à HH'h'mm");

            try{
                for(int i=0; i < listEvents.length(); i++){
                    JSONObject row = listEvents.getJSONObject(i);
                    String dateStr = row.getString("current_date");
                    cal.setTime(sdf.parse(dateStr));

                    if((cal.get(Calendar.MONTH)+1) == month && cal.get(Calendar.YEAR) == year) {
                        events.add(new Event(Integer.parseInt(row.getString("id")), btn_delete, sdf_output.format(date), row.getString("title")));
                        list_id_events.add(Integer.parseInt(row.getString("id")));
                    }
                }
            }
            catch(Exception e){
                Log.e("Error", e.toString());
            }
            //if(titles.size() < 0)
             //   titles.add("Aucun rendez-vous enregistré");

            //ArrayAdapter listAdapter = new ArrayAdapter<String>(CalendarActivity.this, R.layout.simplerow, titles);
            //scroller.setAdapter(listAdapter);
            EventAdapter adapter = new EventAdapter(CalendarActivity.this, events, CalendarActivity.this, id_user);
            scroller.setAdapter(adapter);
        }

        @Override
        public void onLongClickDate(Date date, View view) {

        }

        @Override
        public void onCaldroidViewCreated() {
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Log.d("TEST", "ICI3");

        Intent myIntent = new Intent(CalendarActivity.this, AddEventActivity.class);
        myIntent.putExtra("ID_EVENT", list_id_events.get(position));
        myIntent.putExtra("ID_USER", id_user);
        startActivity(myIntent);
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
                    listEvents = new JSONArray(response.toString());
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf_output = new SimpleDateFormat("dd MMMM à HH'h'mm");

            list_id_events = new ArrayList<Integer>();
            //List<String> titles = new ArrayList<String>();
            Calendar cal = Calendar.getInstance();
            List<Event> events = new ArrayList<Event>();

            if (success) {
                try{

                    for(int i=0; i < listEvents.length(); i++){
                        JSONObject row = listEvents.getJSONObject(i);
                        String dateStr = row.getString("current_date");
                        Date date = sdf.parse(dateStr);
                        ColorDrawable green = new ColorDrawable(Color.GREEN);
                        caldroidFragment.setBackgroundDrawableForDate(green, date);

                        events.add(new Event(Integer.parseInt(row.getString("id")), btn_delete, sdf_output.format(date), row.getString("title")));
                        //titles.add(sdf_output.format(date) + " : "+row.getString("title"));

                        list_id_events.add(Integer.parseInt(row.getString("id")));
                    }
                    caldroidFragment.refreshView();

                    //if(events.size() < 0)
                    //    events.add(new Event(0, null, "", "Aucun rendez-vous enregistré"));
                    //titles.add("Aucun rendez-vous enregistré");

                    EventAdapter adapter = new EventAdapter(CalendarActivity.this, events, CalendarActivity.this, id_user);
                    scroller.setAdapter(adapter);

                }
                catch(Exception e){
                    Log.e("Error", e.toString());
                }
            }
            else{
                Log.e("Errreur", "Erreur");
            }
        }
        @Override
        protected void onCancelled() {

        }
    }
}
