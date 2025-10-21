package namnt.vn.coffestore.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.data.model.auth.LoginRequest;
import namnt.vn.coffestore.data.model.auth.RegisterRequest;
import namnt.vn.coffestore.data.model.auth.TokenResponse;
import namnt.vn.coffestore.network.ApiService;
import namnt.vn.coffestore.network.RetrofitClient;
import namnt.vn.coffestore.viewmodel.AuthViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private ApiService apiService;
    private MutableLiveData<AuthViewModel.AuthResult> authLiveData = new MutableLiveData<>();

    private SharedPreferences prefs;  // Để lưu token

    public AuthRepository(Context context) {
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
    }



    public void register(RegisterRequest request) {
        apiService.register(request).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body().isSuccess()) {
                    authLiveData.setValue(new AuthViewModel.AuthResult() {
                        @Override public boolean isSuccess() { return true; }  // Or false
                        @Override public String getMessage() { return "Message here"; }

                        @Override
                        public TokenResponse getTokenResponse() {
                            return null;
                        }
                    });
                } else {
                    String errorMsg;
                    if (response.body() != null && response.body().getErrors() != null) {
                        errorMsg = response.body().getErrors().get(0).getMessage();  // Lấy lỗi đầu tiên (validation)
                    } else {
                        errorMsg = "Lỗi đăng ký";
                    }
                    authLiveData.setValue(new AuthViewModel.AuthResult() {
                        @Override public TokenResponse getTokenResponse() { return null; }
                        @Override public String getMessage() { return errorMsg; }
                        @Override public boolean isSuccess() { return false; }
                    });
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                authLiveData.setValue(new AuthViewModel.AuthResult() {
                    @Override public TokenResponse getTokenResponse() { return null; }
                    @Override public String getMessage() { return "Lỗi kết nối: " + t.getMessage(); }
                    @Override public boolean isSuccess() { return false; }
                });
            }
        });
    }

    public void login(LoginRequest request) {
        apiService.login(request).enqueue(new Callback<ApiResponse<TokenResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<TokenResponse>> call, Response<ApiResponse<TokenResponse>> response) {
                if (response.isSuccessful() && response.body().isSuccess()) {
                    TokenResponse token = response.body().getData();
                    saveTokens(token);
                    authLiveData.setValue(new AuthViewModel.AuthResult() {
                        @Override public TokenResponse getTokenResponse() { return token; }
                        @Override public String getMessage() { return response.body().getMessage() != null ? response.body().getMessage() : "Đăng nhập thành công"; }
                        @Override public boolean isSuccess() { return true; }
                    });
                } else {
                    String errorMsg;
                    if (response.body() != null && response.body().getErrors() != null) {
                        errorMsg = response.body().getErrors().get(0).getMessage();
                    } else {
                        errorMsg = "Lỗi đăng nhập";
                    }
                    authLiveData.setValue(new AuthViewModel.AuthResult() {
                        @Override public TokenResponse getTokenResponse() { return null; }
                        @Override public String getMessage() { return errorMsg; }
                        @Override public boolean isSuccess() { return false; }
                    });
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TokenResponse>> call, Throwable t) {
                authLiveData.setValue(new AuthViewModel.AuthResult() {
                    @Override public TokenResponse getTokenResponse() { return null; }
                    @Override public String getMessage() { return "Lỗi kết nối: " + t.getMessage(); }
                    @Override public boolean isSuccess() { return false; }
                });
            }
        });
    }

    public LiveData<AuthViewModel.AuthResult> getAuthLiveData() {
        return authLiveData;
    }

    private void saveTokens(TokenResponse token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("access_token", token.getAccessToken());
        editor.putString("refresh_token", token.getRefreshToken());
        // Lưu expiry dưới dạng timestamp (long)
        if (token.getAccessTokenExpiry() != null) {
            editor.putLong("access_expiry", token.getAccessTokenExpiry().getTime());
        }
        if (token.getRefreshTokenExpiry() != null) {
            editor.putLong("refresh_expiry", token.getRefreshTokenExpiry().getTime());
        }
        editor.apply();
    }

    // Helper: Lấy access token hiện tại
    public String getAccessToken() {
        return prefs.getString("access_token", "");
    }

    // Helper: Kiểm tra token còn hạn (tùy chọn)
    public boolean isTokenValid() {
        long expiry = prefs.getLong("access_expiry", 0);
        return System.currentTimeMillis() < expiry;
    }
}
