package com.studio08.xbgamestream.ui.gamestream;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
/* loaded from: /app/base.apk/classes3.dex */
public class GamestreamViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public GamestreamViewModel() {
        MutableLiveData<String> mutableLiveData = new MutableLiveData<>();
        this.mText = mutableLiveData;
        mutableLiveData.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return this.mText;
    }
}
