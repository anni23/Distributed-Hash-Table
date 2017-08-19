package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

//import static android.content.ContentValues.TAG;

public class SimpleDhtProvider extends ContentProvider
{
    LinkedList<String> ports;
    LinkedList<String> nodeids;
    Hashtable<String,String> dht;
    Hashtable<String,String> nodes;
    String myNodeKey;
    String successorNodeKey;
    String predecessorNodeKey;
    String firstPort;
    static final int SERVER_PORT = 10000;
    static String REMOTE_PORT_Succ;
    static String REMOTE_PORT_Pre;
    DBProvider db;
    String myPort;
    String starPort;
    int myCursorCount=0;
    int tempCursorCount=0;
    public boolean onCreate()
    {
        successorNodeKey=null;
        predecessorNodeKey=null;

        //getting my port number
        TelephonyManager tel=(TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        myPort = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        Log.i("port in dp",myPort);
        starPort=myPort;
        try
        {
            myNodeKey=genHash(myPort);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //creating database instance
        db = new DBProvider(this.getContext());

        //starting the server
        try
        {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);

            Thread.sleep(100);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //node join request
        if(myPort.equals("5554"))
        {
            //no need to send join request
        }
        else
        {
            String requestType="join";
            String joinPort="11108";
            String msg=myPort;

            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, joinPort,requestType);
            return true;
        }

        dht=new Hashtable<String,String>();
        ports=new LinkedList<String>();
        ports.add(myPort);
        nodeids=new LinkedList<String>();
        nodes=new Hashtable<String,String>();
        try
        {
            nodeids.add(genHash(myPort));
            nodes.put(genHash(myPort),myPort);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }


    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        // TODO Auto-generated method stub

        db.delete(selection);


        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        Log.i("insert","--------------------------------------------------------");

        try {
            String msgKey=values.get("key").toString();
            String msgText=values.get("value").toString();
                String hashedMsgKey = genHash(msgKey);


            if((successorNodeKey==null)&&(predecessorNodeKey==null))
            {
                //Log.i("reached here", "*******************************************");
                //insert into database or hashtable
                //dht.put(hashedMsgKey,msgText);
                ContentValues cv = new ContentValues();
                cv.put("key", msgKey);
                cv.put("value", msgText);

                db.insertIntoDb(cv,"true");
                myCursorCount++;
            }
            else
            {
                Log.i("888888888888888888","888888888888888888888888888888888");
                Log.i("my port=====",myPort);
                Log.i("my key=====",myNodeKey);
                Log.i("my predecessor=====",predecessorNodeKey);
                Log.i("my successor=====",successorNodeKey);
                Log.i("successor port",REMOTE_PORT_Succ);
                Log.i("predecessor port",REMOTE_PORT_Pre);
                Log.i("key=====",msgKey);
                Log.i("value=====",msgText);
                Log.i("msg key=====",hashedMsgKey);
                Log.i("first port=====",firstPort);


                Log.i("b1", (hashedMsgKey.compareToIgnoreCase(myNodeKey)) + "");
                Log.i("b2", (hashedMsgKey.compareToIgnoreCase(predecessorNodeKey)) + "");

                Log.i("b1", (hashedMsgKey.compareToIgnoreCase(successorNodeKey)) + "");
                Log.i("b2", (hashedMsgKey.compareToIgnoreCase(myNodeKey)) + "");




                if (((hashedMsgKey.compareTo(myNodeKey) <= 0) && (hashedMsgKey.compareTo(predecessorNodeKey) > 0))||((hashedMsgKey.compareTo(myNodeKey)>0)&&(hashedMsgKey.compareTo(predecessorNodeKey)>0)&&(myPort.equals(firstPort)))||((hashedMsgKey.compareTo(myNodeKey)<0)&&(hashedMsgKey.compareTo(predecessorNodeKey)<0)&&(myPort.equals(firstPort))))
                {

                    Log.i("reached here", "***************inserted locally******************");
                    //insert into database or hashtable
                    //dht.put(hashedMsgKey,msgText);
                    ContentValues cv = new ContentValues();
                    cv.put("key", msgKey);
                    cv.put("value", msgText);
                    db.insertIntoDb(cv,"true");
                    myCursorCount++;
                }

                else
                {
                //send to successor

                String myPort=(Integer.parseInt(REMOTE_PORT_Succ)*2)+"";
                String requestType="insert";
                String msg=msgKey+",,"+msgText;
                Log.i("insert-msg",msg);
                Log.i("insert-succport",myPort);
                Log.i("insert-reqtype",requestType);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort,requestType);

                }


            }
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }








        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // TODO Auto-generated method stub

       /* String msg="";
        String myPort=REMOTE_PORT_Pre;
        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
*/
        Log.i("query","AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        Cursor c=null;
        String hashedMsgKey="";
        //Log.i("selection===",selection);
        try
        {
            hashedMsgKey=genHash(selection);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }



        if((successorNodeKey==null)&&(predecessorNodeKey==null))
        {

            if (selection.equals("*") || selection.equals("@"))
            {
                try {
                    c = db.queryDb("*","true");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else
            {
                try {
                    c = db.queryDb((selection),"true");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            if (selection.equals("@"))
            {
                try
                {
                    c = db.queryDb("*","true");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if(selection.equals("*"))
            {

                Log.i("+++++star port++++",starPort);
                if(REMOTE_PORT_Succ.equals(starPort))
                {
                    Log.i("star query","reached in if");
                    try {
                        c = db.queryDb("*","true");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    //get your all rows and return a cursor
                }
                else {
                    Log.i("star query","reached in else");
                    try {
                        OutputStream osc;
                        PrintWriter pwc;
                        InputStream isc;
                        InputStreamReader isrc;
                        BufferedReader brc;


                        String remotePort = REMOTE_PORT_Succ;
                        String req = "star";
                        String msg = req + ",," + starPort + "\n";

                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                (Integer.parseInt(remotePort) * 2));

                        //String ackc = "PA1_OK";

                        osc = socket.getOutputStream();
                        pwc = new PrintWriter(osc);
                        pwc.write(msg);
                        pwc.flush();

                        Log.i("CLIENT", "client side reached1");
                        //os.close();

                        isc = socket.getInputStream();
                        isrc = new InputStreamReader(isc);
                        Log.i("CLIENT", "client side reached2");
                        brc = new BufferedReader(isrc);
                        Log.i("CLIENT", "client side reached4");
                        //Log.i(TAG,socket.isConnected()+"");
                        //Log.i(TAG,socket.isInputShutdown()+"");
                        //Log.i(TAG,socket.isOutputShutdown()+"");
                        String x = brc.readLine();


                        if (!(x.equals(""))) {
                            Log.i("CLIENT", "client side reached5");
                            //if (x.equals(ackc)) {
                            Log.i("CLIENT", "client side reached6");
                            pwc.close();
                            brc.close();
                            socket.close();
                            //}

                            // read x and a make cursor
                            Cursor c1;
                            //try {
                                c1 = db.queryDb("*","true");
                            //} catch (Exception e) {
                             //   e.printStackTrace();
                            //}


                            //check if cursor is empty
                            String m;

                            if(c1.getCount()==0)
                            {
                                m="blank";
                            }
                            else
                            {
                                String key=c1.getString(0);
                                String value=c1.getString(1);
                                m=key+",,"+value;
                                c1.moveToNext();
                                for(int i=0;i<c1.getCount()-1;i++)
                                {
                                    value=c1.getString(1);
                                    key=c1.getString(0);
                                    m=m+","+key+",,"+value;
                                    c1.moveToNext();
                                }
                            }

                            String res="nothing";
                            db.delete("temp");


                            Log.i("x===",x);
                            Log.i("m===",m);



                            if((x.equals("blank"))&&(m.equals("blank")))
                            {
                                c=c1;
                            }
                            else if(x.equals("blank"))
                            {
                                res=m;
                                StringTokenizer st=new StringTokenizer(res,",");
                                while(st.hasMoreTokens())
                                {
                                    String k=st.nextToken();
                                    String v=st.nextToken();
                                    ContentValues cv = new ContentValues();
                                    cv.put("key", k);
                                    cv.put("value", v);
                                    db.insertIntoDb(cv, "temp");
                                    c = db.queryDb("*", "temp");
                                }


                            }
                            else if(m.equals("blank"))
                            {
                                res=x;
                                StringTokenizer st=new StringTokenizer(res,",");
                                while(st.hasMoreTokens())
                                {
                                    String k=st.nextToken();
                                    String v=st.nextToken();
                                    ContentValues cv = new ContentValues();
                                    cv.put("key", k);
                                    cv.put("value", v);
                                    db.insertIntoDb(cv, "temp");
                                    c = db.queryDb("*", "temp");
                                }
                            }
                            else
                            {
                                res=x+","+m;
                                StringTokenizer st=new StringTokenizer(res,",");
                                while(st.hasMoreTokens())
                                {
                                    String k=st.nextToken();
                                    String v=st.nextToken();
                                    ContentValues cv = new ContentValues();
                                    cv.put("key", k);
                                    cv.put("value", v);
                                    db.insertIntoDb(cv, "temp");
                                    c = db.queryDb("*", "temp");
                                }
                            }

                            Log.i("Star result",res);

                            //create cursor from res




                            Log.i("done","dudeeee");
                            /*int a = x.indexOf(",,");
                            String k = x.substring(0, a);
                            String v = x.substring(a + 2, x.length());

                            Log.i("result-----", x);
                            */





                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }

            }
            else
            {
                if (((hashedMsgKey.compareTo(myNodeKey) <= 0) && (hashedMsgKey.compareTo(predecessorNodeKey) > 0))||((hashedMsgKey.compareTo(myNodeKey)>0)&&(hashedMsgKey.compareTo(predecessorNodeKey)>0)&&(myPort.equals(firstPort)))||((hashedMsgKey.compareTo(myNodeKey)<0)&&(hashedMsgKey.compareTo(predecessorNodeKey)<0)&&(myPort.equals(firstPort))))
                {
                    try
                    {
                        c = db.queryDb((selection),"true");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        OutputStream osc;
                        PrintWriter pwc;
                        InputStream isc;
                        InputStreamReader isrc;
                        BufferedReader brc;


                        String remotePort = REMOTE_PORT_Succ;
                        String req = "query";
                        String msg = req+",,"+selection+"\n";

                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                (Integer.parseInt(remotePort) * 2));

                        //String ackc = "PA1_OK";

                        osc = socket.getOutputStream();
                        pwc = new PrintWriter(osc);
                        pwc.write(msg);
                        pwc.flush();

                        Log.i("CLIENT", "client side reached1");
                        //os.close();

                        isc = socket.getInputStream();
                        isrc = new InputStreamReader(isc);
                        Log.i("CLIENT", "client side reached2");
                        brc = new BufferedReader(isrc);
                        Log.i("CLIENT", "client side reached4");
                        //Log.i(TAG,socket.isConnected()+"");
                        //Log.i(TAG,socket.isInputShutdown()+"");
                        //Log.i(TAG,socket.isOutputShutdown()+"");
                        String x = brc.readLine();


                        if (!(x.equals(""))) {
                            Log.i("CLIENT", "client side reached5");
                            //if (x.equals(ackc)) {
                                Log.i("CLIENT", "client side reached6");
                                pwc.close();
                                brc.close();
                                socket.close();
                            //}

                            //make cursor


                            int a=x.indexOf(",,");
                            String k=x.substring(0,a);
                            String v=x.substring(a+2,x.length());

                            ContentValues cv=new ContentValues();
                            cv.put("key",k);
                            cv.put("value",v);
                            db.insertIntoDb(cv,"temp");
                            tempCursorCount++;
                            Log.i("result-----",x);
                            c=db.queryDb(k,"temp");



                        }

                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }


            }

        }
        starPort=myPort;
        Log.i("Query result found","////////////////////////////////////////");
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }






    private class ClientTask extends AsyncTask<String, Void, Void>
    {
        OutputStream osc;
        PrintWriter pwc;
        InputStream isc;
        InputStreamReader isrc;
        BufferedReader brc;
        Socket socket;
        String remotePort;
        String reqType;
        String msg;
        String ackc;
        protected Void doInBackground(String... msgs)
        {
            Log.i("-----------------------","-----------------------------");
            Log.i("---","--------------------------CLIENT--------------------------");
            Log.i("-----------------------","-----------------------------");
            try {

                remotePort = msgs[1];
                reqType=msgs[2];
                msg=msgs[0];
                //if (msgs[1].equals(REMOTE_PORT_Pre))
                  //  remotePort = REMOTE_PORT1;
                Log.i("CLIENT",remotePort);



                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort));
                //String msgToSend = reqType+",,"+msg;
                //Log.i("msgclient",msgToSend);
                //Log.i("reqclient",reqType);
                //Log.i("rportclient",remotePort);
                String msgToSend=reqType+",,"+msg+"\n";
                ackc="PA1_OK";
                osc=socket.getOutputStream();
                pwc=new PrintWriter(osc);
                Log.i("CLIENT",msgToSend);
                pwc.write(msgToSend);
                pwc.flush();

                Log.i("CLIENT","client side reached1");
                //os.close();

                isc = socket.getInputStream();
                isrc=new InputStreamReader(isc);
                Log.i("CLIENT","client side reached2");
                brc = new BufferedReader(isrc);
                Log.i("CLIENT","client side reached4");
                //Log.i(TAG,socket.isConnected()+"");
                //Log.i(TAG,socket.isInputShutdown()+"");
                //Log.i(TAG,socket.isOutputShutdown()+"");
                String x=brc.readLine();


                    if (!(x.equals(""))) {
                        Log.i("CLIENT", "client side reached5");
                        if (x.equals(ackc)) {
                            Log.i("CLIENT", "client side reached6");
                            pwc.close();
                            brc.close();
                            socket.close();
                        }

                    }


                //Log.i("CLIENT",socket.isClosed()+"");

            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }

            Log.i(" client reached here", "*******************************************");
            Log.i(" client reached here",socket.isConnected()+"");
            return null;
        }
    }









    private class ServerTask extends AsyncTask<ServerSocket, String, Void>
    {
        Socket s1;
        InputStream iss;
        OutputStream oss;
        PrintWriter pws;
        InputStreamReader isrs;
        BufferedReader brs;
        String acks="PA1_OK";
        String string_msg;
        @Override
        protected Void doInBackground(ServerSocket... sockets)
        {
            ServerSocket serverSocket = sockets[0];

            Log.i("-----------------------","-----------------------------");
            Log.i("---","--------------------------SERVER--------------------------");
            Log.i("-----------------------","-----------------------------");
            try
            {
                while(true)
                {
                    s1 = serverSocket.accept();
                    Log.i("-----------------------","-----------------------------");
                    Log.i("---","--------------------------SERVER JOIN NODE--------------------------");
                    Log.i("-----------------------","-----------------------------");
                    Log.i("SERVER","reached1");
                    iss = s1.getInputStream();
                    Log.i("SERVER","reached2");
                    isrs = new InputStreamReader(iss);
                    Log.i("SERVER","reached3");
                    brs = new BufferedReader(isrs);
                    Log.i("SERVER","reached4");
                    string_msg = brs.readLine();
                    Log.i("SERVER",string_msg);

                    String reqType;
                    String msgText;
                    String msgKey;
                    String port;
                    int a=string_msg.indexOf(",,");
                    reqType=string_msg.substring(0,a);


                    if((reqType.equals("query"))||(reqType.equals("star")))
                    {

                    }
                    else
                    {
                        oss=s1.getOutputStream();
                        pws=new PrintWriter(oss);
                        pws.write(acks);
                        pws.flush();
                        Log.i("SERVER","reached5");
                        //publishProgress(string_msg);
                        pws.close();
                        brs.close();
                        s1.close();

                    }


                    //seperate key value and and request type from string_msg


                    if(reqType.equals("join"))
                    {
                        //Log.i("SERVER",(a+2)+"");
                        //Log.i("SERVER",string_msg);
                        port=string_msg.substring(a+2,string_msg.length());
                        //Log.i("SERVER",port);
                        ports.add(port);
                        nodeids.add(genHash(port));
                        nodes.put(genHash(port),port);
                        /*for(int i=0;i<ports.size();i++)
                        {
                            Log.i("inserver",ports.get(i));
                            Log.i("inserver",nodeids.get(i));
                            //Log.i("inserver",nodes.get(genHash(ports.get(i))));

                        }*/

                        Collections.sort(nodeids);

                        firstPort=nodes.get(nodeids.get(0));


                        for(int i=0;i<ports.size();i++)
                        {
                            Log.i("inserver",nodeids.get(i));
                            Log.i("inserver",nodes.get(nodeids.get(i)));
                        }

                        for(int i=0;i<nodeids.size();i++)
                        {
                            String pre;
                            String succ;
                            String current=nodes.get(nodeids.get(i));
                            if(i==0)
                            {
                                succ=nodeids.get(i+1);
                                pre=nodeids.get(nodeids.size()-1);
                            }
                            else if(i==nodeids.size()-1)
                            {
                                succ=nodeids.get(0);
                                pre=nodeids.get(i-1);
                            }
                            else
                            {
                                succ=nodeids.get(i+1);
                                pre=nodeids.get(i-1);
                            }

                            Log.i("current",current);
                            Log.i("pre",pre);
                            Log.i("succ",succ);

                            if(current.equals(myPort))
                            {
                                successorNodeKey=succ;
                                predecessorNodeKey=pre;
                                REMOTE_PORT_Succ=nodes.get(succ);
                                REMOTE_PORT_Pre=nodes.get(pre);
                            }
                            else
                            {
                                try {


                                    OutputStream osc;
                                    PrintWriter pwc;
                                    InputStream isc;
                                    InputStreamReader isrc;
                                    BufferedReader brc;



                                    String remotePort = current;
                                    String req="joinResponse";
                                    String msg=req+",,"+nodes.get(pre)+",,"+nodes.get(succ)+",,"+firstPort+"\n";

                                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                            (Integer.parseInt(remotePort)*2));

                                    String ackc="PA1_OK";

                                    osc=socket.getOutputStream();
                                    pwc=new PrintWriter(osc);
                                    pwc.write(msg);
                                    pwc.flush();

                                    Log.i("CLIENT","client side reached1");
                                    //os.close();

                                    isc = socket.getInputStream();
                                    isrc=new InputStreamReader(isc);
                                    Log.i("CLIENT","client side reached2");
                                    brc = new BufferedReader(isrc);
                                    Log.i("CLIENT","client side reached4");
                                    //Log.i(TAG,socket.isConnected()+"");
                                    //Log.i(TAG,socket.isInputShutdown()+"");
                                    //Log.i(TAG,socket.isOutputShutdown()+"");
                                    String x=brc.readLine();


                                    if (!(x.equals(""))) {
                                        Log.i("CLIENT", "client side reached5");
                                        if (x.equals(ackc)) {
                                            Log.i("CLIENT", "client side reached6");
                                            pwc.close();
                                            brc.close();
                                            socket.close();
                                        }

                                    }


                                    //Log.i("CLIENT",socket.isClosed()+"");

                                }
                                catch (UnknownHostException e)
                                {
                                    e.printStackTrace();
                                }
                                catch (Exception e1)
                                {
                                    e1.printStackTrace();
                                }

                            }

                        }


                        /*for(int i=0;i<nodeids.size();i++)
                        {
                            Log.i("inserver",nodeids.get(i));
                            //Log.i("inserver",nodes.get(genHash(ports.get(i))));

                        }*/




                    }

                    else if(reqType.equals("joinResponse"))
                    {
                        String response=string_msg.substring(a+2,string_msg.length());
                        Log.i("++++++RESPONSE++++++",response);


                        /*int b=response.indexOf(",,");
                        REMOTE_PORT_Pre=response.substring(0,b);
                        REMOTE_PORT_Succ=response.substring(b+2,response.length());
*/
                        int b=response.indexOf(",,");
                        REMOTE_PORT_Pre=response.substring(0,b);
                        String temp=response.substring(b+2,response.length());
                        b=temp.indexOf(",,");
                        REMOTE_PORT_Succ=temp.substring(0,b);
                        firstPort=temp.substring(b+2,temp.length());


                        successorNodeKey=genHash(REMOTE_PORT_Succ);
                        predecessorNodeKey=genHash(REMOTE_PORT_Pre);
                    }




                    else if(reqType.equals("insert"))
                    {
                        String temp=string_msg.substring(a+2,string_msg.length());
                        a=temp.indexOf(",,");
                        msgKey=temp.substring(0,a);
                        msgText=temp.substring(a+2,temp.length());
                        String hashedMsgKey=genHash(msgKey);
                        if(reqType.equals("insert"))
                        {
                            Log.i("server-retype=",reqType);
                            Log.i("server-text=",msgText);
                            Log.i("server-key=",msgKey);
                            Log.i("server-hashedkey=",hashedMsgKey);
                            ContentValues cv=new ContentValues();
                            cv.put("key",msgKey);
                            cv.put("value",msgText);
                            Uri.Builder ub;
                            Uri uri;
                            ub=new Uri.Builder();
                            ub.authority("edu.buffalo.cse.cse486586.simpledht.provider");
                            uri=ub.build();
                            Log.i("b1", (hashedMsgKey.compareTo(myNodeKey)) + "");
                            Log.i("b2", (hashedMsgKey.compareTo(predecessorNodeKey)) + "");

                          insert(uri,cv);
                            //insert();
                        }


                    }




                    else if(reqType.equals("query"))
                    {
                        Uri.Builder ub;
                        Uri uri;
                        ub=new Uri.Builder();
                        ub.authority("edu.buffalo.cse.cse486586.simpledht.provider");
                        uri=ub.build();
                        String sel=string_msg.substring(a+2,string_msg.length());
                        Cursor c;
                        c=query(uri, null,sel, null, null);
                        String value=c.getString(1);
                        String key=c.getString(0);

                        //try appending new line
                        String msg=key+",,"+value;

                        oss=s1.getOutputStream();
                        pws=new PrintWriter(oss);
                        pws.write(msg);
                        pws.flush();
                        Log.i("SERVER","reached5");
                        //publishProgress(string_msg);
                        pws.close();
                        brs.close();
                        s1.close();



                    }





                    else if(reqType.equals("star"))
                    {
                        Uri.Builder ub;
                        Uri uri;
                        ub=new Uri.Builder();
                        ub.authority("edu.buffalo.cse.cse486586.simpledht.provider");
                        uri=ub.build();

                        starPort=string_msg.substring(a+2,string_msg.length());
                        Cursor c;
                        c=query(uri,null,"*", null, null);


                        //check if cursor is empty
                        //get values from cursor and form a string and return it
                        String msg;
                        if(c.getCount()==0)
                        {
                            msg="blank";

                        }
                        else
                        {
                            String key=c.getString(0);
                            String value=c.getString(1);
                            msg=key+",,"+value;
                            c.moveToNext();
                            for(int i=0;i<c.getCount()-1;i++)
                            {
                                value=c.getString(1);
                                key=c.getString(0);
                                msg=msg+","+key+",,"+value;
                                c.moveToNext();
                            }
                        }





                        //try appending new line
                        //String msg=key+",,"+value;

                        oss=s1.getOutputStream();
                        pws=new PrintWriter(oss);
                        pws.write(msg);
                        pws.flush();
                        Log.i("SERVER","reached5");
                        //publishProgress(string_msg);
                        pws.close();
                        brs.close();
                        s1.close();



                    }


                    //if insert request
                    //check hashed key
                    //either accept it or forward
                    // +it

                    //else
                    //query request
                    // get the value from key
                    //backtrack



                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

    }
}












