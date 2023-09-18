package me.teboho.tweetjava.ui.postimage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PostImageViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public PostImageViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}