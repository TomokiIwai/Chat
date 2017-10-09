package com.iwai.tomoki.chat.api.repl;

import com.iwai.tomoki.chat.R;
import com.iwai.tomoki.chat.api.repl.request.DialogueRequest;
import com.iwai.tomoki.chat.api.repl.request.RegistrationRequest;
import com.iwai.tomoki.chat.api.repl.response.Dialogue;
import com.iwai.tomoki.chat.api.repl.response.UserId;
import com.iwai.tomoki.chat.app.ChatApplication;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * randomuser.me APIアクセサクラス
 */
public class ReplAiApi {
    private static final String ORIGIN = "https://api.repl-ai.jp";
    private static final String REPL_API_KEY = ChatApplication.getInstance().getString(R.string.repl_api_key);

    private static ReplAiApi instance;

    private Api mInterface;

    /**
     * Repl Ai APIインターフェースを取得します。
     *
     * @return {@link Api}
     */
    public static Api get() {
        synchronized (ReplAiApi.class) {
            if (instance == null) {
                instance = new ReplAiApi();
            }
        }
        return instance.mInterface;
    }

    /**
     * コンストラクタ
     */
    private ReplAiApi() {
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
        return ChatApplication.httpClientBuilder().addInterceptor(chain -> {
            final Request req = chain.request().newBuilder().addHeader("x-api-key", REPL_API_KEY).build();
            return chain.proceed(req);
        }).build();
    }

    /**
     * Api Interface
     */
    public interface Api {
        /**
         * ユーザーIDを取得します。
         *
         * @param request {@link RegistrationRequest}
         * @return {@link UserId}
         */
        @POST("v1/registration")
        Observable<UserId> registration(@Body final RegistrationRequest request);

        /**
         * 会話を行います。
         *
         * @param request {@link DialogueRequest}
         * @return {@link Dialogue}
         */
        @POST("v1/dialogue")
        Observable<Dialogue> dialogue(@Body final DialogueRequest request);
    }
}
