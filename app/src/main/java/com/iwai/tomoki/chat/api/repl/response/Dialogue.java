package com.iwai.tomoki.chat.api.repl.response;

import java.util.Date;

import lombok.Data;

/**
 * Repl AI対話レスポンス
 */
@Data
public class Dialogue {
    /**
     * システムからの応答
     */
    @Data
    public static class SystemText {
        /**
         * システムからの応答文字列
         */
        private String expression;
    }

    /**
     * システムからの応答
     */
    private SystemText systemText;
    /**
     * サーバがレスポンスを送信した時刻
     */
    private Date serverSendTime;
}
