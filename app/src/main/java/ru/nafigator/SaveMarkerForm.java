package ru.nafigator;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by denis on 13.05.2017.
 */

public class SaveMarkerForm extends Activity implements View.OnClickListener {
    DBHelper dbHelper;
    final String LOG_TAG = "myLogs";
    Button btnSave,btnRead;
    EditText etName,etEmail,etPhone,etAddress;
    TextView tvLocation;
    String name,address,email,phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       /* MyMapActivity myMapActivity=new MyMapActivity();
        FillInEt(null,myMapActivity.sourceAddress,null,null,myMapActivity.selectedmarkerposition.latitude+";"+myMapActivity.selectedmarkerposition.longitude);*/

        setContentView(R.layout.activity_save_marker);

        btnSave=(Button) findViewById(R.id.btn_save_marker);
        btnSave.setOnClickListener(this);

        btnRead=(Button) findViewById(R.id.test);
        btnRead.setOnClickListener(this);

        etName=(EditText) findViewById(R.id.marker_name);
        etAddress=(EditText) findViewById(R.id.marker_address);
        etEmail=(EditText) findViewById(R.id.marker_email);
        etPhone=(EditText) findViewById(R.id.marker_phone);

        tvLocation=(TextView) findViewById(R.id.tv_show_coordinates);

        dbHelper=new DBHelper(this);
}

    @Override
    public void onClick(View v) {

        ContentValues cv = new ContentValues();

        // получаем данные из полей ввода
        name=etName.getText().toString();
        address=etAddress.getText().toString();
        email=etEmail.getText().toString();
        phone=etPhone.getText().toString();

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (v.getId()) {
            case R.id.btn_save_marker:
                cv.put("name",name);
                cv.put("address",email);
                cv.put("phone",phone);
                cv.put("email",email);
                db.insert("mytable",null,cv);
                Toast.makeText(getApplicationContext(),"Сохранение ",Toast.LENGTH_SHORT).show();
                break;
            case R.id.test:
                Cursor c = db.query("mytable", null, null, null, null, null, null);
                if(c.moveToFirst()){
                    int idColIndex=c.getColumnIndex("id");
                    int nameColIndex=c.getColumnIndex("name");
                    int addressColIndex=c.getColumnIndex("address");
                    int phoneColIndex=c.getColumnIndex("phone");
                    int emailColIndex=c.getColumnIndex("email");

                do{
                    Log.d(LOG_TAG,"ID = "+c.getInt(idColIndex)+
                            ",name = "+c.getString(nameColIndex)+
                            ",address = "+c.getString(addressColIndex)+
                            ",phone = "+c.getString(phoneColIndex)+
                            ",email = "+c.getString(emailColIndex)
                    );
                }while(c.moveToNext());
                                    }
                else
                    Log.d(LOG_TAG,"нет строк");
                c.close();
                break;
        }
        dbHelper.close();
    }
    //Заполняем поля
    public void FillInEt(String tName,String tAddress,String tPhone,String tEmail,String tLocation){
        etName.setText(tName);
        etAddress.setText(tAddress);
        etPhone.setText(tPhone);
        etEmail.setText(tEmail);
        tvLocation.setText(tLocation);
}
}
