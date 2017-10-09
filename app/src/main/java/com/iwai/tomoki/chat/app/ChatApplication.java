package com.iwai.tomoki.chat.app;

import android.app.Application;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iwai.tomoki.chat.BuildConfig;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * {@link Application}クラス
 */
public class ChatApplication extends Application {
    // AsyncTaskの実装を参考にスレッドプールサイズを定義
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));

    private Executor mThreadPoolExecutor;

    private static ChatApplication mInstance;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        if (TextUtils.equals(BuildConfig.BUILD_TYPE, "debug")) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTerminate() {
        mInstance = null;

        super.onTerminate();
    }

    /**
     * {@link ChatApplication}インスタンスを取得します。
     */
    public static ChatApplication getInstance() {
        return mInstance;
    }

    /**
     * Get thread pool executor.
     *
     * @return {@link Executor}
     */
    public Executor getThreadPoolExecutor() {
        if (mThreadPoolExecutor == null) {
            mThreadPoolExecutor = Executors.newFixedThreadPool(CORE_POOL_SIZE);
        }

        return mThreadPoolExecutor;
    }

    /**
     * {@link OkHttpClient.Builder}を生成します。
     *
     * @return {@link OkHttpClient.Builder}
     */
    public static OkHttpClient.Builder httpClientBuilder() {
        // HTTP通信ログをTimber経由で出力
        final HttpLoggingInterceptor logging = new HttpLoggingInterceptor(Timber::d);
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(logging);
    }

    /**
     * {@link Retrofit.Builder}を生成します。
     *
     * @return {@link Retrofit.Builder}
     */
    public static Retrofit.Builder retrofitBuilder() {
        final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

        return new Retrofit.Builder()
                .callbackExecutor(getInstance().getThreadPoolExecutor())
                // レスポンスをgoogle-gsonでパース
                .addConverterFactory(GsonConverterFactory.create(gson))
                // RetrofitとRxJavaを連携
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
    }
}
