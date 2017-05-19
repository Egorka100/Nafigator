package ru.nafigator;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.http.GET;
import retrofit.http.Query;

import static android.view.View.VISIBLE;
import static ru.nafigator.R.drawable.marker;
import static ru.nafigator.R.id.map;

public class MyMapActivity extends FragmentActivity implements View.OnClickListener, OnMapReadyCallback {
    private TextView showaddress;

    private Button drop_menu;
    private Button show_all;
    private Button choose_map;
    private Button writelog;

    public GridLayout menus_buttons;
    public MapFragment mapFragment;
    public HorizontalScrollView scroll_menu;

    public double mylat;
    public double mylon;

    public String MyPositionString,
            SourcePositionString;

    GoogleMap mapfortrack;

    private LocationManager locationManager;

    public boolean FlagForCamera;
    public int FlagForLogs;

    private Polyline linefortrack;

    final String FILE_NAME = "tracklog2.txt";

    public double lat;
    public double lng;

    private final int IDD_LIST_MARKER_MENU = 0;

    LatLng selectedmarkerposition;

    public String sourceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        FlagForCamera=true;
        FlagForLogs=1;


        mapFragment = ((MapFragment) getFragmentManager()
                .findFragmentById(map));
        mapFragment.getMapAsync(this);

        showaddress = (TextView) findViewById(R.id.showaddress);
        menus_buttons = (GridLayout) findViewById(R.id.menus_buttons);
        scroll_menu = (HorizontalScrollView) findViewById(R.id.scroll_menu);

        show_all = (Button) findViewById(R.id.show_all);
        show_all.setOnClickListener(this);

        drop_menu = (Button) findViewById(R.id.drop_menu);
        drop_menu.setOnClickListener(this);

        choose_map = (Button) findViewById(R.id.choose_map);
        choose_map.setOnClickListener(this);

