package androby.babynator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import static androby.babynator.R.id.choice;

public class ChooseMapActivity extends AppCompatActivity/*, OnItemSelectedListener */{

    private EditText mRadius;
    private Spinner mChoice;
    private CheckBox mOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_map);

        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Hôpital");
        choices.add("Docteur");
        choices.add("Pharmacie");

        Spinner s = (Spinner) findViewById(choice);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, choices);
        s.setAdapter(adapter);

        mRadius = (EditText) findViewById(R.id.radius);
        mChoice = (Spinner) findViewById(R.id.choice);
        mOpen = (CheckBox) findViewById(R.id.open);

        Button mValidate = (Button) findViewById(R.id.btn_valid);
        mValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(attemptChoice()) {
                    Intent myIntent = new Intent(ChooseMapActivity.this, MapsActivity.class);
                    myIntent.putExtra("RADIUS", mRadius.getText().toString());
                    myIntent.putExtra("CHOICE", mChoice.getSelectedItem().toString());
                    myIntent.putExtra("OPEN", mOpen.isChecked());
                    startActivity(myIntent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Erreur : le périmètre doit être compris entre 10 et 50 000 mètres", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean attemptChoice() {


        if(mRadius.getText() == null || mRadius.getText().toString() == ""){
            return false;
        }
        Double radius = Double.parseDouble(mRadius.getText().toString());
        if (radius > 50000.0 || radius < 10.0) {
            return false;
        }
        return true;
    }

    //Méthode qui se déclenchera au clic sur un item
    public boolean onOptionsItemSelected(MenuItem item) {
        //On regarde quel item a été cliqué grâce à son id et on déclenche une action
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return false;
    }
}
