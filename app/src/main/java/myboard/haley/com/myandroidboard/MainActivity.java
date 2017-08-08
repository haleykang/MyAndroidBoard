package myboard.haley.com.myandroidboard;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    // 1. 변수 생성

    // 1) 로그인 진행 상황(서버 연결)을 알려줄 ProgressDialog 변수
    private ProgressDialog progressDialog;
    // 2) 서버에서 전달한 메세지를 저장할 변수
    private String json;
    // 3) 로그인 성공 시 아이디와 이름 저장할 변수
    public String id;
    public String name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v("MainActivity", "onCreate()");


    }

    // 2. 스레드 -서버에서 데이터 다운 받을 스레드 클래스(내부 클래스)
    // -> 모바일에서 사용자가 입력한 id와 pw를 서버에 전달해 로그인 성공 결과를 가져옴
    class MyThread extends Thread {
        private String id;
        private String pw;

        // 스레드 생성자 - 아이디와 비밀번호 받아오는 생성자
        MyThread(String id, String pw) {
            this.id = id;
            this.pw = pw;
            Log.v("MainActivity", "1");

        }

        // run() 함수 -> 서버와 연결
        @Override
        public void run() {
            Log.v("MainActivity", "2");
            // (1)서버 주소 & 요청 가져오기
            String addr = "http://192.168.0.161:8080/myboard/androidlogin?";
            // (2)아이디와 비밀번호 넘겨주기
            // addr에 특수문자나 한글이 있을 경우
            // URLEncoder.encoder(문자열,"utf-8")을 이용해서 인코딩 후 대입해야함 !!
            addr += "id=" + id;
            addr += "&pw=" + pw;

            StringBuilder html = new StringBuilder();
            try {
                Log.v("MainActivity", "3");
                // (3) URL 생성
                URL url = new URL(addr);
                // (4) 연결
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                Log.v("MainActivity", "4");
                if(con != null) {
                    // 연결 시간 설정
                    con.setConnectTimeout(10000);
                    // 캐시 사용 할 수 없게 설정
                    con.setUseCaches(false);
                    if(con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // 연결 성공
                        Log.v("MainActivity", "5");
                        // 서버 메세지 전달 받기
                        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        while(true) {
                            String line = br.readLine();
                            if(line == null) break;
                            html.append(line + '\n');
                        }
                        br.close();

                        // json 변수에 서버 메세지 저장
                        MainActivity.this.json = html.toString();
                        // 핸들러 실행
                        handler.sendEmptyMessage(0);
                    }
                    // 연결 종료
                    con.disconnect();
                    Log.v("MainActivity", "6");
                }

            } catch(Exception e) {
                Log.e("예외", e.getMessage());
                Log.v("MainActivity", "7");
            }

        }
    }


    // 3. 스레드가 데이터 다운 받은 후, 다운 받은 데이터를 파싱해서 출력할 Handler 객체 생성
    // -> MainActivity 안에
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 서버 연결 후 ProgressDialog 중지
            progressDialog.dismiss();
            Log.v("MainActivity", "8");

            try {
                // 서버에서 받은 메세지가 null아 이닌경우
                if(json != null) {
                    // json이 null이 아닌 경우 - 하기와 같이 값이 넘어옴
                    Log.v("MainActivity", "9");

                    // {"result":{"id":"kjj8032","pw":"1216123","name":"haleykang","image":null}}
                    JSONObject login = new JSONObject(json);
                    JSONObject user = login.getJSONObject("result"); // result 키에 저장된 정보 저장
                    Toast.makeText(MainActivity.this, "login success", Toast.LENGTH_SHORT).show();
                    // 아이디와 비밀번호를 가져와서 저장
                    MainActivity.this.id = user.getString("id");
                    MainActivity.this.name = user.getString("name");

                }
            } catch(Exception e) {
                Log.v("MainActivity", "10");
                // Exception이 발생 -> 로그인 실패 한 경우
                // 서버에서 로그인 실패 시  {"result":null} 을 전달 -> Exception 발생
                // 여기에 로그인 실패 시 실행할 명령문 작성
                Toast.makeText(MainActivity.this, "login fail", Toast.LENGTH_SHORT).show();
                // 아이디 & 비밀번호 초기화
                MainActivity.this.id = "";
                MainActivity.this.name = "";

            }
            Log.v("MainActivity", "11");
            Toast.makeText(MainActivity.this, "id = " + id + ", name = " + name, Toast.LENGTH_SHORT).show();
        }
    };

    // 4. 버튼 클릭 이벤트
    public void onClick(View v) {
        switch(v.getId()) {
            // 로그인 버튼 클릭
            case R.id.login:
                Log.v("MainActivity", "12");
                Context context = getApplicationContext();

                // LayoutInflater XML을 뷰로 변경
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.login, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("로그인");
                builder.setView(layout);

                final EditText inputId = (EditText)layout.findViewById(R.id.id_et);
                final EditText inputPw = (EditText)layout.findViewById(R.id.pw_et);

                builder.setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.v("MainActivity", "13");
                        String id = inputId.getText().toString();
                        String pw = inputPw.getText().toString();

                        // 로그인 처리 되고 있다는 다이얼로그 표시
                        progressDialog = ProgressDialog.show(MainActivity.this, "", "로그인 중...");
                        // 서버에 Http 처리 요청은 새로운 스레드를 생성해 비동기식으로 처리하는 것이 효율적
                        MyThread th = new MyThread(id, pw);
                        th.start(); // 스레드 실행

                    }
                });

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.v("MainActivity", "14");
                        dialogInterface.dismiss();

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

                break;


            // 목록 보기 버튼 클릭
            case R.id.boardlist:
                if(MainActivity.this.id != null && !MainActivity.this.id.equals("")) {
                    // 로그인 상태 O -> BoardListActivity로 이동
                    startActivity(new Intent(MainActivity.this, BoardListActivity.class));

                } else {
                    // 로그인 상태 X
                    Toast.makeText(MainActivity.this, "로그인이 필요한 서비스 입니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }


}
