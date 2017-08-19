package edu.buffalo.cse.cse486586.simpledht;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class SimpleDhtActivity extends Activity {
    ContentValues cv;
    ContentResolver cr;
    Uri.Builder ub;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {




        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_dht);



        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        // Log.i("port number",myPort);

       /* ub=new Uri.Builder();
        ub.authority("edu.buffalo.cse.cse486586.simpledht.provider");
        ub.scheme("content");
        uri=ub.build();
        cr=getContentResolver();

        cv = new ContentValues();
        cv.put("key", "port-no");
        cv.put("value", myPort);
        cr.insert(uri, cv);
*/
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        findViewById(R.id.button3).setOnClickListener(
                new OnTestClickListener(tv, getContentResolver()));


     /*   String requestType = "join";
        String joinPort = "11108";
        String msg = myPort;
        String msgToSend = "hello";


        OutputStream osc;
        PrintWriter pwc;
        InputStream isc;
        InputStreamReader isrc;
        BufferedReader brc;
        Socket socket;
        String ackc;

        Log.i("-----------------------", "-----------------------------");
        Log.i("---", "--------------------------CLIENT--------------------------");
        Log.i("-----------------------", "-----------------------------");
        try {

            socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.parseInt(joinPort));
            ackc = "PA1_OK";
            osc = socket.getOutputStream();
            pwc = new PrintWriter(osc);
            pwc.write(msgToSend);
            pwc.flush();

        } catch (Exception e)
        {
            e.printStackTrace();
    }
*/














    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.activity_simple_dht_main, menu);
        return true;
    }









}