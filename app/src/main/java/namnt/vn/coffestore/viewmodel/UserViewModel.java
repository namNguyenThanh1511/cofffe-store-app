package namnt.vn.coffestore.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import namnt.vn.coffestore.data.model.auth.UserProfile;
import namnt.vn.coffestore.data.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository repository = new UserRepository();
    private final MutableListLiveData liveData = new MutableListLiveData();

    public static class MutableListLiveData extends MutableLiveData<UserRepository.ProfileResult> {}

    public UserViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<UserRepository.ProfileResult> getProfileResult() {
        return liveData;
    }

    public void fetchProfile(String bearerToken) {
        repository.fetchProfile(bearerToken, liveData);
    }
}
