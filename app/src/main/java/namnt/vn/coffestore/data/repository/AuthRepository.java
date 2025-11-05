package namnt.vn.coffestore.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;

import namnt.vn.coffestore.data.model.api.ApiError;
import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.data.model.auth.LoginRequest;
import namnt.vn.coffestore.data.model.auth.RegisterRequest;
import namnt.vn.coffestore.data.model.auth.TokenResponse;
import namnt.vn.coffestore.network.ApiService;
import namnt.vn.coffestore.network.RetrofitClient;
import namnt.vn.coffestore.viewmodel.AuthViewModel;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private ApiService apiService;
    private final MutableLiveData<AuthViewModel.AuthResult> authLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthViewModel.AuthResult> logoutLiveData = new MutableLiveData<>();

    private final SharedPreferences prefs;  // Để lưu token

    public AuthRepository(Context context) {
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
    }



    public void register(RegisterRequest request) {
        apiService.register(request).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                ApiResponse<String> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    postResult(authLiveData, true,
                            !TextUtils.isEmpty(body.getMessage()) ? body.getMessage() : "Đăng ký thành công",
                            null);
                } else {
                    postResult(authLiveData, false, extractErrorMessage(response, "Lỗi đăng ký"), null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                postResult(authLiveData, false, "Lỗi kết nối: " + t.getMessage(), null);
            }
        });
    }

    public void login(LoginRequest request) {
        apiService.login(request).enqueue(new Callback<ApiResponse<TokenResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<TokenResponse>> call, Response<ApiResponse<TokenResponse>> response) {
                ApiResponse<TokenResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    TokenResponse token = body.getData();
                    saveTokens(token);
                    postResult(authLiveData, true,
                            !TextUtils.isEmpty(body.getMessage()) ? body.getMessage() : "Đăng nhập thành công",
                            token);
                } else {
                    postResult(authLiveData, false, extractErrorMessage(response, "Lỗi đăng nhập"), null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TokenResponse>> call, Throwable t) {
                postResult(authLiveData, false, "Lỗi kết nối: " + t.getMessage(), null);
            }
        });
    }

    public LiveData<AuthViewModel.AuthResult> getAuthLiveData() {
        return authLiveData;
    }

    public LiveData<AuthViewModel.AuthResult> getLogoutLiveData() {
        return logoutLiveData;
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
        
        // Extract userId from JWT token
        String userId = extractUserIdFromToken(token.getAccessToken());
        if (!TextUtils.isEmpty(userId)) {
            editor.putString("user_id", userId);
            android.util.Log.d("AuthRepository", "Saved userId from token: " + userId);
        }
        
        // Extract role from JWT token
        String roleStr = extractRoleFromToken(token.getAccessToken());
        if (!TextUtils.isEmpty(roleStr)) {
            // Map role string to index: Customer=0, Admin=1, Barista=2
            int roleIdx = 0; // Default to Customer
            String roleLower = roleStr.toLowerCase();
            if (roleLower.contains("admin")) {
                roleIdx = 1;
            } else if (roleLower.contains("barista")) {
                roleIdx = 2;
            }
            editor.putInt("user_role", roleIdx);
            android.util.Log.d("AuthRepository", "Saved role from token: " + roleStr + " -> roleIdx: " + roleIdx);
        }
        
        editor.apply();
    }
    
    /**
     * Extract userId from JWT token payload
     * JWT format: header.payload.signature
     */
    private String extractUserIdFromToken(String token) {
        if (TextUtils.isEmpty(token)) return "";
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return "";
            
            // Decode payload (base64)
            String payload = parts[1];
            // Add padding if needed
            while (payload.length() % 4 != 0) {
                payload += "=";
            }
            
            byte[] decodedBytes = android.util.Base64.decode(payload, android.util.Base64.DEFAULT);
            String payloadJson = new String(decodedBytes, "UTF-8");
            
            // Parse JSON to get userId (try common field names)
            org.json.JSONObject json = new org.json.JSONObject(payloadJson);
            
            // Try different possible field names
            if (json.has("userId")) {
                return json.getString("userId");
            } else if (json.has("user_id")) {
                return json.getString("user_id");
            } else if (json.has("sub")) {
                return json.getString("sub"); // JWT standard subject claim
            } else if (json.has("nameid")) {
                return json.getString("nameid"); // .NET JWT often uses nameid
            } else if (json.has("id")) {
                return json.getString("id");
            }
        } catch (Exception e) {
            android.util.Log.e("AuthRepository", "Failed to extract userId from token: " + e.getMessage());
        }
        return "";
    }
    
    /**
     * Extract role from JWT token payload
     * JWT format: header.payload.signature
     */
    private String extractRoleFromToken(String token) {
        if (TextUtils.isEmpty(token)) return "";
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return "";
            
            // Decode payload (base64)
            String payload = parts[1];
            // Add padding if needed
            while (payload.length() % 4 != 0) {
                payload += "=";
            }
            
            byte[] decodedBytes = android.util.Base64.decode(payload, android.util.Base64.DEFAULT);
            String payloadJson = new String(decodedBytes, "UTF-8");
            
            // Parse JSON to get role (try common field names)
            org.json.JSONObject json = new org.json.JSONObject(payloadJson);
            
            // Try different possible field names
            if (json.has("role")) {
                return json.getString("role");
            } else if (json.has("Role")) {
                return json.getString("Role");
            } else if (json.has("userRole")) {
                return json.getString("userRole");
            } else if (json.has("user_role")) {
                return json.getString("user_role");
            } else if (json.has("http://schemas.microsoft.com/ws/2008/06/identity/claims/role")) {
                // .NET JWT often uses this claim
                Object roleObj = json.get("http://schemas.microsoft.com/ws/2008/06/identity/claims/role");
                if (roleObj instanceof String) {
                    return (String) roleObj;
                } else if (roleObj instanceof org.json.JSONArray) {
                    org.json.JSONArray arr = (org.json.JSONArray) roleObj;
                    if (arr.length() > 0) {
                        return arr.getString(0);
                    }
                }
            }
            
            // Log full payload for debugging
            android.util.Log.d("AuthRepository", "JWT payload: " + payloadJson);
        } catch (Exception e) {
            android.util.Log.e("AuthRepository", "Failed to extract role from token: " + e.getMessage());
        }
        return "";
    }

    // Helper: Lấy access token hiện tại
    public String getAccessToken() {
        return prefs.getString("access_token", "");
    }

    public String getRefreshToken() {
        return prefs.getString("refresh_token", "");
    }

    // Helper: Kiểm tra token còn hạn (tùy chọn)
    public boolean isTokenValid() {
        long expiry = prefs.getLong("access_expiry", 0);
        if (expiry == 0) {
            // Nếu API không trả expiry, coi như token hợp lệ cho tới khi server báo lỗi
            return !TextUtils.isEmpty(getAccessToken());
        }
        return System.currentTimeMillis() < expiry;
    }

    public void logout() {
        String accessToken = getAccessToken();
        if (TextUtils.isEmpty(accessToken)) {
            clearTokens();
            postResult(logoutLiveData, true, "Đã đăng xuất", null);
            return;
        }

        String bearer = "Bearer " + accessToken;

        apiService.logout(bearer).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                clearTokens();

                ApiResponse<String> body = response.body();
                boolean success = response.isSuccessful() && body != null && body.isSuccess();
                if (success) {
                    postResult(logoutLiveData, true,
                            !TextUtils.isEmpty(body.getMessage()) ? body.getMessage() : "Đăng xuất thành công",
                            null);
                } else {
                    postResult(logoutLiveData, false,
                            extractErrorMessage(response, "Đăng xuất thất bại"),
                            null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                clearTokens();
                postResult(logoutLiveData, false, "Lỗi kết nối: " + t.getMessage(), null);
            }
        });
    }

    public void refreshToken() {
        String refreshToken = getRefreshToken();
        if (TextUtils.isEmpty(refreshToken)) {
            postResult(authLiveData, false, "Không có refresh token", null);
            return;
        }

        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), refreshToken);
        apiService.refreshToken(body).enqueue(new Callback<ApiResponse<TokenResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<TokenResponse>> call, Response<ApiResponse<TokenResponse>> response) {
                ApiResponse<TokenResponse> apiResponse = response.body();
                if (response.isSuccessful() && apiResponse != null && apiResponse.isSuccess() && apiResponse.getData() != null) {
                    TokenResponse token = apiResponse.getData();
                    saveTokens(token);
                    postResult(authLiveData, true,
                            !TextUtils.isEmpty(apiResponse.getMessage()) ? apiResponse.getMessage() : "Làm mới token thành công",
                            token);
                } else {
                    postResult(authLiveData, false,
                            extractErrorMessage(response, "Làm mới token thất bại"),
                            null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TokenResponse>> call, Throwable t) {
                postResult(authLiveData, false, "Lỗi kết nối: " + t.getMessage(), null);
            }
        });
    }

    private void clearTokens() {
        prefs.edit()
                .remove("access_token")
                .remove("refresh_token")
                .remove("access_expiry")
                .remove("refresh_expiry")
                .apply();
    }

    private String extractErrorMessage(Response<?> response, String defaultMessage) {
        String fallback = defaultMessage;
        ApiResponse<?> errorBody = null;
        if (response.body() instanceof ApiResponse) {
            errorBody = (ApiResponse<?>) response.body();
        }

        if (errorBody != null) {
            if (!TextUtils.isEmpty(errorBody.getMessage())) {
                return errorBody.getMessage();
            }

            if (errorBody.getErrors() != null && !errorBody.getErrors().isEmpty()) {
                ApiError first = errorBody.getErrors().get(0);
                if (first != null && !TextUtils.isEmpty(first.getMessage())) {
                    return first.getMessage();
                }
            }
        }

        ResponseBody rawError = response.errorBody();
        if (rawError != null) {
            try {
                String raw = rawError.string();
                if (!TextUtils.isEmpty(raw)) {
                    return raw;
                }
            } catch (IOException ignored) {
                // no-op
            }
        }

        return fallback;
    }

    private void postResult(MutableLiveData<AuthViewModel.AuthResult> liveData,
                             boolean success,
                             String message,
                             TokenResponse tokenResponse) {
        liveData.postValue(new AuthViewModel.AuthResult() {
            @Override
            public boolean isSuccess() {
                return success;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public TokenResponse getTokenResponse() {
                return tokenResponse;
            }
        });
    }
}
