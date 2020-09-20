package kr.core.bowwow.network;

import java.util.ArrayList;

/*
서버 통신 후 결과 데이터 담는 클래스
*/
public class HttpResult {
    private String result = "N";
    private String message = "";
    private ArrayList<?> resultDataList = null;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<?> getResultDataList() {
        return resultDataList;
    }

    public void setResultDataList(ArrayList<?> resultDataList) {
        this.resultDataList = resultDataList;
    }

    public boolean hasDataList() {
        return resultDataList != null && resultDataList.size() > 0;
    }
}