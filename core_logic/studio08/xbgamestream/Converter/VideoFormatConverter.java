package com.studio08.xbgamestream.Converter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import com.arthenica.ffmpegkit.ExecuteCallback;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFprobeKit;
import com.arthenica.ffmpegkit.LogCallback;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.Session;
import com.arthenica.ffmpegkit.SessionState;
import com.arthenica.ffmpegkit.Statistics;
import com.arthenica.ffmpegkit.StatisticsCallback;
import java.io.File;
/* loaded from: /app/base.apk/classes3.dex */
public class VideoFormatConverter {
    Context context;
    String convertQualityValue;
    String inputPath;
    private VideoConvertEvents mListener;
    String resolution;

    /* loaded from: /app/base.apk/classes3.dex */
    public interface VideoConvertEvents {
        void onStatsUpdated(Statistics statistics, String str);

        void onVideoConvertFailed(String str);

        void onVideoConvertSuccess(String str);
    }

    public String getConvertQualityValue() {
        return this.convertQualityValue;
    }

    public String getResolution() {
        return this.resolution;
    }

    public void setCustomListener(VideoConvertEvents videoConvertEvents) {
        this.mListener = videoConvertEvents;
    }

    public VideoFormatConverter(String str, Context context) {
        this.inputPath = str;
        this.context = context;
    }

    public void getFFMpegMediaInput() {
        FFprobeKit.getMediaInformation(this.inputPath).getMediaInformation();
    }

    public void cancel() {
        FFmpegKit.cancel();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getOutputFileName() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + (new File(this.inputPath).getName() + "_converted").replace(".", "") + ".mp4";
    }

    private void setSettingValues() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("SettingsSharedPref", 0);
        this.convertQualityValue = sharedPreferences.getString("video_convert_quality_key", "29");
        this.resolution = sharedPreferences.getString("video_convert_size_key", "1280x720");
    }

    public void runFFMpegConvert() {
        final String outputFileName = getOutputFileName();
        setSettingValues();
        String str = "-y -i \"" + this.inputPath + "\" -c:v h264 -crf " + this.convertQualityValue + " -preset veryfast -c:a aac -ac 2 -ab 128k -s " + this.resolution + " \"" + outputFileName + "\"";
        Log.e("HERE", "Running FFMPEG command: " + str);
        FFmpegKit.executeAsync(str, new ExecuteCallback() { // from class: com.studio08.xbgamestream.Converter.VideoFormatConverter.1
            @Override // com.arthenica.ffmpegkit.ExecuteCallback
            public void apply(Session session) {
                String str2;
                SessionState state = session.getState();
                ReturnCode returnCode = session.getReturnCode();
                if (ReturnCode.isSuccess(session.getReturnCode())) {
                    Log.e("HERE", "Session completed correctly!");
                    VideoFormatConverter.this.mListener.onVideoConvertSuccess(outputFileName);
                } else if (ReturnCode.isCancel(session.getReturnCode())) {
                    Log.e("HERE", "Session canceled correctly!");
                    VideoFormatConverter.this.mListener.onVideoConvertFailed("Canceled by user");
                } else {
                    Log.e("HERE", String.format("Command failed with state %s and rc %s.%s", session.getState(), session.getReturnCode(), session.getFailStackTrace()));
                    int size = session.getAllLogs().size();
                    if (size < 1) {
                        str2 = "NULL";
                    } else {
                        str2 = session.getAllLogs().get(size - 1).getMessage();
                    }
                    VideoFormatConverter.this.mListener.onVideoConvertFailed(str2);
                }
                Log.e("HERE", String.format("FFmpeg process exited with state %s and rc %s.%s", state, returnCode, session.getFailStackTrace()));
            }
        }, new LogCallback() { // from class: com.studio08.xbgamestream.Converter.VideoFormatConverter.2
            @Override // com.arthenica.ffmpegkit.LogCallback
            public void apply(com.arthenica.ffmpegkit.Log log) {
                Log.i("HERE", log.getMessage());
            }
        }, new StatisticsCallback() { // from class: com.studio08.xbgamestream.Converter.VideoFormatConverter.3
            @Override // com.arthenica.ffmpegkit.StatisticsCallback
            public void apply(Statistics statistics) {
                VideoFormatConverter.this.mListener.onStatsUpdated(statistics, VideoFormatConverter.this.getOutputFileName());
            }
        });
    }
}
