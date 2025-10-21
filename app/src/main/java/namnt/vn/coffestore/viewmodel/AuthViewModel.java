package namnt.vn.coffestore.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;  // Thay vì ViewModel để có Application context
import androidx.lifecycle.LiveData;

import namnt.vn.coffestore.data.model.auth.LoginRequest;
import namnt.vn.coffestore.data.model.auth.RegisterRequest;
import namnt.vn.coffestore.data.model.auth.TokenResponse;
import namnt.vn.coffestore.data.repository.AuthRepository;

public class AuthViewModel extends AndroidViewModel {
    private AuthRepository repository;
    public interface AuthResult {
        boolean isSuccess();
        String getMessage();
         TokenResponse getTokenResponse() ;
    }

    public AuthViewModel(Application application) {
        super(application);
        repository = new AuthRepository(application);  // Truyền context

    }

    public void register(RegisterRequest request) {
        repository.register(request);
    }

    public void login(LoginRequest request) {
        repository.login(request);
    }

    public LiveData<AuthViewModel.AuthResult> getAuthResult() {
        return repository.getAuthLiveData();
    }

    // Thêm methods để lấy token
    public String getAccessToken() {
        return repository.getAccessToken();
    }

    public boolean isTokenValid() {
        return repository.isTokenValid();
    }
}
