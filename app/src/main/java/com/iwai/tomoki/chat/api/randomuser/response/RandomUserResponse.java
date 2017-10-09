package com.iwai.tomoki.chat.api.randomuser.response;

import android.text.TextUtils;

import com.annimon.stream.Optional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.Data;

/**
 * Randomuser Api Response
 */
public class RandomUserResponse {
    public List<User> results = Collections.emptyList();

    @Data
    public static class User {
        /**
         * 性別
         */
        private String gender;
        /**
         * 名前
         */
        private Name name;
        /**
         * メールアドレス
         */
        private String email;
        /**
         * 画像
         */
        private Picture picture;

        /**
         * フルネームを取得します。
         *
         * @return フルネーム
         */
        public String getFullName() {
            return TextUtils.join(" ", Arrays.asList(
                    Optional.ofNullable(name.first).orElse(""),
                    Optional.ofNullable(name.last).orElse("")));
        }
    }

    /**
     * 名前
     */
    @Data
    @SuppressWarnings("WeakerAccess")
    public static class Name {
        /**
         * 名
         */
        private String first;
        /**
         * 姓
         */
        private String last;
    }

    /**
     * 画像
     */
    @Data
    @SuppressWarnings("WeakerAccess")
    public static class Picture {
        /**
         * largeサイズ画像URL
         */
        private String large;
        /**
         * mediumサイズ画像URL
         */
        private String medium;
        /**
         * サムネイル画像URL
         */
        private String thumbnail;
    }
}
