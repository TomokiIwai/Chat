package com.iwai.tomoki.chat.entity;

import android.support.annotation.IntDef;

import com.annimon.stream.Optional;
import com.iwai.tomoki.chat.api.repl.response.Dialogue;

import java.util.Date;

import lombok.Data;

/**
 * メッセージ
 */
@Data
public class Message {
    public static final int TYPE_ME = 0;
    public static final int TYPE_HER = 1;

    @IntDef({TYPE_ME, TYPE_HER})
    public @interface MessageType {
    }

    /**
     * メッセージ種別
     */
    @MessageType
    private int type;
    /**
     * 本文
     */
    private String body;
    /**
     * 投稿日時
     */
    private Date postDate;
    /**
     * プロフィール画像URL
     */
    private String profileUrl;

    /**
     * コンストラクタ
     *
     * @param type     メッセージ種別
     * @param body     本文
     * @param postDate 投稿日時
     */
    public Message(@MessageType final int type, final String body, final Date postDate) {
        this.type = type;
        this.body = body;
        this.postDate = postDate;
    }

    /**
     * コンストラクタ
     *
     * @param dialogue {@link Dialogue}
     */
    public Message(final Dialogue dialogue) {
        type = TYPE_HER;

        body = Optional.ofNullable(dialogue)
                .map(Dialogue::getSystemText)
                .map(Dialogue.SystemText::getExpression)
                .orElse("");

        postDate = Optional.ofNullable(dialogue)
                .map(Dialogue::getServerSendTime)
                .orElseGet(Date::new);
    }
}
