package androby.babynator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONArray;
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
import java.util.List;
import java.util.Locale;

import static androby.babynator.LoginActivity.IP_SERVER;
import static androby.babynator.R.id.container;
import static androby.babynator.R.id.nickname;

public class ListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

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
    private View mProgressView;
    private ViewPager mViewPager;
    private int id_user;
    private JSONArray listBabies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mProgressView = findViewById(R.id.list_babies_progress);
        showProgress(true);
        id_user = getIntent().getIntExtra("ID_USER", 0);
        ListBabiesTask mAuthTask = new ListBabiesTask(id_user);
        mAuthTask.execute((Void) null);
    }



    public boolean onOptionsItemSelected(MenuItem item) {
        //On regarde quel item a été cliqué grâce à son id et on déclenche une action
        Intent myIntent;
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_settings:
                myIntent = new Intent(ListActivity.this, ChooseMapActivity.class);
                myIntent.putExtra("ID_USER", this.id_user);
                startActivity(myIntent);
                return true;
            case R.id.action_calendar:
                myIntent = new Intent(ListActivity.this, CalendarActivity.class);
                myIntent.putExtra("ID_USER", this.id_user);
                startActivity(myIntent);
                return true;
            case R.id.action_video:
                myIntent = new Intent(ListActivity.this, VideoActivity.class);
                myIntent.putExtra("ID_USER", this.id_user);
                startActivity(myIntent);
                return true;
            case R.id.action_add_baby:
                myIntent = new Intent(ListActivity.this, AddBabyActivity.class);
                myIntent.putExtra("ID_USER", this.id_user);
                startActivity(myIntent);
                return true;
        }
        return false;
    }

    //Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du téléphone
    public boolean onCreateOptionsMenu(Menu menu) {

        //Création d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        //Instanciation du menu XML spécifier en un objet Menu
        inflater.inflate(R.menu.menu_main, menu);

        return true;
    }

    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }
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

      //  addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private EditText nickName;
        private RadioGroup radioSexGroup;
        private EditText age;
        private EditText birthday;
        private EditText length;
        private EditText weight;
        private Switch sexe;
        private DatePickerDialog datePickerDialog;
        private SimpleDateFormat dateFormatter;
        private JSONObject baby;
        private ModifyBabyTask mModifyBabyTask;
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_SECTION_NAME = "section_name";
        private static final String ARG_SECTION_SEXE = "section_sexe";
        private static final String ARG_SECTION_WEIGHT = "section_weight";
        private static final String ARG_SECTION_LENGTH = "section_length";
        private static final String ARG_SECTION_BIRTHDAY = "section_birthday";
        private static final String ARG_SECTION_ID = "section_birthday";
        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, JSONObject baby) {

            PlaceholderFragment fragment = new PlaceholderFragment();
            String name = "";
            String sexe  ="";
            String birthday = "";
            String length = "";
            String weight = "";
            try {
                Log.e("baby class", baby.toString());
                 name = baby.getString("name");
                sexe = baby.getString("gender");
                birthday = baby.getString("birthday");
                length = baby.getString("length");
                weight = baby.getString("weight");

            }
            catch (Exception e){
            }
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString(ARG_SECTION_NAME, name);
            args.putString(ARG_SECTION_BIRTHDAY, birthday);
            args.putString(ARG_SECTION_SEXE, sexe);
            args.putString(ARG_SECTION_LENGTH, length);
            args.putString(ARG_SECTION_WEIGHT, weight);

            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_test_list, container, false);
            nickName = (EditText) rootView.findViewById(nickname);
            birthday = (EditText) rootView.findViewById(R.id.birthday);
            length = (EditText) rootView.findViewById(R.id.length);
            weight = (EditText) rootView.findViewById(R.id.weight);
            nickName.setText(getArguments().getString(ARG_SECTION_NAME));
            length.setText(getArguments().getString(ARG_SECTION_LENGTH));
            weight.setText(getArguments().getString(ARG_SECTION_WEIGHT));
            birthday.setText(getArguments().getString(ARG_SECTION_BIRTHDAY));
            dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            sexe = (Switch)  rootView.findViewById(R.id.sexe);
            sexe.setText(getArguments().getString(ARG_SECTION_SEXE));
            if (getArguments().getString(ARG_SECTION_SEXE).equals("Garçon")){
               // sexe.
            }
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
          /*  FloatingActionButton mModifyBabyButton = (FloatingActionButton) rootView.findViewById(R.id.modify_baby_button);
            mModifyBabyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mModifyBabyTask = new ModifyBabyTask(getArguments().getString(ARG_SECTION_LENGTH), getArguments().getString(ARG_SECTION_WEIGHT), getArguments().getString(ARG_SECTION_SEXE), getArguments().getString(ARG_SECTION_BIRTHDAY), getArguments().getString(ARG_SECTION_NAME));
                    mModifyBabyTask.execute((Void) null);
                }
            });*/
           // setDateTimeField();
            return rootView;
        }



        public class ModifyBabyTask extends AsyncTask<Void, Void, Boolean> {

            private final String sexe;
            private final String birthday;
            private final String nickname;
            private final String length;
            private final String weight;
            private int id_baby;
            private int responseCode;

            ModifyBabyTask(String length, String weight, String sexe, String birthday, String nickname) {
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
                    URL url = new URL("http://"+LoginActivity.IP_SERVER+"/RestServer/babyNator/babies/modify");
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
                        //babyToAdd.put("id_user", getIntent().getIntExtra("ID_USER", 0));
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
                    URL url = new URL("http://"+LoginActivity.IP_SERVER+"/RestServer/babyNator/datas/add");
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
               // mAddBabyTask = null;
                // showProgress(false);

                if (success && responseCode == 202) {
                    Toast.makeText(getActivity().getApplicationContext(), "Votre bébé a bien été enregitré", Toast.LENGTH_LONG).show();
                    Intent myIntent = new Intent(getActivity(), ListActivity.class);
                    startActivity(myIntent);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Un problème a été rencontré lors de l'enregistrement de votre bébé", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected void onCancelled() {
            //    mAddBabyTask = null;
                // showProgress(false);
            }
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
            JSONObject baby = null;
            try {
                Toast.makeText(getApplicationContext(),
                        position + "position actuelle",
                        Toast.LENGTH_SHORT).show();
                baby= listBabies.getJSONObject(position);
            }
            catch (Exception e){

            }
            Log.e("baby ****",baby.toString() + position);
            return PlaceholderFragment.newInstance(position,baby);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return listBabies.length();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
    public class ListBabiesTask extends AsyncTask<Void, Void, Boolean> {

        private final int id_user;

        ListBabiesTask(int id_user) {
           this.id_user = id_user;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }
            // connexion au serveur pour test le user
            getBabies();

            return true;
        }
        private boolean getBabies(){
            try {
                URL url = new URL("http://"+ IP_SERVER+"/RestServer/babyNator/babies/list");
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
                //mConnection = true;

                wr.writeBytes (this.id_user+"");
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
                    listBabies = new JSONArray(response.toString());
                    Log.e("List baby ****",listBabies.toString());
                }
                catch(Exception e){
                    Log.e("baby list error",Log.getStackTraceString(e));
                }
            }
            catch (MalformedURLException ex) {
                Log.e("httptest",Log.getStackTraceString(ex));
                // return false;
            }
            catch (ConnectException ce){
                Log.e("httptest2",Log.getStackTraceString(ce));
                //  return false;
            }
            catch (IOException ex) {
                Log.e("httptest3",Log.getStackTraceString(ex));
                // return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            showProgress(false);

            if (success) {
                Toast.makeText(getApplicationContext(), "Bienvenu", Toast.LENGTH_LONG).show();
                mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

                // Set up the ViewPager with the sections adapter.
                mViewPager = (ViewPager) findViewById(container);
                mViewPager.setAdapter(mSectionsPagerAdapter);
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);


          //      startActivity(myIntent);
            } else {

                    Toast.makeText(getApplicationContext(), "Connexion impossible, veuillez vérifier votre connexion", Toast.LENGTH_LONG).show();

            }
        }

        @Override
        protected void onCancelled() {

        }


    }
}
