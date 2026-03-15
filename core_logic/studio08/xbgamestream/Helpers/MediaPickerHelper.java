package com.studio08.xbgamestream.Helpers;

import android.net.Uri;
import android.util.Log;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.anggrayudi.storage.file.MimeType;
import java.util.List;
/* loaded from: /app/base.apk/classes3.dex */
public class MediaPickerHelper {
    private MediaPickerCallback listener;
    private ActivityResultLauncher<String[]> pickAudio;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    /* loaded from: /app/base.apk/classes3.dex */
    public interface MediaPickerCallback {
        void onMediaPicked(List<Uri> list);
    }

    public MediaPickerHelper(AppCompatActivity appCompatActivity, MediaPickerCallback mediaPickerCallback) {
        this.listener = mediaPickerCallback;
        registerVideoPicker(appCompatActivity);
        registerAudioPicker(appCompatActivity);
    }

    private void registerVideoPicker(AppCompatActivity appCompatActivity) {
        this.pickMedia = appCompatActivity.registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(5), new ActivityResultCallback() { // from class: com.studio08.xbgamestream.Helpers.MediaPickerHelper$$ExternalSyntheticLambda0
            @Override // androidx.activity.result.ActivityResultCallback
            public final void onActivityResult(Object obj) {
                MediaPickerHelper.this.m337xbb950f3b((List) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$registerVideoPicker$0$com-studio08-xbgamestream-Helpers-MediaPickerHelper  reason: not valid java name */
    public /* synthetic */ void m337xbb950f3b(List list) {
        if (!list.isEmpty()) {
            Log.d("VideoPicker", "Number of items selected: " + list.size());
            this.listener.onMediaPicked(list);
            return;
        }
        Log.d("VideoPicker", "No media selected");
    }

    private void registerAudioPicker(AppCompatActivity appCompatActivity) {
        this.pickAudio = appCompatActivity.registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), new ActivityResultCallback() { // from class: com.studio08.xbgamestream.Helpers.MediaPickerHelper$$ExternalSyntheticLambda1
            @Override // androidx.activity.result.ActivityResultCallback
            public final void onActivityResult(Object obj) {
                MediaPickerHelper.this.m336x25a0d597((List) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$registerAudioPicker$1$com-studio08-xbgamestream-Helpers-MediaPickerHelper  reason: not valid java name */
    public /* synthetic */ void m336x25a0d597(List list) {
        if (list == null || list.isEmpty()) {
            Log.d("AudioPicker", "No audio files selected");
            return;
        }
        Log.d("AudioPicker", "Number of audio files selected: " + list.size());
        this.listener.onMediaPicked(list);
    }

    public void launchVideoPicker() {
        this.pickMedia.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE).build());
    }

    public void launchAudioPicker() {
        this.pickAudio.launch(new String[]{MimeType.AUDIO});
    }
}
