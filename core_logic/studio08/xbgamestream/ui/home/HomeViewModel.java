package com.studio08.xbgamestream.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
/* loaded from: /app/base.apk/classes3.dex */
public class HomeViewModel extends ViewModel {
    private MutableLiveData<String> mText = new MutableLiveData<>();

    public LiveData<String> getText() {
        return this.mText;
    }
}
