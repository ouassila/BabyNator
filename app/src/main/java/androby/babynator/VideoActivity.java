package androby.babynator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class VideoActivity extends AppCompatActivity {

    private EditText textUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
