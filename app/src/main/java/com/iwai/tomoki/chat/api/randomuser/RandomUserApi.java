package com.iwai.tomoki.chat.api.randomuser;

import com.iwai.tomoki.chat.api.randomuser.response.RandomUserResponse;
import com.iwai.tomoki.chat.app.ChatApplication;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * randomuser.me APIアクセサクラス
 */
public class RandomUserApi {
    private static final String ORIGIN = "https://randomuser.me";

    private static RandomUserApi instance;

    private Api mInterface;

    /**
     * randomuser.me Apiインターフェースを取得します。
     *
     * @return {@link Api}
     */
    public static Api get() {
        synchronized (RandomUserApi.class) {
            if (instance == null) {
                instance = new RandomUserApi();
            }
        }
        return instance.mInterface;
    }

    /**
     * コンストラクタ
     */
    private RandomUserApi() {
        mInterface = ChatApplication.retrofitBuilder()
                .client(httpClient())
                .baseUrl(ORIGIN)
                .build()
                .create(Api.class);
    }

    /**
     * {@link OkHttpClient}を生成します。
     *
     * @return {@link OkHttpClient}
     */
    private OkHttpClient httpClient() {
        return ChatApplication.httpClientBuilder().build();
    }

    /**
     * Api Interface
     */
    public interface Api {
        /**
         * ユーザー一覧を取得します。
         *
         * @param page ページ番号
         * @return {@link RandomUserResponse}
         */
        @GET("api?results=20&seed=hoge&nat=us")
        Observable<RandomUserResponse> list(@Query("page") final Integer page);
    }
}
