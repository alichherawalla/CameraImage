package com.betasolutions.cameraimage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

import javax.net.ssl.HttpsURLConnection;

public class RetreiveImage extends ActionBarActivity {
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotifyManager;
    int id = 1;
    int counter = 0;
    private NotificationReceiver nReceiver;

    class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getExtras().getString(NLService.NOT_EVENT_KEY);
            Log.i("NotificationReceiver", "NotificationReceiver onReceive : " + event);

        }
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retreive_image);
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Downloading image").setContentText("Download in progress").setSmallIcon(R.mipmap.ic_launcher);
        // Start a lengthy operation in a background thread

        if(isNetworkAvailable()){
            new DownloadImage().execute("http://www.techinsights.com/uploadedImages/Public_Website/Content_-_Primary/Teardowncom/Sample_Reports/sample-icon.png");
        }else{
            Toast.makeText(getBaseContext(), "Network is not Available", Toast.LENGTH_SHORT).show();
        }

        ContentResolver contentResolver = getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getPackageName();

        // check to see if the enabledNotificationListeners String contains our
        // package name
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
            // in this situation we know that the user has not granted the app
            // the Notification access permission
            // Check if notification is enabled for this application
            Log.i("ACC", "Dont Have Notification access");
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        } else {
            Log.i("ACC", "Have Notification access");
        }

        nReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NLService.NOT_TAG);
        registerReceiver(nReceiver, filter);


    }

    private boolean isNetworkAvailable(){
        boolean available = false;
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo !=null && networkInfo.isAvailable())
            available = true;
        return available;
    }

    private Bitmap downloadUrl(String stringURL) throws IOException{
        Bitmap bitmap=null;
        InputStream inputStream = null;
        try{
            URL url = new URL(stringURL);
            HttpURLConnection connectionURL = (HttpURLConnection) url.openConnection();
            connectionURL.connect();
            int fileSize=connectionURL.getContentLength();
            inputStream = connectionURL.getInputStream();
            byte[] receivedBytes=new byte[1024];
            byte[] finalBuffer=new byte[64000];
            int receivedBytesLength=0;
            int count=0;

            while((receivedBytesLength=inputStream.read(receivedBytes))>0){
                for(int index=count;index<count+receivedBytesLength;index++){
                    finalBuffer[index]=receivedBytes[index-count];

                }

                count+=receivedBytesLength;
                mBuilder.setContentText(String.valueOf(count/1024)+"Kbs/"+String.valueOf(fileSize/1024)+"kbs");
                mBuilder.setProgress(fileSize,count,false);
                mNotifyManager.notify(id, mBuilder.build());
                mBuilder.setAutoCancel(true);


            }

            Log.e("count",String.valueOf(count));
            bitmap = BitmapFactory.decodeByteArray(finalBuffer,0,count);
            ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
            if(bitmap==null)
                Log.e("failed","failed");

        } catch (Exception e){
            Log.e("failed","failed");

        }finally{
            inputStream.close();
        }
        return bitmap;
    }

    private class DownloadImage extends AsyncTask<String, Integer, Bitmap>{
        Bitmap bitmap = null;
        @Override
        protected Bitmap doInBackground(String... url) {
            try{
                bitmap = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
           ImageView ivDownload = (ImageView) findViewById(R.id.ivDownload);
            ivDownload.setImageBitmap(result);
            Toast.makeText(getBaseContext(), "Image downloaded successfully", Toast.LENGTH_SHORT).show();
            Log.e("done ","done");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}