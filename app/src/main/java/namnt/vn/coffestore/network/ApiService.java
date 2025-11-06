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
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import java.util.List;

import namnt.vn.coffestore.data.model.chat.ChatConversation;
import namnt.vn.coffestore.data.model.chat.ChatMessage;
import namnt.vn.coffestore.data.model.chat.SendMessageRequest;
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

    // Chat APIs
    @POST("api/chats/conversation/customer")
    Call<ApiResponse<ChatConversation>> createConversationForCustomer(@Header("Authorization") String bearer);

    @GET("api/chats/user/{userId}")
    Call<ApiResponse<List<ChatConversation>>> getUserConversations(@Header("Authorization") String bearer,
                                                                   @Path("userId") String userId);

    @GET("api/chats/{conversationId}/messages")
    Call<ApiResponse<List<ChatMessage>>> getConversationMessages(@Header("Authorization") String bearer,
                                                                 @Path("conversationId") long conversationId);

    @POST("api/chats/send")
    Call<ApiResponse<ChatMessage>> sendMessage(@Header("Authorization") String bearer,
                                               @Body SendMessageRequest request);
    @GET("api/products")
    Call<ApiResponse<List<Product>>> getProducts();
    
    @GET("api/products/{id}")
    Call<ApiResponse<Product>> getProductById(@Path("id") String productId);
    
    @POST("api/orders")
    Call<ApiResponse<OrderResponse>> createOrder(
        @Header("Authorization") String bearerToken,
        @Body OrderRequest orderRequest
    );
    
    // Customer orders (with optional filters)
    @GET("api/orders")
    Call<ApiResponse<List<OrderResponse>>> getOrders(
        @Header("Authorization") String bearerToken,
        @Query("Search") String search,
        @Query("SortBy") String sortBy,
        @Query("SortOrder") String sortOrder,
        @Query("Field") String field,
        @Query("Statuses") List<String> statuses,
        @Query("DeliveryTypes") List<String> deliveryTypes,
        @Query("PaymentStatuses") List<String> paymentStatuses,
        @Query("SelectFields") List<String> selectFields,
        @Query("PageNumber") Integer pageNumber,
        @Query("PageSize") Integer pageSize
    );

    // Admin/Barista orders (with optional filters)
    @GET("api/orders/admin-barista")
    Call<ApiResponse<List<OrderResponse>>> getOrdersForAdminBarista(
        @Header("Authorization") String bearerToken,
        @Query("Search") String search,
        @Query("SortBy") String sortBy,
        @Query("SortOrder") String sortOrder,
        @Query("Field") String field,
        @Query("Statuses") List<String> statuses,
        @Query("DeliveryTypes") List<String> deliveryTypes,
        @Query("PaymentStatuses") List<String> paymentStatuses,
        @Query("SelectFields") List<String> selectFields,
        @Query("PageNumber") Integer pageNumber,
        @Query("PageSize") Integer pageSize
    );
    
    // Get order by ID (Customer)
    @GET("api/orders/{id}")
    Call<ApiResponse<OrderResponse>> getOrderById(
        @Header("Authorization") String bearerToken,
        @Path("id") String orderId
    );
    
    // Get order by ID (Admin/Barista)
    @GET("api/orders/admin-barista/{id}")
    Call<ApiResponse<OrderResponse>> getOrderByIdForAdminBarista(
        @Header("Authorization") String bearerToken,
        @Path("id") String orderId
    );

    // Pay order
    @POST("api/orders/paying")
    Call<ApiResponse<String>> payOrder(
        @Header("Authorization") String bearerToken,
        @Body namnt.vn.coffestore.data.model.order.PayOrderRequest request
    );

    // Update order status
    @PUT("api/orders/status")
    Call<ApiResponse<OrderResponse>> updateOrderStatus(
        @Header("Authorization") String bearerToken,
        @Body namnt.vn.coffestore.data.model.order.UpdateOrderStatusRequest request
    );
    
    // TODO: Add separate API endpoints for admin/barista when available
    // @GET("api/admin/orders")
    // Call<ApiResponse<List<OrderResponse>>> getAdminOrders(
    //     @Header("Authorization") String bearerToken
    // );
    // 
    // @GET("api/barista/orders")
    // Call<ApiResponse<List<OrderResponse>>> getBaristaOrders(
    //     @Header("Authorization") String bearerToken
    // );
    
    @GET("api/addons")
    Call<ApiResponse<List<namnt.vn.coffestore.data.model.Addon>>> getAddons();
    
    @GET("api/auth/profile")
    Call<ApiResponse<namnt.vn.coffestore.data.model.UserProfile>> getUserProfile(
        @Header("Authorization") String bearerToken
    );
}
