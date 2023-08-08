package com.PACOsoft.promise_betting.view;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import com.PACOsoft.promise_betting.obj.Promise;
import com.PACOsoft.promise_betting.util.Date_Picker;
import com.PACOsoft.promise_betting.R;
import com.PACOsoft.promise_betting.util.Time_Picker;

public class Create_Room extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    TextView timeText, textView, locationText, friendsText;
    private String myId;
    Promise promise;

    String all_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);
        Intent intent = getIntent();
        myId = intent.getStringExtra("myId"); //Home에서 intent해준 id를 받아옴
        locationText = findViewById(R.id.location_Tview);
        friendsText = findViewById(R.id.friends_Tview);
    }

    //날짜데이터 받아오기
    public void processDatePickerResult(int year, int month, int day) {
        String month_string = Integer.toString(month + 1);
        String day_string = Integer.toString(day);
        String year_string = Integer.toString(year);
        String dateMessage = (year_string + " " + month_string + " " + day_string);
        textView = findViewById(R.id.date_Tview);
        textView.setText(dateMessage);
    }

    //날짜 선택 버튼
    public void btn_date_set(View view) {
        DialogFragment newFragment = new Date_Picker();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    //시간 선택 버튼
    public void btn_time_set(View view) {
        DialogFragment timepicker = new Time_Picker();
        timepicker.show(getSupportFragmentManager(), "time picker");
    }

    //위치 선택 버튼
    public void intent_btn_search_local(View view) {
        Intent intent = new Intent(this, Search_Location.class);
        search_local_start.launch(intent);
    }

    ActivityResultLauncher<Intent> search_local_start = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == RESULT_OK){
            Intent intent = result.getData();
            assert intent != null;
            String title = intent.getStringExtra("title");
            String category = intent.getStringExtra("category");
            String address = intent.getStringExtra("address");
            String roadAddress = intent.getStringExtra("road");
            String mapx = intent.getStringExtra("x");
            String mapy = intent.getStringExtra("y");
            Log.v("tt", title);
        }
        if(result.getResultCode() == RESULT_CANCELED){
            Log.e("result error", "받아오기 실패");
        }
    });

    //친구 초대 버튼
    public void btn_intent_invite_friend(View view) {
        Intent intent = new Intent(this, Invite_Friend.class);
        intent.putExtra("myId", myId);//ID 정보 intent
        invite_friend_start.launch(intent);
    }

    ActivityResultLauncher<Intent> invite_friend_start = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == RESULT_OK){
            Intent intent = result.getData();
            assert intent != null;
            String[] friends = intent.getStringArrayExtra("friends");
            String friends_list = String.join(" ", friends);
            friendsText.setText(friends_list);
        }
        if(result.getResultCode() == RESULT_CANCELED){
            Log.e("result error", "받아오기 실패");
        }
    });

    //생성 버튼
    public void btn_create_room(View view){
        Intent intent = new Intent(this, Map.class);
        //TODO 정보를 객체에 담아서 넘기기
        startActivity(intent);
        finish();
    }

    //닫기 버튼
    public void create_room_close(View view) {
        finish();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        timeText = findViewById(R.id.time_Tview);
        if (hourOfDay < 12) {
            timeText.setText("오전 " + hourOfDay + "시 " + minute + "분");
        } else if (hourOfDay == 12) {
            timeText.setText("오후 " + hourOfDay + "시 " + minute + "분");
        } else {
            hourOfDay -= 12;
            timeText.setText("오후 " + hourOfDay + "시 " + minute + "분");
        }
    }
}