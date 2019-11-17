package ako.fit.project.api;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;

public class MLServer {

  private static final String TAG = "MLSERVER";
  public static final String SERVER = "https://192.168.1.10:5001";
  private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

  public static OkHttpClient getClient(){
    return
      HttpUtil.getUnsafeOkHttpClient()
      .connectionSpecs(Collections.singletonList(getSpecs()))
      .connectTimeout(60, TimeUnit.HOURS)
      .writeTimeout(60, TimeUnit.HOURS)
      .readTimeout(60, TimeUnit.HOURS)
      .build();
  }

  public static void SendImage(SendImageCallback callback){
    try {

      OkHttpClient client = getClient();

      RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
        .addFormDataPart("file", "a.jpg", RequestBody.create(MEDIA_TYPE_PNG, new File(callback.fileLocation)))
        .build();

      Request request = new Request.Builder().url( SERVER +  "/api/ml/")
        .post(requestBody).build();

      Log.d("API", "before send");
      client.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
          Log.d(TAG, e.toString());
          e.printStackTrace();
          callback.onError(e.toString());
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
          String responseTxt = response.body().string();
          Log.d(TAG, "response:: " + responseTxt);
          try {
            JSONObject obj = new JSONObject(responseTxt);

            ApiResponse apiResponse= new ApiResponse();
            apiResponse.IsWinston = (boolean)obj.get("isWinston");
            apiResponse.Header = obj.getString("header");
            apiResponse.SubHeader = obj.getString("subHeader");
            apiResponse.WinstonVj = obj.getString("winstonVj");
            apiResponse.UpaljacVj = obj.getString("upaljacVj");
            apiResponse.OstaloVj = obj.getString("ostaloVj");

            callback.onSuccess(apiResponse);
          } catch (JSONException e) {
            e.printStackTrace();
            callback.onError(e.toString());
          }
        }
      });


    }
    catch (Exception e){
      e.printStackTrace();
      callback.onError(e.toString());
    }
  }

  private static ConnectionSpec getSpecs(){
    return new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
      .supportsTlsExtensions(true)
      .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
      .cipherSuites(
        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
        CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
        CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
        CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
        CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
        CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA)
      .build();
  }

  public static void getImages(GetImagesCallback callback){
    try{

      OkHttpClient client = getClient();
      Request request = new Request.Builder().url( SERVER +  "/api/images").build();
      client.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
          Log.d(TAG, e.toString());
          e.printStackTrace();
          callback.onError(e.toString());
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
          String responseTxt = response.body().string();
          Log.d(TAG, "response:: " + responseTxt);
          try {
            List<String> result = new ArrayList<>();
            JSONArray arry = new JSONArray(responseTxt);
            for(int i = 0; i < arry.length(); i++)
              result.add(arry.getString(i));
            callback.onSuccess(result);

          } catch (JSONException e) {
            e.printStackTrace();
            callback.onError(e.toString());
          }
        }
      });
    }
    catch (Exception e){

    }
  }

}
