package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ConnectionViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ConnectionViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Connection fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}