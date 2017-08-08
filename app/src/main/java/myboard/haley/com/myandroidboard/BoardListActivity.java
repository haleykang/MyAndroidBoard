package myboard.haley.com.myandroidboard;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// 목록 보기 클래스

public class BoardListActivity extends AppCompatActivity {

    // 1. 필요한 인스턴스 변수 선언

    // 1) 서버에서 가져온 데이터를 저장할 List 객체
    List<Board> list;
    // 2) ListView
    ListView listView;
    // 3) ArrayAdapter
    ArrayAdapter<String> adapter;
    // 4) 서버에서 가져온 데이터 중 화면에 출력할 값만 저장하는 ArrayList
    List<String> arrayList = new ArrayList<>();
    // 5) 진행상황을 알려줄 progress dialog
    private ProgressDialog progressDialog;
    // 6) 서버에서 다운로드 받은 문자열을 저장할 변수
    private String json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_list);

        // 4. 실행
        // 1) 리스트 객체 생성
        list = new ArrayList<>();
        // 2) 어댑터에 arrayList(다운로드한 데이터 중 출력할 데이터 저장한 리스트) 추가
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        // 3) ListView와 어댑터 연결
        listView = (ListView)findViewById(R.id.board_listview);
        listView.setAdapter(adapter);
        // 4) ListView 설정 추가
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setDivider(new ColorDrawable(Color.RED));
        listView.setDividerHeight(2);

        // 5) progressDialog 설정
        progressDialog = ProgressDialog.show(BoardListActivity.this, "", "서버와 연결 중...");

        // 6) 스레드 실행
        // -> 서버에 HTTP 처리 요청은 새로운 스레드를 생성하여 비동기식으로 처리하는것이 효율적
        BoardThread th = new BoardThread();
        th.start();


        // 6) 메인으로 버튼 클릭 이벤트
        Button mainBt = (Button)findViewById(R.id.main_bt);
        mainBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // BoardListActivity 종료
                finish();
            }
        });


        // ListView에서 항목을 클릭 했을 때 실행할 이벤트 설정
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
                // 첫 번째 매개변수(adapterView)는 이벤트가 발생한 리스트 뷰
                // 두 번째 매개변수(view)는 이벤트가 발생한 항목 뷰
                // 세 번째 매개변수(index)는 이벤트가 발생한 인덱스(=포지션)
                // 네 번째 매개변수(id)는 이벤트가 발생한 항목 뷰의 아이디

                // 1) Board 클래스 객체에 list에서 선택한 index의 데이터를 저장
                Board board = list.get(index);
                // 2) 저장된 값을 intent로 전달 후 상세보기 화면으로 이동
                // -> 이 때 Board 클래슨 Serializable 된 상태여야 함 !!!
                Intent intent = new Intent(BoardListActivity.this, DetailActivity.class);
                intent.putExtra("board", board);
                startActivity(intent);
            }
        });


    } // end of onCreate()

    // 2. JSON 파싱을 수행하고 ListView의 화면을 갱신해 줄 핸들러 클래스의 객체를 생성
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            try {
                if(json != null) {
                    // 1) 첫 번째 {} Object 생성 - 서버에서 받아온 json 데이터 입력
                    JSONObject data = new JSONObject(json);
                    // 2) 두 번째 [] Array 생성 - 받아온 json 데이터에서 result 키에 저장된 배열 가져오기
                    JSONArray result = data.getJSONArray("result");
                    // 3) 반복문을 통해 result 배열에 저장된 {} Object 가져와서 VO 클래스에 저장
                    for(int i = 0; i < result.length(); ++i) {
                        // (1) result 배열에서 i 위치에 있는 {} Object 가져와서 저장
                        JSONObject temp = result.getJSONObject(i);
                        // (2) 가져온 정보를 저장한 Board 객체 생성
                        Board board = new Board();
                        // (3) 가져온 각각의 값을 저장
                        board.setBno(temp.getInt("bno"));
                        board.setTitle(temp.getString("title"));
                        board.setContent(temp.getString("content"));
                        board.setId(temp.getString("id"));
                        board.setImage(temp.getString("image"));
                        // (4) list에 저장
                        list.add(board);
                        // (5) 목록 보기 화면에 출력할 값을 arrayList에 저장
                        arrayList.add(temp.getString("title"));

                    }
                    // 4) 리스트 어댑터에 변화 내용 있음을 알림
                    adapter.notifyDataSetChanged();
                }

            } catch(Exception e) {
                // 예외 상황 발생 -> 다운로드 받은 데이터가 없음
                Toast.makeText(BoardListActivity.this, "게시글이 없습니다.", Toast.LENGTH_SHORT).show();

            }

        }
    }; // end of Handler


    // 3. 서버에서 데이터를 다운로드 받을 스레드 클래스 생성
    class BoardThread extends Thread {
        @Override
        public void run() {
            // 1) 서버의 주소 & 요청 주소 저장
            String addr = "http://192.168.0.161:8080/myboard/androidlist";

            // 2) 서버에서 다운로드 받은 데이터를 한 줄 한 줄 읽어온 값을 저장할 StringBuilder 객체 생성
            // -> 이 작업은 꼭 StringBuilder로!! (String 사용시 메모리 엄청 사용됨)
            StringBuilder html = new StringBuilder();
            try {
                // 3) 서버 요청 주소에 접속
                URL url = new URL(addr);
                // 4) 서버 연결 요청
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                if(con != null) {
                    // (1) 연결 시간 설정
                    con.setConnectTimeout(10000);
                    // (2) 캐시에서 값 가져오지 않도로
                    con.setUseCaches(false);
                    // 5) 서버 연결 성공 시
                    if(con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // (1) 서버에서 전달한 값을 읽어올 BufferedReader 객체 생성
                        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        // (2) while 문으로 값 읽어오기 (한 줄씩 읽고 나서 다음 줄이 없으면 false 반환)
                        while(true) {
                            String templine = br.readLine();
                            if(templine == null) break;
                            html.append(templine + "\n");
                        }
                        // (3) BuffredReader close
                        br.close();
                        // (4) json 변수에 읽어온 값 저장
                        json = html.toString();
                        // (5) 핸들러 실행
                        handler.sendEmptyMessage(0);

                    }
                    // 6) 연결 끊기
                    con.disconnect();

                }

            } catch(Exception e) {
                Log.e("에러", e.getMessage());
            }
        }
    }


}
