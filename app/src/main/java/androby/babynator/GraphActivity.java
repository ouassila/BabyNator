package androby.babynator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class GraphActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private static JSONArray listDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        GetGraphTask graphTask = new GetGraphTask(getIntent().getIntExtra("baby", 0));
        graphTask.execute((Void) null);
        super.onCreate(savedInstanceState);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
            String option = "weight";
            String option_graph = "Poids (Kg)";
            if (getArguments().getInt(ARG_SECTION_NUMBER)==1){
                option="length";
                option_graph= "Taille (cm)";
            }
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM");
                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                });
                DataPoint[] ds = new DataPoint[listDatas.length()];
                Log.e("test datas ",listDatas.toString());
                for (int i = 0; i < listDatas.length(); i++) {
                    JSONObject data = listDatas.getJSONObject(i);
                    ds[i]= new DataPoint(formatter.parse(data.getString("current_date")), data.getInt(option));
                    series.appendData(ds[i], true, 10);
                    Log.e("test datas ", "datas " + ds[i].toString());
                }
                GraphView graph = (GraphView) rootView.findViewById(R.id.graph);
                graph.addSeries(series);
                DateFormat dateFormat = new SimpleDateFormat("dd/MM");

// set date label formatter
           graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity(),dateFormat));
                graph.getViewport().setMinY(0);
                graph.getViewport().setMaxY(listDatas.getJSONObject(listDatas.length()-1).getInt(option));
                graph.getViewport().setYAxisBoundsManual(true);
          graph.getViewport().setMinX(formatter.parse((listDatas.getJSONObject(0)).getString("current_date")).getTime());
          graph.getViewport().setMaxX(formatter.parse(listDatas.getJSONObject(listDatas.length()-1).getString("current_date")).getTime());
            graph.getViewport().setXAxisBoundsManual(true);

// as we use dates as labels, the human rounding to nice readable numbers
// is not necessary
            graph.getGridLabelRenderer().setHumanRounding(false);
                TextView textView = (TextView) rootView.findViewById(R.id.option);
                textView.setText(option_graph);
            }
            catch(Exception e){
                Log.e("eroor",e.getMessage());
            }
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
            }
            return null;
        }
    }
    public class GetGraphTask extends AsyncTask<Void, Void, Boolean> {

        private final int id_baby;

        GetGraphTask(int id_baby) {
            this.id_baby=id_baby;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return getGraph();
        }

        private boolean getGraph(){
            try {
                URL url = new URL("http://"+LoginActivity.IP_SERVER+"/RestServer/babyNator/datas/list");
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


                wr.writeBytes (this.id_baby+"");
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
                    listDatas = new JSONArray(response.toString());
                    Log.e("List baby ****",listDatas.toString());
                }
                catch(Exception e){
                    Log.e("data list error",Log.getStackTraceString(e));
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
            return true;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                setContentView(R.layout.activity_graph);

                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                // Create the adapter that will return a fragment for each of the three
                // primary sections of the activity.
                mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

                // Set up the ViewPager with the sections adapter.
                mViewPager = (ViewPager) findViewById(R.id.container);
                mViewPager.setAdapter(mSectionsPagerAdapter);
            }
        }

        @Override
        protected void onCancelled() {
            //    mAddBabyTask = null;
            // showProgress(false);
        }
    }

}
