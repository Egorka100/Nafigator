package ru.nafigator;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

/**
 * Created by denis on 15.08.2017.
 */

public class Load_points extends AppCompatActivity implements
        View.OnClickListener
        {
            ListView lvMyPoints;
            DBHelper dbHelper;
            protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
                lvMyPoints=(ListView) findViewById(R.id.lvMyPoints);
                dbHelper=new DBHelper(this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor c = db.query("mytable", null, null, null, null, null, null);
                if(c.moveToFirst()){
                    int idColIndex=c.getColumnIndex("id");
                    int nameColIndex=c.getColumnIndex("name");

                do{
                    //c.getInt(idColIndex);

                    c.getString(nameColIndex);
                }while (c.moveToNext());}
            }
            @Override
            public void onClick(View v) {

            }
            public void Get_names() {


            }


        }
