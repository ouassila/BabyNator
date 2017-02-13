package androby.babynator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class VideoActivity extends AppCompatActivity {

    private EditText textUrl;
    private int id_user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id_user = getIntent().getIntExtra("ID_USER", 0);
        setContentView(R.layout.activity_video);
        textUrl =
                (EditText) findViewById(R.id.textUrl);

        Button mCameraButtonIp = (Button) findViewById(R.id.ip_camera_button);
        mCameraButtonIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = textUrl.getText().toString();
                try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://\"+url+\":9000/"));
                        intent.setDataAndType(Uri.parse("http://" + url + ":9000/"), "video/*");
                        startActivity(intent);

                } catch (Exception e) {
                    Log.e("boolean", e.getMessage());
                    Toast.makeText(getApplicationContext(), "Connexion impossible, veuillez vérifier l'adresse Ip de la caméra", Toast.LENGTH_LONG).show();
                }
            }
        });
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.floatingActionButton2);
        myFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this);
                builder
                        .setMessage("Voici l'aide pour récup l'IP de la caméra ...")
                        .show();
            }
        });
    }

    private boolean executeCommand(String url){
        System.out.println("executeCommand");
        Runtime runtime = Runtime.getRuntime();
        try
        {
            Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 " + url);
            int mExitValue = mIpAddrProcess.waitFor();
            System.out.println(" mExitValue "+mExitValue);
            if(mExitValue==0){
                return true;
            }else{
                return false;
            }
        }
        catch (InterruptedException ignore)
        {
            ignore.printStackTrace();
            System.out.println(" Exception:"+ignore);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println(" Exception:"+e);
        }
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        //On regarde quel item a été cliqué grâce à son id et on déclenche une action
        Intent myIntent;
        switch (item.getItemId()) {
            case R.id.action_settings:
                myIntent = new Intent(VideoActivity.this, ChooseMapActivity.class);
                myIntent.putExtra("ID_USER", this.id_user);
                startActivity(myIntent);
                return true;
            case R.id.action_calendar:
                myIntent = new Intent(VideoActivity.this, CalendarActivity.class);
                myIntent.putExtra("ID_USER", this.id_user);
                startActivity(myIntent);
                return true;
            case R.id.action_add_baby:
                myIntent = new Intent(VideoActivity.this, AddBabyActivity.class);
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
}
