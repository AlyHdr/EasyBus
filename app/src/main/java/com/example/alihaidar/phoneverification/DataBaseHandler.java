package com.example.alihaidar.phoneverification;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Ali Haidar on 12/6/2017.
 */

public class DataBaseHandler extends SQLiteOpenHelper {

    private static final  int DATABASE_VERSION=10;
    private static final  String DATABASE_NAME="studentsManager";

    private static final  String TABLE_NOTIFICATIONS="notifications";

    private static final  String KEY_BODY="body";
    private static final  String KEY_TITLE="title";
    private static final  String KEY_DATE="date";


    private static final  String TABLE_VISITED="visited";
    private static final  String KEY_VISITED_ID="id";
    private static final  String KEY_IS_VISITED="isVisited";

    private static final  String TABLE_STUDENTS="students";

    private static final  String KEY_ID="id";
    private static final  String KEY_NAME="name";
    private static final  String KEY_PH_NO="phone_number";
    private static final  String KEY_LATLNG="latlng";
    private static final  String KEY_DRIVER_PHONE="driverPhone";
    private static final  String KEY_FENCE="fenceRaduis";

    private static final  String TABLE_ORDERED_STUDENTS="orderedStudents";
    private static final  String KEY_Lat="latitude";
    private static final  String KEY_Long="longitude";

    Context context;
    public DataBaseHandler(Context c)
    {
        super(c,DATABASE_NAME,null,DATABASE_VERSION);
        context=c;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_STUDENTS + "("+ KEY_ID + " INTEGER," +  KEY_NAME + " TEXT,"+ KEY_DRIVER_PHONE + " TEXT," + KEY_LATLNG+" TEXT,"+ KEY_PH_NO+" TEXT, "+KEY_FENCE+" INTEGER)";
        db.execSQL(CREATE_CONTACTS_TABLE);
       String CREATE_ORDERED_STUDENTS_TABLE= "CREATE TABLE " + TABLE_ORDERED_STUDENTS+ "("+ KEY_Lat + " REAL," +  KEY_Long+ " REAL)";
        db.execSQL(CREATE_ORDERED_STUDENTS_TABLE);
        String CREATE_VISITED_TABLE= "CREATE TABLE " + TABLE_VISITED+ "("+ KEY_VISITED_ID + " INTEGER," +  KEY_IS_VISITED+ " INTEGER)";
        db.execSQL(CREATE_VISITED_TABLE);
        String CREATE_NOTIFICATIONS_TABLE= "CREATE TABLE " + TABLE_NOTIFICATIONS+ "("+  KEY_BODY+ " TEXT,"+KEY_TITLE+ " TEXT, "+KEY_DATE+" TEXT)";
        db.execSQL(CREATE_NOTIFICATIONS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERED_STUDENTS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_VISITED);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        onCreate(sqLiteDatabase);
    }
    public void addNotification(Message message)
    {
        ContentValues content=new ContentValues();
        content.put(KEY_BODY,message.body);
        content.put(KEY_TITLE,message.title);
        content.put(KEY_DATE,message.date);
        SQLiteDatabase db=getWritableDatabase();
        db.insert(TABLE_NOTIFICATIONS,null,content);
    }
    public ArrayList<Message> getNotifications()
    {
        SQLiteDatabase db=getReadableDatabase();
        String query="select * from "+TABLE_NOTIFICATIONS;
        Cursor cursor=db.rawQuery(query,null);
        ArrayList<Message> notifications=new ArrayList<>();
        if(cursor.moveToNext())
        {
            do{
                Message message=new Message(cursor.getString(1),cursor.getString(0),cursor.getString(2));
                notifications.add(message);
            }while(cursor.moveToNext());
        }
        db.close();
        return notifications;
    }
    public void addStudents(ArrayList<Student> students)
    {
        SQLiteDatabase db=getWritableDatabase();
        db.execSQL("DELETE FROM "+TABLE_STUDENTS);
        for(Student student:students)
        {
            ContentValues content = new ContentValues();
            content.put(KEY_ID,student.id);
            content.put(KEY_NAME,student.getName());
            content.put(KEY_DRIVER_PHONE, student.getDriverPhone());
            String latlng=student.getLatLng().latitude+" "+student.getLatLng().longitude;
            content.put(KEY_LATLNG, latlng);
            content.put(KEY_PH_NO,student.getPhNumber());
            content.put(KEY_FENCE,student.getFenceRaduis());
            db.insert(TABLE_STUDENTS, null, content);
        }
        db.close();
    }
    /*public ArrayList<Student> getStudents()
    {
        SQLiteDatabase db=getReadableDatabase();
        String query="select * from "+TABLE_STUDENTS;
        Cursor cursor=db.rawQuery(query,null);
        ArrayList<Student> students=new ArrayList<>();
        if(cursor.moveToNext())
        {
            do{
                String[] latlng=cursor.getString(3).split(" ");
                LatLng l=new LatLng(Double.parseDouble(latlng[0]),Double.parseDouble(latlng[1]));
                Student student=new Student(Integer.parseInt(cursor.getString(0)),cursor.getString(1),cursor.getString(2),l,cursor.getString(4));
                students.add(student);
            }while(cursor.moveToNext());
        }
        db.close();
        return students;
    }*/

    public void addOrderedStudents(ArrayList<Student> orderedStudents) {
        SQLiteDatabase db=getWritableDatabase();
        db.execSQL("DELETE FROM "+TABLE_ORDERED_STUDENTS);
        for(Student student:orderedStudents) {
            ContentValues content = new ContentValues();
            content.put(KEY_Lat,student.getLatLng().latitude);
            content.put(KEY_Long,student.getLatLng().longitude);
            db.insert(TABLE_ORDERED_STUDENTS, null, content);
        }
        db.close();
    }

    public void putVisitedStudents(ArrayList<Student> students)
    {
        SQLiteDatabase db=getWritableDatabase();
        db.execSQL("DELETE FROM "+TABLE_VISITED);
        for(Student s:students) {
            ContentValues content = new ContentValues();
            content.put(KEY_VISITED_ID,s.id);
            content.put(KEY_IS_VISITED,0);
            db.insert(TABLE_VISITED, null, content);
        }
        db.close();
    }
    public void updateVisited(int id)
    {
        SQLiteDatabase db=getWritableDatabase();
        String query="update "+TABLE_VISITED+ " set "+KEY_IS_VISITED+"=1 where "+KEY_VISITED_ID+" ="+id;
        System.out.println("queryyyyyyyyyyyyyyyyyyyy "+id+" "+query);
        db.execSQL(query);
        db.close();
    }
    public void printVisited()
    {
        SQLiteDatabase db=getReadableDatabase();
        String query="select * from "+TABLE_VISITED;
        Cursor cursor=db.rawQuery(query,null);
        if(cursor.moveToNext())
        {
            do {
                System.out.print("--------------- id: "+cursor.getInt(0));
                System.out.println("--------------- visited "+cursor.getInt(1));
            }
            while (cursor.moveToNext());
        }
    }
    public boolean getVisited(int id)
    {
        SQLiteDatabase db=getReadableDatabase();
        String query="select * from "+TABLE_VISITED+" where "+KEY_VISITED_ID+"="+id;
        Cursor cursor=db.rawQuery(query,null);
        boolean res=false;
        if(cursor.moveToNext())
        {

            if(cursor.getInt(1)==1)
                res=true;
        }
        db.close();
        return res;
    }
}
