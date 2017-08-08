package myboard.haley.com.myandroidboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;

// 상세보기 클래스

public class DetailActivity extends AppCompatActivity {

    // 1. 변수 생성
    TextView titleTv;
    ImageView imageView;
    TextView contentTv;

    Board board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 4. 이전 클래스에서 받아온 데이터 처리
        // 1) Intent 생성 - 전 클래스에서 전달한 데이터 받아오기
        Intent intent = getIntent();
        // 2) board 객체에 가져온 데이터 저장 -> 시리얼라이즈 된 데이터니까 !
        board = (Board)intent.getSerializableExtra("board");
        // 3) xml 고유 아이디 가져오기
        titleTv = (TextView)findViewById(R.id.detail_title);
        imageView = (ImageView)findViewById(R.id.detail_image);
        contentTv = (TextView)findViewById(R.id.detail_content);
        // 4) 타이틀 / 내용에 값 출력
        titleTv.setText(board.getTitle());
        contentTv.setText(board.getContent());

        // 5) 스레드 시작
        DetailThread th = new DetailThread();
        th.start();

        // 목록으로 버튼 클릭 이벤트
        Button listBt = (Button)findViewById(R.id.list_bt);
        listBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // 2. 핸들러 객체 생성
    Handler mAfterDown = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 이미지를 비트맵 형태로 받아와서 저장
            Bitmap bit = (Bitmap)msg.obj;
            if(bit == null) {
                Toast.makeText(DetailActivity.this, "이미지가 없습니다.", Toast.LENGTH_SHORT).show();
            } else {
                imageView.setImageBitmap(bit);

            }
        }
    };


    // 3. 스레드 - 서버와 연결해서 특정 bno의 image를 다운로드 받는 스레드
    class DetailThread extends Thread {
        @Override
        public void run() {
            try {
                // 1) 업로드된 이미지를 저장하는 폴더의 url 저장 (Board 객체에 저장된 image 주소 가져옴)
                String addr = "http://192.168.0.161:8080/myboard/boardimage/" + board.getImage();

                // 2) openStream()
                InputStream is = new URL(addr).openStream();
                // 3) Bitmap 파일로 가져옴
                Bitmap bit = BitmapFactory.decodeStream(is);
                is.close();
                Message msg = mAfterDown.obtainMessage();
                msg.obj = bit;
                // 핸들러 실행
                mAfterDown.sendMessage(msg);

            } catch(Exception e) {
                Log.e("에러", e.getMessage());
            }


        }
    }


}
