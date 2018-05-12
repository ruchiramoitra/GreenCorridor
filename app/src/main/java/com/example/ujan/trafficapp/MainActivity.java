

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap finalMap;
    UiSettings mUiSettings;
    static LatLng source;
    LatLng cdest;
    static LatLng destination;
    static LatLng current;
    String TAG="Ruchira";
    String traffic_id="1";
    static double current_lat;
    static double current_long;
    double const_loc_lat=22.5745;
    double const_loc_lng = 88.4338;
    private PendingIntent mPendingIntent;


    private BroadcastReceiver mRegistrationBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //add toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        //get handle to map fragment
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");

                    if (success) {
                        //i
                        // nt age = jsonResponse.getInt("age");
                        String source1=jsonResponse.getString("source");
                        String destination1=jsonResponse.getString("destination");
                        String current1=jsonResponse.getString("current");
                        parseLatLng(source1,destination1,current1);
                        parseLatLngDist(current1);
                        if(distance(const_loc_lat,const_loc_lng,current_lat,current_long)<=1) {
                           // String msg = current1;
                            sendNotification();

                            onAmbulance();
                        }


                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("No ambulance nearing")
                                .setNegativeButton("Continue", null)
                                .create()
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        TrafficRequest trafficRequest = new TrafficRequest(traffic_id, responseListener);
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        queue.add(trafficRequest);





     /**   mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                    Toast.makeText(context, "registration", Toast.LENGTH_SHORT).show();



                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");
                    Log.e(TAG, message);
                   // Toast.makeText(context,"ruchu", Toast.LENGTH_LONG).show();
                   // Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();
                    //cdest=destination;
                        //parseToLatLong(message);
                        //if (distance(cdest.latitude, cdest.longitude, destination.latitude, destination.longitude) > 2){
                        //onAmbulance();
                        //}


                }
            }
        };**/


    }
    // Fetches reg id from shared preferences
    // and displays on the screen
void parseLatLng(String source1,String destination1, String current1){
    String source2[]= source1.split(",");
    String destination2[]=destination1.split(",");
    String current2[]=current1.split(",");
    source=new LatLng(Double.parseDouble(source2[0]), Double.parseDouble(source2[1]));
    destination=new LatLng(Double.parseDouble(destination2[0]), Double.parseDouble(destination2[1]));
    current=new LatLng(Double.parseDouble(current2[0]), Double.parseDouble(current2[1]));

}
void parseLatLngDist(String current1){
    String current2[]=current1.split(",");
    current_lat= Double.parseDouble(current2[0]);
    current_long= Double.parseDouble(current2[1]);

}
    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometer output

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist; // output distance, in MILES
    }


 /**   void parseToLatLong(String message){

        String a[]=message.split(",");
        String msource[]=a[0].split("a");
        String dest[]=a[1].split("a");
        String mcurrent[]=a[2].split("a");
        source=new LatLng(Double.parseDouble(msource[0]), Double.parseDouble(msource[1]));
        destination=new LatLng(Double.parseDouble(dest[0]), Double.parseDouble(dest[1]));
        current=new LatLng(Double.parseDouble(mcurrent[0]), Double.parseDouble(mcurrent[1]));


    }**/


    @Override
    public void onMapReady(GoogleMap googleMap) {

        finalMap=googleMap;

        //customize map
        mUiSettings = finalMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setRotateGesturesEnabled(false);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);
        finalMap.setPadding(0,840,0,0);

        LatLng intersection= new LatLng(const_loc_lat,const_loc_lng);
        finalMap.moveCamera(CameraUpdateFactory.newLatLngZoom(intersection, (float) 14.8));

       /* LatLngBounds LORDS = new LatLngBounds(
                new LatLng(22.502044, 88.354389),new LatLng(22.502161, 88.360986));
        finalMap.setLatLngBoundsForCameraTarget(LORDS); */ //bounds for map

        finalMap.setMinZoomPreference(14.8f);
        finalMap.setMaxZoomPreference(17.0f);

        CircleOptions circleOptions = new CircleOptions()
                .center(intersection)
                .radius(500)
                .strokeColor(Color.parseColor("#c4daff"));
        Circle circle = finalMap.addCircle(circleOptions);


    }

    @Override
    protected void onResume() {

        super.onResume();

        // register GCM registration complete receiver
       /** LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());**/


    }

    @Override
    protected void onPause() {

     /**   LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);**/
        super.onPause();

    }

    void onAmbulance(){
//        cdest=destination;
        finalMap.clear();
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(const_loc_lat,const_loc_lng ))
                .radius(500)
                .strokeColor(Color.parseColor("#c4daff"));
        Circle circle = finalMap.addCircle(circleOptions);

        finalMap.addMarker(new MarkerOptions()
                .position(current)
        );


        String url = getDirectionsUrl(source,destination); //latlong, latlong

        DownloadTask downloadTask = new DownloadTask();

        downloadTask.execute(url);

    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            //Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }


    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>>{
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }


        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            finalMap.addPolyline(lineOptions);
        }
    }
  /** public PendingIntent goToNotificationIntent(){
       if (mPendingIntent != null) {
           return mPendingIntent;
       }
       Intent intent = new Intent(this, SendNotificationService.class);
       return PendingIntent.getService(this, 0, intent, PendingIntent.
               FLAG_UPDATE_CURRENT);


   }**/
    private void sendNotification(){



        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(R.drawable.mr_ic_play_dark)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.mr_ic_play_dark))
                .setColor(Color.RED)
                .setContentTitle("Ambulance Details")
                .setContentText("Ambulance is nearing")
                .setContentIntent(notificationPendingIntent);

        builder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());


    }

}
