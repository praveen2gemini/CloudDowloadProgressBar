package dpdlad.com.clouddowloadprogressbar;

import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.dpdlad.customprogressbar.DownloadProgressBar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int THREAD_DELAYED_TIME = 300;
    private DownloadProgressBar cloudProgressBar;
    private int mCounter = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cloudProgressBar = findViewById(R.id.animated_cloud_progressbar);
        cloudProgressBar.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startProgressDemo() {
        if (mCounter == -1) {
            cloudProgressBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cloudProgressBar.post(getProgressRunnable(mCounter));
                }
            }, 3000);
        }
    }

    private Runnable getProgressRunnable(@IntRange(from = 0, to = 100) final int progress) {
        return new Runnable() {
            @Override
            public void run() {
                // This condition not needed for real scenario. It just used for demo
                if (mCounter <= 100 && progress <= cloudProgressBar.getMaximumProgress()) {
                    cloudProgressBar.setProgress(progress);
                    Log.e("###############", "mCounter: " + mCounter);
                    mCounter++;
                    cloudProgressBar.postDelayed(getProgressRunnable(mCounter), THREAD_DELAYED_TIME);
                } else {
                    mCounter = -1;
                    cloudProgressBar.reset();
                    cloudProgressBar.removeCallbacks(this);
                    Log.e("***************", "mCounter: " + mCounter);
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        startProgressDemo();
    }
}
