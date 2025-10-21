package namnt.vn.coffestore.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.data.model.CoffeeItem;
import namnt.vn.coffestore.network.ApiService;
import namnt.vn.coffestore.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CoffeeRepository {
    private ApiService apiService;
    private MutableLiveData<List<CoffeeItem>> menuLiveData = new MutableLiveData<>();

    public CoffeeRepository() {
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
    }

//    public void fetchMenu() {
//        apiService.getMenu().enqueue(new Callback<ApiResponse<List<CoffeeItem>>>() {
//            @Override
//            public void onResponse(Call<ApiResponse<List<CoffeeItem>>> call, Response<ApiResponse<List<CoffeeItem>>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    menuLiveData.setValue(response.body().getData());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ApiResponse<List<CoffeeItem>>> call, Throwable t) {
//                // Handle error (e.g., show toast)
//            }
//        });
//    }

    public LiveData<List<CoffeeItem>> getMenuLiveData() {
        return menuLiveData;
    }
}