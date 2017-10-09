package com.iwai.tomoki.chat;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.iwai.tomoki.chat.api.repl.ReplAiApi;
import com.iwai.tomoki.chat.api.repl.request.RegistrationRequest;
import com.iwai.tomoki.chat.api.repl.response.UserId;
import com.iwai.tomoki.chat.app.ChatApplication;
import com.iwai.tomoki.chat.fragment.ProgressDialogFragment;
import com.iwai.tomoki.chat.fragment.UserListFragment;
import com.iwai.tomoki.chat.security.AndroidKeyStoreManager;
import com.iwai.tomoki.chat.util.SharedPreferencesUtil;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * メインアクティビティ
 */
public class MainActivity extends AppCompatActivity {
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {
        super.onStart();

        // ユーザー一覧画面を起動
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, UserListFragment.newInstance());
        ft.commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (TextUtils.isEmpty(SharedPreferencesUtil.getUserId())) {
            // ユーザーID取得APIを呼び出す
            ReplAiApi.get().registration(new RegistrationRequest("sample"))
                    .onErrorResumeNext(Observable.empty())
                    // 通信をバックグランドスレッドで実行
                    .subscribeOn(Schedulers.from(ChatApplication.getInstance().getThreadPoolExecutor()))
                    // レスポンス処理をmainスレッドで実行
                    .observeOn(AndroidSchedulers.mainThread())
                    // ユーザーIDを取得
                    .map(UserId::getAppUserId)
                    // 暗号化
                    .map(AndroidKeyStoreManager.getInstance()::encrypt)
                    // プログレスダイアログ表示
                    .doOnNext(e -> ProgressDialogFragment.show(getSupportFragmentManager()))
                    // プログレスダイアログ停止
                    .doOnComplete(() -> ProgressDialogFragment.dismiss(getSupportFragmentManager()))
                    // ユーザーIDを保存
                    .subscribe(SharedPreferencesUtil::setUserId);
        }
    }
}
