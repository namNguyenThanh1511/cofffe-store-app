package namnt.vn.coffestore.network;

import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.data.model.auth.LoginRequest;
import namnt.vn.coffestore.data.model.auth.RegisterRequest;
import namnt.vn.coffestore.data.model.auth.TokenResponse;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Body;

public interface ApiService {
    @POST("api/auth/register")
    Call<ApiResponse<String>> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<ApiResponse<TokenResponse>> login(@Body LoginRequest request);

}
