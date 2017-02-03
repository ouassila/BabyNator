package androby.babynator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoActivity extends AppCompatActivity {

    private VideoView videoView;
    private EditText textUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        videoView =
                (VideoView) findViewById(R.id.video_view);
        textUrl =
                (EditText) findViewById(R.id.textUrl);

        Button mCameraButtonIp = (Button) findViewById(R.id.ip_camera_button);
        mCameraButtonIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String test = textUrl.getText().toString();
                Toast.makeText(getApplicationContext(), test, Toast.LENGTH_LONG).show();
                videoView.setVideoPath(
                        "http://"+test+":8090");

                videoView.start();
               /* if (videoView.isShown()){
                    Log.e("httptest2","Video lancé");
                    Toast.makeText(getApplicationContext(), "Video lancé", Toast.LENGTH_LONG).show();
                }*/
                if (videoView.isPlaying()){
                    Log.e("httptest2","Video lancé playing");
                    Toast.makeText(getApplicationContext(), "Video lancé", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
