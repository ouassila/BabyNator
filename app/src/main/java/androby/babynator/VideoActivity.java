package androby.babynator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.VideoView;

public class VideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        final VideoView videoView =
                (VideoView) findViewById(R.id.video_view);
        videoView.setVideoPath(
                "http://192.168.0.31:8090");
        videoView.start();
    }
}
