package kr.core.bowwow.network;

import android.content.Context;
import android.util.Log;

import kr.core.bowwow.utils.MyUtil;
import kr.core.bowwow.utils.StringUtil;

public abstract class ReqBasic extends BaseReq {
    public ReqBasic(Context context, String url) {
        super(context, url);
    }

    @Override
    public HttpResult onParse(String jsonString) {

        HttpResult res = new HttpResult();
        if (MyUtil.isNull(jsonString)) {
            res.setResult(null);
            Log.e(StringUtil.TAG, " Get Info: null");
        } else {
            res.setResult(jsonString);
            Log.e(StringUtil.TAG, " Get Info: " + jsonString);
        }
        return res;
    }
}
