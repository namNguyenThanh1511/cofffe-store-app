package namnt.vn.coffestore.network;

import namnt.vn.coffestore.utils.Constants;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import okhttp3.OkHttpClient;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Custom TrustManager để trust all certs (DEV ONLY - bỏ qua SSL cho localhost/self-signed)
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            // FIX: Trả mảng rỗng thay vì null để tránh NPE trong OkHttp
                            return new X509Certificate[0];
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                            // Bỏ qua check client
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                            // Bỏ qua check server
                        }
                    }
            };

            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new SecureRandom());
                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                // Bỏ qua hostname verification (cho localhost)
                                return true;
                            }
                        })
                        .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))  // Log API calls để debug
                        .build();

                retrofit = new Retrofit.Builder()
                        .baseUrl(Constants.BASE_URL)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            } catch (Exception e) {
                // Log lỗi nếu có (ví dụ: SSLContext init fail)
                e.printStackTrace();
            }
        }
        return retrofit;
    }
}
