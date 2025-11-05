package namnt.vn.coffestore.network;

import namnt.vn.coffestore.data.model.Product;
import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.data.model.auth.LoginRequest;
import namnt.vn.coffestore.data.model.auth.RegisterRequest;
import namnt.vn.coffestore.data.model.auth.TokenResponse;
import namnt.vn.coffestore.data.model.order.OrderRequest;
import namnt.vn.coffestore.data.model.order.OrderResponse;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;

public interface ApiService {

    @POST("api/auth/register")
    Call<ApiResponse<String>> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<ApiResponse<TokenResponse>> login(@Body LoginRequest request);

    @POST("api/auth/logout")
    Call<ApiResponse<String>> logout(@Header("Authorization") String bearerToken);

    @Headers("Content-Type: text/plain")
    @POST("api/auth/refresh-token")
    Call<ApiResponse<TokenResponse>> refreshToken(@Body RequestBody refreshToken);

    @GET("api/products")
    Call<ApiResponse<List<Product>>> getProducts();

    @GET("api/products/{id}")
    Call<ApiResponse<Product>> getProductById(@Path("id") String productId);

    @POST("api/orders")
    Call<ApiResponse<OrderResponse>> createOrder(
        @Header("Authorization") String bearerToken,
        @Body OrderRequest orderRequest
    );

    @GET("api/orders")
    Call<ApiResponse<List<OrderResponse>>> getOrders(
        @Header("Authorization") String bearerToken
    );

    @GET("api/addons")
    Call<ApiResponse<List<namnt.vn.coffestore.data.model.Addon>>> getAddons();

    @GET("api/auth/profile")
    Call<ApiResponse<namnt.vn.coffestore.data.model.UserProfile>> getUserProfile(
        @Header("Authorization") String bearerToken
    );

    // Server đang trả text/plain -> nhận String rồi tự parse
    @GET("api/auth/profile")
    Call<String> getProfile(@Header("Authorization") String bearerToken);
}
