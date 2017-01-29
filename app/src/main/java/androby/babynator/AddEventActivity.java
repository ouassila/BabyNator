package androby.babynator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class AddEventActivity extends AppCompatActivity {

    private String date;
    private TextView lbl_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        date = getIntent().getStringExtra("DATE");
        lbl_date = (TextView) findViewById(R.id.lbl_date);
        lbl_date.setText(date);

    }
}
