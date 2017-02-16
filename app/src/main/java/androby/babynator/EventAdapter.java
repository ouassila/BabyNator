package androby.babynator;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Ouassila on 06/02/2017.
 */


public class EventAdapter extends ArrayAdapter<Event> {

    private Activity activity;
    private Event event;
    private RemoveCalendarTask task = null;
    private Context context;
    private int id_user;

    public EventAdapter(Context context, List<Event> events, Activity current_activity, int id_user) {
        super(context, 0, events);
        this.activity = current_activity;
        this.context = context;
        this.id_user = id_user;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.simplerow, parent, false);
        }

        EventViewHolder viewHolder = (EventViewHolder) convertView.getTag();
        if (viewHolder == null) {
            viewHolder = new EventViewHolder();
            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            viewHolder.text = (TextView) convertView.findViewById(R.id.text);
            viewHolder.btn_delete = (ImageButton) convertView.findViewById(R.id.btn_delete);
            convertView.setTag(viewHolder);
        }

        event = getItem(position);
        viewHolder.date.setText(event.getDate());
        viewHolder.text.setText(event.getText());
        viewHolder.btn_delete.setTag(position);
        viewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                int positiotn = (Integer)v.getTag();
                event = getItem(positiotn);
                AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                alert.setTitle("Demande de suppression");
                alert.setMessage("Voulez-vous supprimer cet évènement ?");
                alert.setPositiveButton("Valider", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RemoveCalendarTask task = new RemoveCalendarTask(event.getId(), false);
                        task.execute((Void) null);

                        Log.d("event",event.getId()+event.getDate()+event.getText());
                        Intent intent= new Intent(context, CalendarActivity.class);
                        intent.putExtra("ID_USER", id_user);
                        activity.finish();
                        context.startActivity(intent);
                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        });
        return convertView;
    }

    private class EventViewHolder{
        public TextView date;
        public TextView text;
        public ImageButton btn_delete;
    }

    public class RemoveCalendarTask extends AsyncTask<Void, Void, Boolean> {

        private int mId;
        private boolean mConnection;

        RemoveCalendarTask(int id, boolean connection) {
            mConnection = connection;
            mId = id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                URL url = new URL("http://"+LoginActivity.IP_SERVER+"/RestServer/babyNator/events/remove");
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
                mConnection = true;

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
    }
}
