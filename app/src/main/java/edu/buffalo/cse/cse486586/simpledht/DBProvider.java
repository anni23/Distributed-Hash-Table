package edu.buffalo.cse.cse486586.simpledht;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.Context;
import android.util.Log;

public class DBProvider extends SQLiteOpenHelper
{
    public static final String DatabaseName="SimpleDht";
    public static final String TableName="MessegesNew1";
    public static final String TableTemp="Temp";
    public static final String ColName1="key";
    public static final String ColName2="value";
    public static final String CreateTableQuery="create table "+TableName+"("+ColName1+" varchar(50) primary key, "+ColName2+" varchar(50) not null);";
    public static final String CreateTableTempQuery="create table "+TableTemp+"("+ColName1+" varchar(50) primary key, "+ColName2+" varchar(50) not null);";
    SQLiteDatabase d;
    public DBProvider(Context ctx)throws SQLException
    {
        super(ctx,DatabaseName,null,1);
    }
    @Override
    public void onCreate(SQLiteDatabase db)throws SQLException
    {
        db.execSQL(CreateTableQuery);
        db.execSQL(CreateTableTempQuery);

        Log.i("db provider","reached1");
    }

    public void insertIntoDb(ContentValues cv,String x)throws SQLException
    {
        try
        {

            if(x.equals("temp"))
            {
                d = getWritableDatabase();
                d.insertWithOnConflict(TableTemp, null, cv,SQLiteDatabase.CONFLICT_REPLACE);
            }
            else
            {
                d = getWritableDatabase();
                long l = d.insertWithOnConflict(TableName, null, cv,SQLiteDatabase.CONFLICT_REPLACE);
                if (l == -1)
                    Log.i("DbProvider", "error occured");
                else
                    Log.i("DbProvider", "done");
                //String x=cv.get("1").toString();
                //Log.i("DbProvider",x);
                Log.i("db provider","reached2");
            }



        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public Cursor queryDb(String sel,String x)throws SQLException
    {
        Log.i("database","--------------------------------------------------------");
        Cursor c;
        String s[]={ColName2};
        d=getReadableDatabase();
        if(x.equals("temp"))
        {
            if(sel.equals("*"))
            {
                c=d.query(TableTemp,null,null,null,null,null,null,null);
                c.moveToFirst();
                Log.i("key",(c.getCount()+""));
            }
            else
            {
                c=d.query(TableTemp,null,ColName1+"='"+sel+"'",null,null,null,null,null);
                c.moveToFirst();
                Log.i("key",(c.getCount()+""));
            }

        }
        else
        {
            if(sel.equals("*"))
            {
                c=d.query(TableName,null,null,null,null,null,null,null);
                c.moveToFirst();
                Log.i("key",(c.getCount()+""));
            }
            else
            {
                c=d.query(TableName,null,ColName1+"='"+sel+"'",null,null,null,null,null);
                c.moveToFirst();
                Log.i("key",(c.getCount()+""));
            }
        }


  //      Log.i("value",(c.getString(1)+""));
        //c=d.query(TableName,null,null,null,null,null,null,null);
        return c;
    }


    public void delete(String x)
    {
        if(x.equals("temp"))
        {
            d.delete(TableTemp,null,null);
        }
        else
        {
            d.delete(TableName,ColName1+"='"+x+"'",null);
        }

    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}