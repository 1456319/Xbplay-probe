package com.studio08.xbgamestream;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

/**
 * MainActivity serves as a placeholder entry point for the reconstructed Android project.
 * Its primary documented function is to proactively attempt to load the required
 * native JNI libraries (e.g., 'gkcodecs') upon initialization to support the
 * application's core logic without relying on the original complex UI structure.
 */
public class MainActivity extends AppCompatActivity {
    static {
        // Attempt to load native libraries required for media decoding/streaming
        try {
            System.loadLibrary("gkcodecs");
        } catch (UnsatisfiedLinkError e) {
            // Expected to fail if JNI libs (.so) are not properly mapped or available for the architecture
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
