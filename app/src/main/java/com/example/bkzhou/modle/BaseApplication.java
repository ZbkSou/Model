package com.example.bkzhou.modle;

import android.app.Application;
import android.content.Context;

import com.example.bkzhou.modle.network.UtilVolley;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * Created by bkzhou on 15-9-12.
 */
public class BaseApplication extends Application {
    public static Context mContext;
    public static UtilVolley mUtilVolley;

    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context)
                .threadPoolSize(3).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().init(config);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

    }

    public static Context getContext() {
        return mContext;
    }

    public static UtilVolley getUtilVolley() {
        if (mUtilVolley == null) {
            mUtilVolley = new UtilVolley(mContext);
        }
        return mUtilVolley;
    }

    /**
     * 获取volley ImageLoader
     *
     * @return
     */
    public static com.android.volley.toolbox.ImageLoader getVolleyImageLoader() {
        return getUtilVolley().getImageLoader();
    }



}
