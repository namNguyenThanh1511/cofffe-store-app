package namnt.vn.coffestore.data.repository;

import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.data.model.auth.UserProfile;
import namnt.vn.coffestore.network.ApiService;
import namnt.vn.coffestore.network.RetrofitClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private static final String TAG = "ProfileAPI";

    private final ApiService apiService;
    private final Gson gson = new Gson();

    public static class ProfileResult {
        public final boolean success;
        public final String message;
        public final UserProfile data;

        public ProfileResult(boolean success, String message, UserProfile data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
    }

    public UserRepository() {
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
    }

    public void fetchProfile(String bearerToken, MutableLiveData<ProfileResult> liveData) {
        Log.d(TAG, "Calling /api/auth/profile with header: " + bearerToken.substring(0, Math.min(bearerToken.length(), 20)) + "...");
        apiService.getProfile(bearerToken).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d(TAG, "HTTP " + response.code());
                Log.d(TAG, "Headers:\n" + response.headers());

                if (!response.isSuccessful()) {
                    String msg;
                    if (response.code() == 401) {
                        msg = "401 Unauthorized – Token hết hạn/không hợp lệ. Hãy đăng xuất và đăng nhập lại.";
                    } else if (response.code() == 403) {
                        msg = "403 Forbidden – Tài khoản không đủ quyền truy cập.";
                    } else {
                        msg = extractErrorMessage(response, "Lấy hồ sơ thất bại");
                    }
                    Log.e(TAG, "onResponse !isSuccessful -> " + msg);
                    liveData.postValue(new ProfileResult(false, msg, null));
                    return;
                }

                String raw = response.body();
                Log.d(TAG, "Raw body:\n" + raw);

                if (raw == null || raw.isEmpty()) {
                    liveData.postValue(new ProfileResult(false, "Phản hồi rỗng từ server", null));
                    return;
                }

                try {
                    ApiResponse<UserProfile> body = gson.fromJson(
                            raw, new TypeToken<ApiResponse<UserProfile>>(){}.getType()
                    );
                    if (body != null && body.isSuccess() && body.getData() != null) {
                        String message = !TextUtils.isEmpty(body.getMessage()) ? body.getMessage() : "Lấy hồ sơ thành công";
                        liveData.postValue(new ProfileResult(true, message, body.getData()));
                    } else {
                        String message = (body != null && !TextUtils.isEmpty(body.getMessage()))
                                ? body.getMessage() : "Lấy hồ sơ thất bại";
                        Log.e(TAG, "Parsed JSON but not success. message=" + message);
                        liveData.postValue(new ProfileResult(false, message, null));
                    }
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "JSON parse error: " + e.getMessage(), e);
                    liveData.postValue(new ProfileResult(false, "Lỗi parse JSON: " + e.getMessage(), null));
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage(), t);
                liveData.postValue(new ProfileResult(false, "Lỗi kết nối: " + t.getMessage(), null));
            }
        });
    }

    private String extractErrorMessage(Response<?> response, String defaultMessage) {
        ResponseBody raw = response.errorBody();
        if (raw != null) {
            try {
                String rawStr = raw.string();
                if (!TextUtils.isEmpty(rawStr)) return rawStr;
            } catch (IOException ignored) {}
        }
        return defaultMessage;
    }
}
