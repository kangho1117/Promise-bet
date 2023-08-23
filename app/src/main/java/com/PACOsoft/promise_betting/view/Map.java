package com.PACOsoft.promise_betting.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.PACOsoft.promise_betting.R;
import com.PACOsoft.promise_betting.obj.Promise;
import com.PACOsoft.promise_betting.obj.PromisePlayer;
import com.PACOsoft.promise_betting.obj.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;


public class Map extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private static NaverMap naverMap;
    private static final int PERMISSION_REQUEST_CODE = 100; // 권한 부여 코드 생성
    private FusedLocationSource locationSource; //현재 위치를 나타내줄 로케이션 소스
    private static final String[] PERMISSIONS = { //위치 권한을 담은 배열
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private DrawerLayout drawerLayout;
    private View drawerView;
    private Promise promise;
    private TextView people_number, room_name, reach_location;
    private LinearLayout players;
    private String[] location_xy;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private String rid;
    private String UID;
    private User me;
    private PromisePlayer promisePlayer_me;
    private ArrayList<PromisePlayer> promisePlayers;
    private NaverMap.OnLocationChangeListener locationListener;
    private ValueEventListener promiseListener;
    private ValueEventListener userListener;
    private int i = 0;

    //방 콜백 인터페이스
    public interface MyCallback {
        void onCallback(Promise promise);
    }

    //유저 콜백 인터페이스
    public interface MyCallback2 {
        void onCallback(User user);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerView = (View) findViewById(R.id.drawer);
        drawerLayout.setDrawerLockMode(drawerLayout.LOCK_MODE_LOCKED_CLOSED);

        //네이버 지도
        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        locationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

        people_number = findViewById(R.id.tv_room_people_count);
        room_name = findViewById(R.id.tv_room_promise);
        players = findViewById(R.id.player_list_lo);
        reach_location = findViewById(R.id.reach_location_btn);

        //rid사용해서 콜백으로 객체 가져오기
        rid = getIntent().getStringExtra("rid");
        UID = getIntent().getStringExtra("UID");
        readPromise(new MyCallback() {
            @Override
            public void onCallback(Promise p) {
                promise = p;
                //방설정
                people_number.setText(String.valueOf(promise.getNumOfPlayer()));
                room_name.setText(promise.getPromiseName());
                location_xy = promise.getPromisePlace().split(" ");
                for (PromisePlayer i : promise.getPromisePlayer()) {
                    TextView tv = new TextView(getApplicationContext());
                    tv.setText(i.getNickName());
                    tv.setTextSize(15);
                    tv.setGravity(1);
                    players.addView(tv);
                }
            }
        });

        //나를 식별하기 위해 콜백으로 user데이터를 가져옴
        readUser(new MyCallback2() {
            @Override
            public void onCallback(User user) {
                me = user;
            }
        });

        //나를 식별하여 promisePlayer 정보 가져오기
        promisePlayers = promise.getPromisePlayer();
        for(i = 0; i < promisePlayers.size(); i++){
            if(promisePlayers.get(i).getPlayerUID().equals(UID)){
                promisePlayer_me = promisePlayers.get(i);
                return;
            }
        }
    }

    //방 콜백 메소드생성
    public void readPromise(MyCallback myCallback) {
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Promise").child(rid);
        promiseListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Promise p = dataSnapshot.getValue(Promise.class);
                myCallback.onCallback(p);//최강 콜백!!
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Map", String.valueOf(databaseError.toException()));
            }
        };
        databaseReference.addValueEventListener(promiseListener);
    }

    //유저 콜백 메소드생성
    public void readUser(MyCallback2 myCallback2) {
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("User").child(UID);
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                myCallback2.onCallback(user);//최강 콜백!!
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Map", String.valueOf(databaseError.toException()));
            }
        };
        databaseReference.addValueEventListener(userListener);
    }

    public void room_menu(View view) {
        drawerLayout.openDrawer(drawerView);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource); //현재 위치 반영
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

        double x = Double.valueOf(location_xy[0]);
        double y = Double.valueOf(location_xy[1]);
        Marker locat = new Marker();
        locat.setPosition(new LatLng(y, x));
        locat.setMap(naverMap);

        CircleOverlay circle = new CircleOverlay();
        circle.setCenter(new LatLng(y, x));
        circle.setRadius(50);
        circle.setColor(Color.argb(70, 153, 232, 174));
        circle.setOutlineWidth(5);
        circle.setOutlineColor(Color.argb(70, 0, 0, 0));
        circle.setMap(naverMap);
        locationListener = new NaverMap.OnLocationChangeListener(){
            @Override
            public void onLocationChange(@NonNull Location location) {
                Location A = new Location("point A");
                A.setLatitude(location.getLatitude());
                A.setLongitude(location.getLongitude());

                Location B = new Location("point B");
                B.setLatitude(y);
                B.setLongitude(x);

                double distance = A.distanceTo(B);
                //TODO: if문에 시간 조건도 추가하기
                if (distance <= 50.0) {
                    reach_location.setEnabled(true);
                }

                //내위치 저장
                database = FirebaseDatabase.getInstance();
                databaseReference = database.getReference("Promise").child(rid).child("promisePlayer").child(String.valueOf(i));
                databaseReference.child("x").setValue(x);
                databaseReference.child("y").setValue(y);
            }
        };
        naverMap.addOnLocationChangeListener(locationListener);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // request code와 권한 획득 여부 확인
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }

    public void target_select(View view){
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
    }

    public void btn_reach_place(View view) {
        promisePlayer_me.setArrival(true);
    }

    public void btn_vote_start(View view){
    }

    @Override
    public void onBackPressed() {
       super.onBackPressed();
       naverMap.removeOnLocationChangeListener(locationListener);
       databaseReference.removeEventListener(promiseListener);
       databaseReference.removeEventListener(userListener);
       finish();
    }

}