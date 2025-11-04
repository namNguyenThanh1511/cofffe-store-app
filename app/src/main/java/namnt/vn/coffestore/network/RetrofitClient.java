package namnt.vn.coffestore.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import namnt.vn.coffestore.utils.Constants;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {

            // DEV ONLY - lược bỏ verify SSL cho localhost/self-signed
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                        @Override public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                        @Override public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    }
            };

            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new SecureRandom());

                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY); // log đầy đủ

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override public boolean verify(String hostname, SSLSession session) { return true; }
                        })
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .addInterceptor(logging)
                        .build();

                Gson gson = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                        .create();

                retrofit = new Retrofit.Builder()
                        .baseUrl(Constants.BASE_URL) // http://10.0.2.2:7000/
                        .client(okHttpClient)
                        .addConverterFactory(ScalarsConverterFactory.create())   // nhận text/plain
                        .addConverterFactory(GsonConverterFactory.create(gson))  // parse JSON khi có application/json
                        .build();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return retrofit;
    }
}
