package com.gameservice.sdk.im.token;

import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.gameservice.sdk.im.IMService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * TokenBindTask
 * Description: 绑定token的任务
 */
public class TokenBindTask extends AsyncTask {
    private String mDeviceToken;
    private String mUserId;
    private IMService.TaskCallback mTaskCallback;
    private static final String BIND_COMMAND = "/device/bind";
    private static final String DEVICE_TOKEN = "ng_device_token";
    private static final String TAG = "TokenBindTask";

    public TokenBindTask(String deviceToken, String userId, IMService.TaskCallback taskCallback) {
        mDeviceToken = deviceToken;
        mUserId = userId;
        mTaskCallback = taskCallback;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        if (TextUtils.isEmpty(mDeviceToken)) {
            Log.d(TAG, "deviceToken is empty");
            mTaskCallback.onFailure();
            return null;
        }
        if (TextUtils.isEmpty(mUserId)) {
            mTaskCallback.onFailure();
            Log.d(TAG, "userId is empty");
            return null;
        }
        try {
            bindDeviceToken(mDeviceToken, mUserId);
        } catch (Exception e) {
            //handle error
            e.printStackTrace();
        }
        return null;
    }

    public void bindDeviceToken(String deviceToken, String accessToken) {
        //construct post body json
        URL url = null;
        InputStream is = null;
        OutputStream os = null;
        HttpURLConnection conn = null;
        try {
            url = new URL("http://" + IMService.HOST + BIND_COMMAND);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
                conn.setRequestProperty("Connection", "close");
            }
            conn.setDoInput(true);
            conn.setDoOutput(true);
            //            conn.setChunkedStreamingMode(0);
            byte[] outputInBytes = initContent(deviceToken).getBytes("UTF-8");
            conn.setFixedLengthStreamingMode(outputInBytes.length);
            conn.connect();
            os = conn.getOutputStream();
            os.write(outputInBytes);
            os.flush();
            os.close();
            //do somehting with response
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "token response failure code is:" + responseCode);
                mTaskCallback.onFailure();
            } else {
                Log.d(TAG, "token response success");
                mTaskCallback.onSuccess();
                //读取内容
                /*System.out.println("response success");
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(),"utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                System.out.println(""+sb.toString());*/
            }
        } catch (Exception e) {
            mTaskCallback.onFailure();
            e.printStackTrace();
        } finally {
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != conn) {
                conn.disconnect();
            }
        }
    }

    private String initContent(String deviceToken) {
        if (TextUtils.isEmpty(deviceToken)) {
            throw new IllegalArgumentException("deviceToken is null");
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(DEVICE_TOKEN, deviceToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }
}