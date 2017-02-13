package androby.babynator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jpardogo.android.googleprogressbar.library.FoldingCirclesDrawable;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private ProgressBar mProgressBar;
    private ViewPager mViewPager;
    private int id_user;
    private JSONArray listBabies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mProgressView = findViewById(R.id.list_babies_progress);
        mProgressBar = (ProgressBar) findViewById(R.id.list_babies_progress);

        mProgressBar.setIndeterminateDrawable(new FoldingCirclesDrawable.Builder(this)
                .build());

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
        private TextView nickName;
        private RadioGroup radioSexGroup;
        private EditText age;
        private TextView birthday;
        private TextView length;
        private TextView weight;
        private EditText pLength;
        private EditText pWeight;
        private Switch sexe;
        private DatePickerDialog datePickerDialog;
        private SimpleDateFormat dateFormatter;
        private SimpleDateFormat dateFormatter_out;
        private JSONObject baby;
        private AddDataTask mModifyBabyTask;
        private ImageView imageSexe;
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_SECTION_NAME = "section_name";
        private static final String ARG_SECTION_ID_BABY = "section_id_baby";
        private static final String ARG_SECTION_SEXE = "section_sexe";
        private static final String ARG_SECTION_WEIGHT = "section_weight";
        private static final String ARG_SECTION_LENGTH = "section_length";
        private static final String ARG_SECTION_BIRTHDAY = "section_birthday";
        private static final String ARG_SECTION_ID = "section_birthday";
        private static final String ARG_SECTION_PICTURE = "section_picture";
        private static int ID_USER;
        private static int ID_BABY;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, JSONObject baby, int id) {

            PlaceholderFragment fragment = new PlaceholderFragment();
            String name = "";
            String sexe  ="";
            String birthday = "";
            String length = "";
            String weight = "";
            String picture = "";

            int id_baby = 0;
            ID_USER = id;

            if(baby != null){
                try {
                    Log.e("baby class", baby.toString());
                    name = baby.getString("name");
                    sexe = baby.getString("gender");
                    birthday = baby.getString("birthday");
                    length = baby.getString("length");
                    weight = baby.getString("weight");
                    picture = baby.getString("picture");
                    id_baby = baby.getInt("id");
                    ID_BABY = baby.getInt("id");
                }
                catch (Exception e){
                }
            }
            else{
                ID_BABY = 0;
            }
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putInt(ARG_SECTION_ID_BABY, id_baby);
            args.putString(ARG_SECTION_NAME, name);
            args.putString(ARG_SECTION_BIRTHDAY, birthday);
            args.putString(ARG_SECTION_SEXE, sexe);
            args.putString(ARG_SECTION_LENGTH, length);
            args.putString(ARG_SECTION_WEIGHT, weight);
            args.putString(ARG_SECTION_PICTURE, picture);

            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView;

            String picture = "";

            if(ID_BABY > 0){
                rootView = inflater.inflate(R.layout.fragment_fiche_baby, container, false);
                nickName = (TextView) rootView.findViewById(nickname);
                birthday = (TextView) rootView.findViewById(R.id.birthday);
                length = (TextView) rootView.findViewById(R.id.length);
                weight = (TextView) rootView.findViewById(R.id.weight);
                imageSexe = (ImageView) rootView.findViewById(R.id.sexe);
                nickName.setText(getArguments().getString(ARG_SECTION_NAME));
                length.setText(getArguments().getString(ARG_SECTION_LENGTH));
                weight.setText(getArguments().getString(ARG_SECTION_WEIGHT));

                dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                dateFormatter_out = new SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE);
                String birth = getArguments().getString(ARG_SECTION_BIRTHDAY);
                try {
                    Date date = dateFormatter.parse(birth);
                    birthday.setText(dateFormatter_out.format(date));
                } catch (ParseException e) {
                   Log.e("Erreur", e.toString());
                }
                picture = getArguments().getString(ARG_SECTION_PICTURE);
                Bitmap bm = BitmapFactory.decodeFile(picture);

                if(!picture.equals("vide") && bm != null) {
                    imageSexe.setImageBitmap(bm);
                }
                else if ((bm == null) || (picture.equals("vide") &&
                        getArguments().getString(ARG_SECTION_SEXE).equals("Garçon"))){
                    imageSexe.setImageResource(R.mipmap.ic_garcon);
                }

                Button showGraphButton = (Button) rootView.findViewById(R.id.view_graph);
                showGraphButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent myIntent = new Intent(getContext(), GraphActivity.class);
                        myIntent.putExtra("baby", getArguments().getInt(ARG_SECTION_ID_BABY));
                        startActivity(myIntent);
                    }
                });
                Button addDataButton = (Button) rootView.findViewById(R.id.add_data);
                addDataButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {

                        // custom dialog
                        final Dialog dialog = new Dialog(getContext());
                        dialog.setContentView(R.layout.pop_up_add_data);

                        // set the custom dialog components - text, image and button
                        pWeight = (EditText) dialog.findViewById(R.id.weight);
                        pLength = (EditText) dialog.findViewById(R.id.length);

                        FloatingActionButton dialogButton = (FloatingActionButton) dialog.findViewById(R.id.addData);
                        // if button is clicked, close the custom dialog
                        dialogButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                dialog.dismiss();
                                builder
                                        .setMessage("Voulez vous vraiment ajouter ces données du jour ?")
                                        .setPositiveButton("Valider",  new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface current_dialog, int id) {
                                                AddDataTask addDataTask = new AddDataTask( pLength.getText().toString(), pWeight.getText().toString(), getArguments().getInt(ARG_SECTION_ID_BABY));
                                                addDataTask.execute((Void) null);
                                                current_dialog.dismiss();
                                                getActivity().finish();
                                                startActivity(getActivity().getIntent());
                                            }
                                        })
                                        .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface current_dialog, int id) {
                                                current_dialog.cancel();
                                            }
                                        })
                                        .show();

                            }
                        });
                        dialog.show();
                    }
                });

                FloatingActionButton btn_remove = (FloatingActionButton) rootView.findViewById(R.id.btn_remove);
                btn_remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder
                                .setMessage("Voulez vous vraiment supprimer ce bébé ?")
                                .setPositiveButton("Supprimer",  new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface current_dialog, int id) {
                                        RemoveDataTask removeDataTask = new RemoveDataTask(getArguments().getInt(ARG_SECTION_ID_BABY));
                                        removeDataTask.execute((Void) null);
                                        current_dialog.dismiss();
                                        getActivity().finish();
                                        startActivity(getActivity().getIntent());
                                    }
                                })
                                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface current_dialog, int id) {
                                        current_dialog.cancel();
                                    }
                                })
                                .show();
                    }
                });
            }
            else{
                rootView = inflater.inflate(R.layout.fragment_no_baby, container, false);
                Button addBabyButton = (Button) rootView.findViewById(R.id.add_baby);
                addBabyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                    Intent myIntent = new Intent( getActivity(), AddBabyActivity.class);
                    myIntent.putExtra("ID_USER", ID_USER);
                    startActivity(myIntent);
                    }
                });
            }
            return rootView;
        }

        public class RemoveBabyTask extends AsyncTask<Void, Void, Boolean> {

            private int mId;

            RemoveBabyTask(int id) {
                mId = id;
            }

            @Override
            protected Boolean doInBackground(Void... params) {

                try {
                    URL url = new URL("http://"+LoginActivity.IP_SERVER+"/RestServer/babyNator/babies/remove");
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

                    wr.writeBytes (this.mId+"");
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
                        Log.d("RESPONSE", response.toString());
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
                if (success) {
                    if (success) {
                        getActivity().finish();

                        Intent myIntent = new Intent(getActivity(), ListActivity.class);
                        myIntent.putExtra("ID_USER", ID_USER);
                        startActivity(myIntent);
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "Un problème a été rencontré lors de la suppression de votre bébé", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

        public class RemoveDataTask extends AsyncTask<Void, Void, Boolean> {

            private int mId;

            RemoveDataTask(int idBaby) {
                mId = idBaby;
            }

            @Override
            protected Boolean doInBackground(Void... params) {

                try {
                    URL url = new URL("http://"+LoginActivity.IP_SERVER+"/RestServer/babyNator/datas/remove");
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

                    wr.writeBytes (this.mId+"");
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
                        Log.d("RESPONSE", response.toString());
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
                if (success) {
                    RemoveBabyTask removeBabyTask = new RemoveBabyTask(ID_BABY);
                    removeBabyTask.execute((Void) null);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Un problème a été rencontré lors de la suppression de votre bébé", Toast.LENGTH_LONG).show();
                }
            }
        }

        public class AddDataTask extends AsyncTask<Void, Void, Boolean> {

            private final String length;
            private final String weight;
            private int id_baby;
            private int responseCode;

            AddDataTask(String length, String weight, int id_baby) {

                this.length = length;
                this.weight = weight;
                this.id_baby=id_baby;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                    return addData();
            }

            private boolean addData(){
                try {
                    URL url = new URL("http://"+LoginActivity.IP_SERVER+"/RestServer/babyNator/datas/add");
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
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));
                    JSONObject babyToAdd = new JSONObject();
                    JSONObject dataToAdd = new JSONObject();
                    //  mConnection = true;
                    try {
                        Date d = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);

                        Log.d("DATE", sdf.format(d).toString());
                        dataToAdd.put("length", Integer.parseInt(length));
                        dataToAdd.put("weight", Integer.parseInt(weight));
                        dataToAdd.put("id_baby", id_baby);
                        dataToAdd.put("current_date", sdf.format(d).toString());
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
                    try {
                        Log.d("RESPONSE", response.toString());
                    }
                    catch(Exception e){
                        return false;
                    }
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

                if (success && responseCode == 202) {
                    Toast.makeText(getActivity().getApplicationContext(), "Les données ont bien été enregitrées", Toast.LENGTH_LONG).show();
                    Intent myIntent = new Intent(getActivity(), ListActivity.class);
                    myIntent.putExtra("ID_USER", ID_USER);
                    startActivity(myIntent);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Un problème a été rencontré lors de l'enregistrement de votre bébé", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected void onCancelled() {
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
            JSONObject baby = null;
            try {
                baby = listBabies.getJSONObject(position);
            }
            catch (Exception e){
                Log.e("Erreur", e.toString());
            }
            return PlaceholderFragment.newInstance(position,baby, id_user);
        }

        @Override
        public int getCount() {
            if(listBabies.length() > 0)
                return listBabies.length();
            else
                return 1;
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
                    Log.e("List baby",listBabies.toString());
                }
                catch(Exception e){
                    Log.e("baby list error",Log.getStackTraceString(e));
                }
            }
            catch (MalformedURLException ex) {
                Log.e("httptest",Log.getStackTraceString(ex));
            }
            catch (ConnectException ce){
                Log.e("httptest2",Log.getStackTraceString(ce));
            }
            catch (IOException ex) {
                Log.e("httptest3",Log.getStackTraceString(ex));
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            showProgress(false);

            if (success) {
                mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

                mViewPager = (ViewPager) findViewById(container);
                mViewPager.setAdapter(mSectionsPagerAdapter);
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
            } else {
                Toast.makeText(getApplicationContext(), "Connexion impossible, veuillez vérifier votre connexion", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            // do something on back.
            Log.e("test", "retouyr");
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }
}
