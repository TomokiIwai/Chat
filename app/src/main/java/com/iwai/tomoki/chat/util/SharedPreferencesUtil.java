package com.iwai.tomoki.chat.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.iwai.tomoki.chat.app.ChatApplication;

/**
 * {@link SharedPreferences}にアクセスするためのユーティリティクラス
 */
public class SharedPreferencesUtil {
    private static final String NAME = "chat";

    // お客さまID
    private static final String KEY_USER_ID = "user_id";

    /**
     * Get shared preferences.
     *
     * @return {@link SharedPreferences}
     */
    @SuppressWarnings("WeakerAccess")
    public static SharedPreferences getSharedPreferences() {
        return ChatApplication.getInstance().getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    /**
     * 文字列を取得します。
     *
     * @param key    key name
     * @param defVal default value
     * @return String value
     */
    private static String getString(final String key, final String defVal) {
        return SharedPreferencesUtil.getSharedPreferences().getString(key, defVal);
    }

    /**
     * 文字列を保存します。
     *
     * @param key   key name
     * @param value String value
     */
    private static void putString(final String key, final String value) {
        SharedPreferencesUtil.getSharedPreferences().edit().putString(key, value).apply();
    }

    /**
     * クリアします。
     */
    public static void clear() {
        SharedPreferencesUtil.getSharedPreferences().edit().clear().apply();
    }

    /**
     * ユーザーIDを取得します。
     *
     * @return ユーザーID
     */
    public static String getUserId() {
        return getString(KEY_USER_ID, null);
    }

    /**
     * ユーザーIDを保存します。
     *
     * @param userId ユーザーID
     */
    public static void setUserId(final String userId) {
        putString(KEY_USER_ID, userId);
    }
}
