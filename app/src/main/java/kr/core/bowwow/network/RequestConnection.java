package kr.core.bowwow.network;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kr.core.bowwow.network.inter.OnAfterConnection;
import kr.core.bowwow.network.inter.OnParsingResult;
import kr.core.bowwow.utils.MyUtil;


//실제 서버 통신하는 클래스
public class RequestConnection {
    private int _RESULT_CODE = HttpURLConnection.HTTP_OK;
    private final String _CHARSET = "UTF-8";
    private final String _BOUNDARY = "boundary";
    private final String _CRLF = "\r\n";

    //통신할 url
    String _URL;
    //일반 파라미터 (HashMap 대신 ContentValue로 대체 가능)
    HashMap<String, String> params;

    HashMap<String, String[]> arrParams;

    //파일 파라미터
    /*HashMap<String, File> fileParams;*/
    ArrayList<FileParam> fileParams;

    class FileParam {
        String key;
        File file;

        public FileParam(String key, File file){
            this.key = key;
            this.file = file;
        }

//        public String getKey() {
//            return key;
//        }
//
//        public File getFile() {
//            return file;
//        }
    }

    OnParsingResult onParsingResult;
    OnAfterConnection onAfter;

    public RequestConnection(String url) {
        this. _URL = url;
        this.params = new HashMap<String, String>();

        this.arrParams = new HashMap<String, String[]>();
//        this.fileParams = new HashMap<String, File>();

        this.fileParams = new ArrayList<FileParam>();
    }

    //서버 통신해서 결과값 받아오는 메소드
    public String request(String url) {
//        HttpURLConnection connection = null;
        HttpURLConnection connection = null;

        String resultString = null;

        try{
            //create connection
            URL requestURL = new URL(url);
            connection = (HttpURLConnection) requestURL.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
//            connection.setConnectTimeout(10000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
//            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + _BOUNDARY);
//            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + _BOUNDARY);


            OutputStream os = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, _CHARSET), true);
            //일반 파라미터 입력
            if (!params.isEmpty()) {
                setQuery(writer, params);
//                setNormalQuery(writer, params);
            }

            if (!arrParams.isEmpty()) {
                setArrQuery(writer, arrParams);
            }
            //파일 파라미터 입력
            if (fileParams.size() > 0) {
                setFileQuery(writer, os, fileParams);
            }
            writer.close();
            os.close();

            //서버와 연결 시도
            _RESULT_CODE = connection.getResponseCode();
            if(_RESULT_CODE != HttpURLConnection.HTTP_OK){
                //연결 실패
                resultString = null;
            }

            //연결 성공, 결과 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), _CHARSET));

            String line;
            String totalResultText = "";

            while((line=reader.readLine()) != null){
                totalResultText += line;
            }
            resultString = totalResultText.toString();
            Log.i(MyUtil.TAG,"resultString: "+resultString);

        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            if(connection != null){
                connection.disconnect();
            }
        }

        //결과값을 반환한다
        return resultString;
    }

    private void setQuery(PrintWriter writer, HashMap<String, String> parameters) {
        StringBuilder result = new StringBuilder();
        boolean first = true; // 첫 번째 매개변수 여부

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (first)
                first = false;
            else // 첫 번째 매개변수가 아닌 경우엔 앞에 &를 붙임
                writer.append("&");
            try { // UTF-8로 주소에 키와 값을 붙임
                writer.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                writer.append("=");
                if(entry.getValue().contains("%")){
                    writer.append(entry.getValue());
                }else {
                    writer.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
            } catch (UnsupportedEncodingException ue) {
                ue.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setNormalQuery(PrintWriter writer, HashMap<String, String> parameters) {
        for(String key : parameters.keySet()){
            writer.append("--" + _BOUNDARY).append(_CRLF);
            writer.append("Content-Disposition: form-data; name=\"" + key + "\"").append(_CRLF);
            writer.append("Content-Type: text/plain; charset=" + _CHARSET).append(_CRLF);
            writer.append(_CRLF);
            writer.append(String.valueOf(parameters.get(key))).append(_CRLF);
            writer.append("--" + _BOUNDARY).append(_CRLF);
            writer.flush();
        }
    }

    private void setArrQuery(PrintWriter writer, HashMap<String, String[]> parameters) {
        for(String key : parameters.keySet()){
            for(int i = 0; i < parameters.get(key).length; i++) {
                writer.append("--" + _BOUNDARY).append(_CRLF);
                writer.append("Content-Disposition: form-data; name=\"" + key + "\"").append(_CRLF);
                writer.append("Content-Type: text/plain; charset=" + _CHARSET).append(_CRLF);
                writer.append(_CRLF);
                writer.append(String.valueOf(parameters.get(key)[i])).append(_CRLF);
                writer.append("--" + _BOUNDARY).append(_CRLF);
                writer.flush();
            }
        }
    }

    private void setFileQuery(PrintWriter writer, OutputStream os, ArrayList<FileParam> fileParams) {
//        for(String key : fileParams.keySet()){

          for(int i = 0; i < fileParams.size(); i++){
            String key = fileParams.get(i).key;
            File file = fileParams.get(i).file;
            String fileName = file.getName();
            writer.append("--" + _BOUNDARY).append(_CRLF);
            writer.append("Content-Disposition: form-data; name=\""+key+"\"; filename=\"" + file.getName()+ "\"").append(_CRLF);
//            writer.append("Content-Type: text/plain; charset=" + _CHARSET).append(_CRLF);
//            writer.append("Content-Type: multipart/form-data;").append(_CRLF);
            writer.append("Content-Type: "+URLConnection.guessContentTypeFromName(fileName)).append(_CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(_CRLF);
            writer.append(_CRLF);
            writer.flush();

            try{
                FileInputStream inputStream = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
                inputStream.close();

                writer.append(_CRLF);
                writer.append("--" + _BOUNDARY).append(_CRLF);
                writer.flush();

            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void execute(boolean createNewThread) {
        if(createNewThread){
            final AsyncTask<Void, Void, String> request = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    return request(_URL);
                }

                @Override
                protected void onPostExecute(String result) {
                    onPostExecuteAction(result);
                }
            };
            request.execute();
        }else{
            onPostExecuteAction(request(_URL));
        }
    }

    private void onPostExecuteAction(String result) {
        if(onParsingResult != null){
            HttpResult resultData = onParsingResult.onParse(result);
            if(onAfter != null){
                onAfter.onAfter(_RESULT_CODE, resultData);
            }
        }
    }

    //결과 코드 getter
    public int getResultCode() {
        return _RESULT_CODE;
    }

    public void setOnParsingResult(OnParsingResult onParsingResult) {
        this.onParsingResult = onParsingResult;
    }

    public void setOnAfter(OnAfterConnection onAfter) {
        this.onAfter = onAfter;
    }

    //일반 파라미터 넣기
    public void addParams(String key, String value){
        params.put(key, value);
    }

    public void addArrParams(String key, String[] value){
        arrParams.put(key,value);
    }

    //파일 파라미터 넣기
    public void addFileParams(String key, File file){
//        fileParams.put(key, file);
        fileParams.add(new FileParam(key,file));
    }


    public void removeParams(String key){
        params.remove(key);
    }
}
