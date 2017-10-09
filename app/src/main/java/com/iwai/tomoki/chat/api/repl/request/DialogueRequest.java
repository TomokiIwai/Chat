package com.iwai.tomoki.chat.api.repl.request;

import com.annimon.stream.Optional;
import com.iwai.tomoki.chat.security.AndroidKeyStoreManager;
import com.iwai.tomoki.chat.util.SharedPreferencesUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dialogue API Request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DialogueRequest {
    /**
     * ユーザーID
     */
    private String appUserId;
    /**
     * ボットID
     */
    private String botId;
    /**
     * テキスト
     */
    private String voiceText;
    /**
     * 初回発話フラグ
     */
    private boolean initTalkingFlag;
    /**
     * シナリオID
     */
    private String initTopicId;

    /**
     * 会話開始リクエストを生成します。
     *
     * @return {@link DialogueRequest}
     */
    public static DialogueRequest forStart() {
        return Optional.ofNullable(SharedPreferencesUtil.getUserId())
                .map(AndroidKeyStoreManager.getInstance()::decrypt)
                .map(userId -> new DialogueRequest(userId, "sample", "init", true, "aisatsu"))
                .orElse(null);
    }

    /**
     * 会話リクエストを生成します。
     *
     * @param body 本文
     * @return {@link DialogueRequest}
     */
    public static DialogueRequest forTalk(final String body) {
        return Optional.ofNullable(SharedPreferencesUtil.getUserId())
                .map(AndroidKeyStoreManager.getInstance()::decrypt)
                .map(userId -> new DialogueRequest(userId, "sample", body, false, null))
                .orElse(null);
    }
}
