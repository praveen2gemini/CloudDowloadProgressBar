package dpdlad.com.clouddowloadprogressbar;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.dpdlad.customprogressbar.DownloadProgressBar;

/**
 * @author Praveen Kumar updated on 23 Nov 2018
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int THREAD_DELAYED_TIME = 300;
    private int mCounter = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViewWithListener(R.id.animated_cloud_progressbar1,
                R.id.animated_cloud_progressbar2,
                R.id.animated_cloud_progressbar3);
    }

    private void initViewWithListener(@IdRes int... animated_cloud_progressbarIds) {
        for (int animated_cloud_progressbarId : animated_cloud_progressbarIds)
            findViewById(animated_cloud_progressbarId).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * It is just demo purpose created {@link Runnable} for update the progress value every seconds.
     *
     * @param cloudProgressBar
     */
    private void startProgressDemo(final DownloadProgressBar cloudProgressBar) {
        if (mCounter == -1) {
            cloudProgressBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cloudProgressBar.clearAnimation();
                    cloudProgressBar.post(getProgressRunnable(cloudProgressBar, mCounter));
                }
            }, 3000);
            cloudProgressBar.startBlink();
        }
    }

    private Runnable getProgressRunnable(final DownloadProgressBar cloudProgressBar, @IntRange(from = 0, to = 100) final int progress) {
        return new Runnable() {
            @Override
            public void run() {
                // This condition not needed for real scenario. It just used for demo
                if (mCounter <= 100 && progress <= cloudProgressBar.getMaximumProgress()) {
                    cloudProgressBar.setProgress(progress);
                    Log.e("###############", "mCounter: " + mCounter);
                    mCounter++;
                    cloudProgressBar.postDelayed(getProgressRunnable(cloudProgressBar, mCounter), THREAD_DELAYED_TIME);
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
        startProgressDemo((DownloadProgressBar) v);
    }
}
