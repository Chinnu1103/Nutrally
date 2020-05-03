package com.example.nutrally.ui.Social;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SocialViewModel extends ViewModel {

    private MutableLiveData<Integer> currentLikes;

    public MutableLiveData<Integer> getCurrentLikes() {
        if (currentLikes == null) currentLikes = new MutableLiveData<>();
        return currentLikes;
    }

}