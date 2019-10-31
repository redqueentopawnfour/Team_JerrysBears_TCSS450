package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Weather;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WeatherViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public WeatherViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Weather fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}