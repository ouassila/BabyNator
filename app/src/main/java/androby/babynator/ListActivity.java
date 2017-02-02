package androby.babynator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ListActivity extends AppCompatActivity {

    private int id_user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        id_user = getIntent().getIntExtra("ID_USER", 0);
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
