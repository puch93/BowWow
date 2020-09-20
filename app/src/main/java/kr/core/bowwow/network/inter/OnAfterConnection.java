package kr.core.bowwow.network.inter;


import kr.core.bowwow.network.HttpResult;

/*
파싱 후 결과값 처리하는 인터페이스
resultCode = 통신 결과 코드
resultData = 결과값, 메세지, 아이템 데이터 리스트가 들어있습니다.
*/
public interface OnAfterConnection {
    void onAfter(int resultCode, HttpResult resultData);
}