package ru.nafigator;


import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.http.GET;
import retrofit.http.Query;

import static android.view.View.VISIBLE;
import static ru.nafigator.R.drawable.marker;
import static ru.nafigator.R.id.map;

public class MyMapActivity extends FragmentActivity implements View.OnClickListener,OnMapReadyCallback,GoogleMap.OnMarkerClickListener{
    private TextView showaddress;
    private Button drop_menu;
    private Button show_all;
    private Button choose_map;
    public GridLayout menus_buttons;
    public MapFragment mapFragment;
    public HorizontalScrollView scroll_menu;
    public double mylat;
    public double mylon;
    public String MyPositionString,
            SourcePositionString;
    GoogleMap mapfortrack;
    private View infoWindowContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapFragment = ((MapFragment) getFragmentManager()
                .findFragmentById(map));
        mapFragment.getMapAsync(this);

        showaddress = (TextView) findViewById(R.id.showaddress);
        menus_buttons=(GridLayout) findViewById(R.id.menus_buttons);
        scroll_menu=(HorizontalScrollView) findViewById(R.id.scroll_menu);
        show_all=(Button) findViewById(R.id.show_all);
        show_all.setOnClickListener(this);
        drop_menu=(Button) findViewById(R.id.drop_menu);
        drop_menu.setOnClickListener(this);
        choose_map=(Button) findViewById(R.id.choose_map);
        choose_map.setOnClickListener(this);
        infoWindowContainer=findViewById(R.id.container_popup);

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
                showRoute();
        }
    }
    private void init(){
        mapfortrack.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                infoWindowContainer.setVisibility(View.INVISIBLE);
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
    @Override
    public void onMapReady(GoogleMap map) {
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapfortrack=map;
        init();
        GPSTracker gps = new GPSTracker(this);
        mylat = gps.getLatitude();
        mylon = gps.getLongitude();
        LatLng mapCenter = new LatLng(mylat, mylon);
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
        getaddress(mylat,mylon);
    }

    public void getaddress(double lat, double lon){
        MyPositionString=Double.toString(mylat)+","+Double.toString(mylon);
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

    @Override
    public boolean onMarkerClick(Marker marker) {

        //заполняем и показываем окно
        //…
        infoWindowContainer.setVisibility(VISIBLE);
        return true;
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){

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

                mapfortrack.addPolyline(line);
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

}

