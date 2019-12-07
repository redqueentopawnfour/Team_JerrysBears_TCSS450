package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class MyHomeViewModelFactory implements ViewModelProvider.Factory {
    private String mEmail;
    private String mJwt;

    public MyHomeViewModelFactory(String email, String jwt) {
        mEmail = email;
        mJwt = jwt;
    }


    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new HomeViewModel(mEmail, mJwt);
    }
}
