package myboard.haley.com.myandroidboard;

import java.io.Serializable;
import java.sql.Date;

/**
 * 서버에서 가져온 게시글 정보를 저장할 VO 클래스
 */

public class Board implements Serializable {

    // 1. 테이블 컬럼 이름과 동일한 인스턴스 변수 선언
    private int bno;
    private String title;
    private String content;
    private String id;
    private Date regdate;
    private int readcnt;
    private String ip;
    private String image;


    // 2. get, set 함수
    public int getBno() {
        return bno;
    }

    public void setBno(int bno) {
        this.bno = bno;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getRegdate() {
        return regdate;
    }

    public void setRegdate(Date regdate) {
        this.regdate = regdate;
    }

    public int getReadcnt() {
        return readcnt;
    }

    public void setReadcnt(int readcnt) {
        this.readcnt = readcnt;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
