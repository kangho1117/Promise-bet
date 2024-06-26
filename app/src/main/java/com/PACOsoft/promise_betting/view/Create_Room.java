package com.PACOsoft.promise_betting.view;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.PACOsoft.promise_betting.obj.Promise;
import com.PACOsoft.promise_betting.obj.PromisePlayer;
import com.PACOsoft.promise_betting.obj.User;
import com.PACOsoft.promise_betting.util.Date_Picker;
import com.PACOsoft.promise_betting.R;
import com.PACOsoft.promise_betting.util.Time_Picker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.Contract;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class Create_Room extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    private final String room_validation = "^[a-z가-힇]+[a-z0-9가-힇]{1,10}$";
    private int people;
    private String location_xy;
    TextView timeText, textView, locationText, friendsText, create;
    TextInputLayout lo_roomname;
    TextInputEditText et_roomname;
    private String UID, TAG;
    private ArrayList<String> friendsUID, friendsNick, friendsID;
    private boolean[] check = {false, false, false, false, false}; //0: 방이름, 1: 날짜, 2: 시간, 3: 위치, 4: 친구초대
    private int y, mo, d, h, m; // 시간 받는 값
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private User me;

    //콜백 인터페이스
    public interface MyCallback {
        void onCallback(User user);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);
        Intent intent = getIntent();
        UID = intent.getStringExtra("UID"); //Home에서 intent해준 id를 받아옴
        TAG = "Create_Room";
        locationText = findViewById(R.id.location_Tview);
        friendsText = findViewById(R.id.friends_Tview);
        lo_roomname = findViewById(R.id.lo_room_name);
        et_roomname = findViewById(R.id.et_room_name);
        create = findViewById(R.id.select_create_room);

        readUser(new MyCallback() {
            @Override
            public void onCallback(User user) {
                me = user;
            }
        });


        et_roomname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String nickCheck = et_roomname.getText().toString();
                boolean validation = Pattern.matches(room_validation, nickCheck);
                if (validation) {
                    lo_roomname.setError("");
                    check[0] = true;
                } else if (nickCheck.isEmpty()) {
                    lo_roomname.setError("방 이름을 입력해 주세요.");
                    check[0] = false;
                } else {
                    lo_roomname.setError("방 이름이 올바르지 않습니다.");
                    check[0] = false;
                }
                create_enable();
            }
        });
    }

    //콜백 메소드생성
    public void readUser(MyCallback myCallback) {
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("User").child(UID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                myCallback.onCallback(u);//최강 콜백!!
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Map", String.valueOf(databaseError.toException()));
            }
        });
    }

    //날짜데이터 받아오기
    public void processDatePickerResult(int year, int month, int day) {
        y = year;
        mo = month + 1;
        d = day;
        String month_string = Integer.toString(month + 1);
        String day_string = Integer.toString(day);
        String year_string = Integer.toString(year);
        String dateMessage = (year_string + " " + month_string + " " + day_string);
        textView = findViewById(R.id.date_Tview);
        textView.setText(dateMessage);
        check[1] = true;
        create_enable();
    }

    //날짜 선택 버튼
    public void btn_date_set(View view) {
        DialogFragment newFragment = new Date_Picker();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    //시간데이터 받아오기
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        timeText = findViewById(R.id.time_Tview);
        h = hourOfDay;
        m = minute;
        if (hourOfDay < 12) {
            timeText.setText("오전 " + hourOfDay + "시 " + minute + "분");
        } else if (hourOfDay == 12) {
            timeText.setText("오후 " + hourOfDay + "시 " + minute + "분");
        } else {
            hourOfDay -= 12;
            timeText.setText("오후 " + hourOfDay + "시 " + minute + "분");
        }
        check[2] = true;
        create_enable();
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
        if (result.getResultCode() == RESULT_OK) {
            Intent intent = result.getData();
            assert intent != null;
            String title = intent.getStringExtra("title");
            String category = intent.getStringExtra("category");
            String address = intent.getStringExtra("address");
            String roadAddress = intent.getStringExtra("road");
            String mapx = intent.getStringExtra("x");
            String mapy = intent.getStringExtra("y");
            location_xy = mapx + " " + mapy;
            locationText.setText(title + " " + address);
            check[3] = true;
            create_enable();
        }
        if (result.getResultCode() == RESULT_CANCELED) {
            Log.e("result error", "받아오기 실패");
        }
    });

    //친구 초대 버튼
    public void btn_intent_invite_friend(View view) {
        Intent intent = new Intent(this, Invite_Friend.class);
        intent.putExtra("UID", UID);
        invite_friend_start.launch(intent);
    }

    ActivityResultLauncher<Intent> invite_friend_start = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Intent intent = result.getData();
            assert intent != null;
            friendsUID = intent.getStringArrayListExtra("ArrUID");
            friendsNick = intent.getStringArrayListExtra("ArrNickName");
            friendsID = intent.getStringArrayListExtra("ArrID");
            people = friendsUID.size() + 1;//자기자신 포함
            String friends_list = String.join(" ", friendsNick);
            friendsText.setText(friends_list);
            check[4] = true;
            create_enable();
        }
        if (result.getResultCode() == RESULT_CANCELED) {
            Log.e("result error", "받아오기 실패");
        }
    });

    public void create_enable() {
        if (check[0] && check[1] && check[2] && check[3] && check[4]) {
            create.setEnabled(true);
        } else {
            create.setEnabled(false);
        }
    }

    //시간 검증
    public boolean time_validation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime setTime = LocalDateTime.of(y, mo, d, h, m);

            if (now.isBefore(setTime) && ChronoUnit.MINUTES.between(now, setTime) >= 30) {
                return true;
            } else {
                return false;
            }
        }
        Toast.makeText(getApplicationContext(), "현재 안드로이드의 버전에서는 지원하지 않습니다.", Toast.LENGTH_LONG);
        return false;
    }

    //생성 버튼
    public void btn_create_room(View view) {
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        if (!time_validation()) {
            Toast.makeText(getApplicationContext(), "약속은 30분 이후로만 가능합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        //자신 추가후 객체에 넣기
        friendsUID.add(me.getUID());
        friendsNick.add(me.getNickName());
        ArrayList<PromisePlayer> friendsArray = new ArrayList<PromisePlayer>();
        int i = 0;
        for (String id : friendsUID) {
            PromisePlayer player = new PromisePlayer();
            player.setBettingMoney(0);
            player.setRanking(0);
            player.setArrival(false);
            player.setPlayerUID(id);
            player.setNickName(friendsNick.get(i));
            player.setX(0.10001);
            player.setY(0.10001);
            player.setVoteState(0);
            friendsArray.add(player);
            i++;
        }


        textView = findViewById(R.id.date_Tview);
        timeText = findViewById(R.id.time_Tview);
        et_roomname = findViewById(R.id.et_room_name);
        UUID uuid = UUID.randomUUID();//UUID생성
        String rid = toUnsignedString(uuid.getMostSignificantBits(), 6) + toUnsignedString(uuid.getLeastSignificantBits(), 6);
        Promise promise = new Promise();
        promise.setbettingMoney(0);
        promise.setPromisePlayer(friendsArray);
        promise.setPromiseKey(rid); //고유코드
        promise.setPromiseName(et_roomname.getText().toString());//방이름
        promise.setNumOfPlayer(people);//인원수
        promise.setDate(y + " " + mo + " " + d);//날짜
        promise.setTime(h + " " + m);//시간
        promise.setPromisePlace(location_xy);
        promise.setVote(0);

        databaseReference.child("Promise").child(rid).setValue(promise);
        database = FirebaseDatabase.getInstance();//파이어베이스 데이터베이스 연결
        for (String friendUID : friendsUID) {
            databaseReference = database.getReference("User").child(friendUID).child("promiseKey");//DB테이블 연결, 파이어베이스 콘솔에서 User에 접근
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String pr_key = dataSnapshot.getValue(String.class);

                    if (pr_key.equals("")) {
                        pr_key = rid;
                    } else {
                        pr_key = pr_key + " " + rid;
                    }

                    DatabaseReference mDatabase;
                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("User").child(friendUID).child("promiseKey").setValue(pr_key);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //DB를 가져오는 중에 에러 발생 시 어떤걸 띄울 것인가
                    Log.e(TAG, String.valueOf(databaseError.toException()));//에러문 출력
                }
            });
        }
        Intent intent = new Intent(this, Map.class);
        intent.putExtra("rid", rid);
        intent.putExtra("UID", me.getUID());
        intent.putExtra("Nop", String.valueOf(people));
        startActivity(intent);
        finish();
    }

    //닫기 버튼
    public void create_room_close(View view) {
        finish();
    }

    //UUID 짧게 변환
    @NonNull
    @Contract("_, _ -> new")
    public static String toUnsignedString(long i, int shift) {
        char[] buf = new char[64];
        int charPos = 64;
        int radix = 1 << shift;
        long mask = radix - 1;
        long number = i;
        do {

            buf[--charPos] = digits[(int) (number & mask)];
            number >>>= shift;

        } while (number != 0);
        return new String(buf, charPos, (64 - charPos));
    }


    final static char[] digits = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', '_', '*', '.', '-'
    };
}