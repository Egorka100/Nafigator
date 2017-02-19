package ru.nafigator;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import static ru.nafigator.R.id.map;

public class MyMapActivity extends FragmentActivity implements View.OnClickListener,OnMapReadyCallback {
    private TextView showaddress;
    private Button drop_menu;
    private Button show_all;

    public GridLayout menus_buttons;

    public HorizontalScrollView scroll_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        showaddress = (TextView) findViewById(R.id.showaddress);

        menus_buttons=(GridLayout) findViewById(R.id.menus_buttons);

        scroll_menu=(HorizontalScrollView) findViewById(R.id.scroll_menu);

        show_all=(Button) findViewById(R.id.show_all);
        show_all.setOnClickListener(this);
        drop_menu=(Button) findViewById(R.id.drop_menu);
        drop_menu.setOnClickListener(this);



    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.drop_menu:
                drop_bottom_menu();
                break;
            case R.id.show_all:
                drop_all_bottom_menu();
        }
    }
    public void drop_all_bottom_menu(){
        if(scroll_menu.getVisibility()==View.GONE){
            show_all.setText("Меньше");
            scroll_menu.setVisibility(View.VISIBLE);
        }
        else
        {
            show_all.setText("Больше");
            scroll_menu.setVisibility(View.GONE);
        }
    }
    public void drop_bottom_menu(){
        if(menus_buttons.getVisibility()== View.GONE)
        {
            drop_menu.setText("Вниз");
            menus_buttons.setVisibility(View.VISIBLE);
            show_all.setVisibility(View.VISIBLE);
        }
        else
        {
            drop_menu.setText("Вверх");
            menus_buttons.setVisibility(View.GONE);
            show_all.setVisibility(View.GONE);
            scroll_menu.setVisibility(View.GONE);
        }
    }
    @Override
    public void onMapReady(GoogleMap map) {
        GPSTracker gps = new GPSTracker(this);
        double lat = gps.getLatitude();
        double lon = gps.getLongitude();
        LatLng mapCenter = new LatLng(lat, lon);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true);

       // LatLng mapCenter = new LatLng(41.889, -10.622);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter, 15));

        // Вращение камеры за маркером
       /* map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.direction_arrow))
                .position(mapCenter)
                .flat(true)
                .rotation(245));*/

        CameraPosition cameraPosition = CameraPosition.builder()
                .target(mapCenter)
                .zoom(15)
                .bearing(90)
                .build();

        // Анимация
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                2000, null);
getaddress(lat,lon);
    }
    public void getaddress(double lat, double lon){
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        try{
            address=coder.getFromLocation(lat,lon,5);
            Address location=address.get(0);
            showaddress.setText(location.getAddressLine(1)+", "+location.getAddressLine(0));
        }catch (Exception e){
            showaddress.setText("Не могу получить адрес по координатам:"+lat+";"+lon);
        }
    }
}