        writelog=(Button)findViewById(R.id.writelog);
        writelog.setOnClickListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


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
            case R.id.choose_map:
                break;
            case R.id.writelog:
                testtrack();
                /*
                FlagForLogs++;
                if(FlagForLogs%2==0){
                    writelog.setText("Остановить запись");
                }
                else{writelog.setText("Писать Лог");}*/
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 1, 1, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 1, 1,
                locationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        locationManager.removeUpdates(locationListener);
    }

    private void init(){
        mapfortrack.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                FlagForCamera=false;
            }
        });
        mapfortrack.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mapfortrack.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(marker))
                        .position(latLng)
                        .flat(false));
                SourcePositionString=latLng.latitude+","+latLng.longitude;
            }
        });
        mapfortrack.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                showDialog(IDD_LIST_MARKER_MENU);
                selectedmarkerposition=marker.getPosition();
                getmarkeraddress(selectedmarkerposition.latitude,selectedmarkerposition.longitude);
                return true;
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case IDD_LIST_MARKER_MENU:
                final String[] Menus_positions ={"Проложить маршрут", "Сохранить место", "Удалить место"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(MyMapActivity.this);
                builder.setTitle("Выберите действие"); // заголовок для диалога
                builder.setItems(Menus_positions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item){
                            case 0:
                                showRoute();
                                break;
                            case 1:
                                Intent intent = new Intent(MyMapActivity.this,SaveMarkerForm.class);
                                intent.putExtra("tLocation",selectedmarkerposition.latitude+";"+selectedmarkerposition.longitude);
                                intent.putExtra("tAddress",sourceAddress);
                                startActivity(intent);
                                break;
                            case 2:
                                break;

                        }
                        Toast.makeText(getApplicationContext(),
                                "Выбрано действие: " + Menus_positions[item],
                                Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setCancelable(true);
                return builder.create();

            default:
                return null;
        }
    }

    public void drop_all_bottom_menu(){
        if(scroll_menu.getVisibility()==View.GONE){
            show_all.setText("Меньше");
            scroll_menu.setVisibility(VISIBLE);
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
            menus_buttons.setVisibility(VISIBLE);
            show_all.setVisibility(VISIBLE);
        }
        else
        {
            drop_menu.setText("Вверх");
            menus_buttons.setVisibility(View.GONE);
            show_all.setVisibility(View.GONE);
            scroll_menu.setVisibility(View.GONE);
        }
    }
    public void gowith(double lat,double lon){
        LatLng mapCenter=new LatLng(lat,lon);
        mapfortrack.moveCamera(CameraUpdateFactory.newLatLng(mapCenter));
    }
    @Override
    public void onMapReady(GoogleMap map) {
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapfortrack=map;
        init();
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
        LatLng mapCenter=new LatLng(mylat,mylon);
        mapfortrack.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter,17));

    }
    public void getmarkeraddress(double lat,double lon){
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        try{

            address=coder.getFromLocation(lat,lon,5);
            Address location=address.get(0);
            sourceAddress=location.getAddressLine(1)+", "+location.getAddressLine(0);

        }catch (Exception e){
            sourceAddress="Не могу получить адрес по координатам:"+lat+";"+lon;
        }
    }
    public void getmyaddress(double lat, double lon){
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        try{
            address=coder.getFromLocation(lat,lon,5);
            Address location=address.get(0);

            showaddress.setText(location.getAddressLine(1)+", "+location.getAddressLine(0)+", "+lat+", "+lon);
        }catch (Exception e){
            showaddress.setText("Не могу получить адрес по координатам:"+lat+";"+lon);
        }
    }
    //Класс точки маршрута движения
    public class RouteResponse {

        public List<Route> routes;

        public String getPoints() {
            return this.routes.get(0).overview_polyline.points;
        }

        class Route {
            OverviewPolyline overview_polyline;
        }

        class OverviewPolyline {
            String points;
        }
    }
    //Интерфейс для запросак маршрута
    public interface RouteApi {
        @GET("/maps/api/directions/json")
        void getRoute(
                @Query(value = "origin", encodeValue = false) String position,
                @Query(value = "destination", encodeValue = false) String destination,
                @Query("sensor") boolean sensor,
                @Query("language") String language,
                Callback<RouteResponse> cb
        );
    }

    // метод показа маршрута
    public void showRoute() {
        if(linefortrack!=null) {
            linefortrack.remove();
        }
            //Переход от интерфейса к API
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://maps.googleapis.com")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        RouteApi routeService = restAdapter.create(RouteApi.class);

        //Вызов запроса на маршрут (асинхрон)
        routeService.getRoute(MyPositionString, SourcePositionString, true, "ru", new Callback<RouteResponse>() {
            public void success(RouteResponse arg0, retrofit.client.Response arg1) {
                //Если прошло успешно, то декодируем маршрут в точки LatLng
                List<LatLng> mPoints = PolyUtil.decode(arg0.getPoints());
                //Строим полилинию
                PolylineOptions line = new PolylineOptions();
                line.width(10f).color(R.color.colorPrimary);
                LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
                for (int i = 0; i < mPoints.size(); i++) {

                    line.add(mPoints.get(i));
                    latLngBuilder.include(mPoints.get(i));
                }
                linefortrack = mapfortrack.addPolyline(line);

                int size = getResources().getDisplayMetrics().widthPixels;
                LatLngBounds latLngBounds = latLngBuilder.build();
                CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 25);
                mapfortrack.moveCamera(track);
            }
            //Если запрос прошел неудачно
            public void failure(RetrofitError arg0) {
            }
        });
    }
    public void testtrack(){

        try{
            PolylineOptions line = new PolylineOptions();
            LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();

            BufferedReader reader=new BufferedReader(new FileReader(Environment.getExternalStorageDirectory().toString() + "/" + FILE_NAME));
            List<String> lines=new ArrayList<String>();
            String liness;
            while ((liness=reader.readLine())!=null){
                lines.add(liness);
            }
            int a=0;
            int d=0;
                String[] GEO=lines.get(0).split(":");
                for(int i=0;i<GEO.length;i++){
                    a++;
                    if(d==1){
                        lng=Double.parseDouble(GEO[i]);
                        d++;
                    } else if(d==0){
                        lat=Double.parseDouble(GEO[i]);
                        d++;
                    }else if(d==2){
                        if (i % 2 == 0) {
                            lat = Double.parseDouble(GEO[i]);
                        }
                        if (i % 2 != 0) {
                            lng = Double.parseDouble(GEO[i]);
                        }
                    }
                    if(a==2){
                        LatLng Location=new LatLng(lat,lng);
                        line.width(10f).color(R.color.colorPrimary);
                        line.add(Location);
                        latLngBuilder.include(Location);
                        a=0;
                    }
                }
            linefortrack = mapfortrack.addPolyline(line);
            int size = getResources().getDisplayMetrics().widthPixels;
            LatLngBounds latLngBounds = latLngBuilder.build();
            CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 25);
            mapfortrack.moveCamera(track);

        }catch (IOException e){}
    }
    //Запись логов в корень файловой системы телефона
    void writeFileSD(double lat,double lon) throws IOException {
        // получаем путь к SD
        File myFile = new File(Environment.getExternalStorageDirectory().toString() + "/" + FILE_NAME);
        if (!myFile.exists()) {
            myFile.createNewFile();
            Toast.makeText(this, "Создание файла...", Toast.LENGTH_SHORT).show();
        }
        // формируем объект File, который содержит путь к файлу
        try {
            // открываем поток для записи
            FileWriter fw = new FileWriter(myFile, true);
            // пишем данные
            fw.append("Latitude:" + lat + "\n" + "Longitude:" + lon + "\n" + "\n");
            // закрываем поток
            fw.close();
            Toast.makeText(this, "Сох. точки...", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Расчитываем все промежуточные точки в маршруте
    /*public TakeAllPointsForTrack(){
        if(){
        for(int i){

        }
        }
    }   */
    //Слушатель локации
    private LocationListener locationListener=new LocationListener() {
        //При изменении положения
        @Override
        public void onLocationChanged(Location location) {
            mylat=location.getLatitude();
            mylon=location.getLongitude();
            getmyaddress(mylat,mylon);
            //Проверка условия для ведения камерой за меткой
            if(FlagForCamera==true){
            gowith(mylat,mylon);
            }
            //Составление строки для задания начальной точки для построения маршрута
            MyPositionString=mylat+","+mylon;
            //Проверка флага на четность разрешения записи логов
            if(FlagForLogs%2==0)
            try {
                writeFileSD(mylat,mylon);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


}