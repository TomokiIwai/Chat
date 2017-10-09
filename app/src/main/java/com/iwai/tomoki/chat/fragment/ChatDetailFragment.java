package com.iwai.tomoki.chat.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.transition.TransitionInflater;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Optional;
import com.iwai.tomoki.chat.R;
import com.iwai.tomoki.chat.api.randomuser.response.RandomUserResponse;
import com.iwai.tomoki.chat.api.repl.ReplAiApi;
import com.iwai.tomoki.chat.api.repl.request.DialogueRequest;
import com.iwai.tomoki.chat.app.ChatApplication;
import com.iwai.tomoki.chat.entity.Message;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * チャットフラグメントクラス
 */
public class ChatDetailFragment extends Fragment {
    public static final String TAG = ChatDetailFragment.class.getSimpleName();

    /**
     * パラメータ名：名前
     */
    private static final String PARAM_USER_NAME = "param0";
    /**
     * パラメータ名：プロフィール画像
     */
    private static final String PARAM_USER_PICTURE = "param1";

    /**
     * {@link Picasso}で画像を丸く切り取るための{@link Transformation}実装
     */
    private static final Transformation OVAL = new RoundedTransformationBuilder().oval(true).build();

    /**
     * プロフィール画像を表示する{@link android.widget.ImageView}
     */
    private AppCompatImageView mPicture;
    /**
     * 会話相手のユーザー名を表示する{@link android.widget.TextView}
     */
    private AppCompatTextView mName;
    /**
     * 会話を表示する{@link RecyclerView}
     */
    private RecyclerView mRecyclerView;
    /**
     * 送信メッセージを入力する{@link android.widget.EditText}
     */
    private AppCompatEditText mEditTextMessage;
    /**
     * 送信{@link android.widget.ImageButton}
     */
    private AppCompatImageButton mBtnSend;

    /**
     * 会話表示{@link RecyclerView}のデータアダプター
     */
    private ChatAdapter mChatAdapter;

    /**
     * 会話相手のプロフィール画像URL
     */
    private String mHerProfileUrl;

    /**
     * Create new instance.
     */
    public static ChatDetailFragment newInstance(final RandomUserResponse.User user) {
        final ChatDetailFragment fragment = new ChatDetailFragment();

        final Bundle args = new Bundle();
        args.putString(PARAM_USER_NAME, user.getFullName());
        args.putString(PARAM_USER_PICTURE, user.getPicture().getLarge());
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * コンストラクタ
     */
    public ChatDetailFragment() {
        // Required empty public constructor
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSharedElementEnterTransition(TransitionInflater.from(ChatApplication.getInstance()).inflateTransition(android.R.transition.move));
        setSharedElementReturnTransition(TransitionInflater.from(ChatApplication.getInstance()).inflateTransition(android.R.transition.move));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_chat_detail, container, false);

        bindViews(root);

        initView();

        bindUiEvents();

        return root;
    }

    /**
     * Viewをバインドします。
     *
     * @param root ルート{@link View}
     */
    private void bindViews(final View root) {
        mPicture = root.findViewById(R.id.image_picture);
        mName = root.findViewById(R.id.text_name);
        mRecyclerView = root.findViewById(R.id.recycler_view);
        mEditTextMessage = root.findViewById(R.id.edit_text_message);
        mBtnSend = root.findViewById(R.id.btn_send);
    }

    /**
     * Viewの初期化処理を行います。
     */
    private void initView() {
        // shared elementを指定
        Optional.ofNullable(getArguments())
                .map(args -> args.getString(PARAM_USER_NAME))
                .ifPresent(mPicture::setTransitionName);

        // ユーザー名
        Optional.ofNullable(getArguments())
                .map(args -> args.getString(PARAM_USER_NAME))
                .ifPresent(mName::setText);

        // サムネイル画像
        Optional.ofNullable(getArguments())
                .map(args -> args.getString(PARAM_USER_PICTURE))
                .executeIfPresent(url -> mHerProfileUrl = url)
                .ifPresent(url -> Picasso.with(ChatApplication.getInstance()).load(url).transform(OVAL).into(mPicture));

        mChatAdapter = new ChatAdapter();
        mRecyclerView.setAdapter(mChatAdapter);
    }

    /**
     * UIイベントハンドラーを登録します。
     */
    private void bindUiEvents() {
        // メッセージ入力
        RxTextView.textChanges(mEditTextMessage)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onTextChanged);
        // 送信ボタンクリック
        RxView.clicks(mBtnSend).debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onClickSend);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        loadDialogue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDetach() {
        super.onDetach();
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // UIイベントハンドラー
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * メッセージ入力内容が変化した際に呼び出されます。
     *
     * @param text 入力値
     */
    private void onTextChanged(final CharSequence text) {
        mBtnSend.setEnabled(!TextUtils.isEmpty(text));
    }

    /**
     * 送信ボタンが押下された際に呼び出されます。
     */
    private void onClickSend(@SuppressWarnings("unused") Object ignore) {
        // 入力内容を取得
        final String body = mEditTextMessage.getText().toString();
        if (TextUtils.isEmpty(body)) {
            return;
        }

        // 入力域はクリアしておく
        mEditTextMessage.setText("");

        // チャットへ発言を表示
        final Message msg = new Message(Message.TYPE_ME, body, new Date());
        mChatAdapter.addItem(msg);

        // メッセージを送信
        sendMessage(body);
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // 通信処理
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * 会話データをロードします。
     */
    private void loadDialogue() {
        ReplAiApi.get().dialogue(DialogueRequest.forStart())
                .onErrorResumeNext(Observable.empty())
                .subscribeOn(Schedulers.from(ChatApplication.getInstance().getThreadPoolExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .map(Message::new)
                .doOnNext(obj -> obj.setProfileUrl(mHerProfileUrl))
                .subscribe(mChatAdapter::addItem);
    }

    /**
     * メッセージを送信します。
     *
     * @param message メッセージ本文
     */
    private void sendMessage(final String message) {
        ReplAiApi.get().dialogue(DialogueRequest.forTalk(message))
                .onErrorResumeNext(Observable.empty())
                .subscribeOn(Schedulers.from(ChatApplication.getInstance().getThreadPoolExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .map(Message::new)
                .doOnNext(obj -> obj.setProfileUrl(mHerProfileUrl))
                .subscribe(mChatAdapter::addItem);
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // 内部クラス
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * メッセージを表示する{@link RecyclerView.ViewHolder}
     */
    @SuppressWarnings("WeakerAccess")
    public static abstract class MessageViewHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView mBody;

        /**
         * コンストラクタ
         */
        private MessageViewHolder(final View itemView) {
            super(itemView);

            mBody = itemView.findViewById(R.id.body);
        }

        /**
         * 値をセットします。
         *
         * @param message 値
         */
        public void setValue(final Message message) {
            // 本文
            Optional.ofNullable(message).map(Message::getBody).ifPresent(mBody::setText);
        }
    }

    /**
     * ユーザー自身のメッセージを表示するViewの{@link RecyclerView.ViewHolder}
     */
    private static class MyMessageViewHolder extends MessageViewHolder {
        private static final int LAYOUT = R.layout.chat_detail_list_item_me;

        /**
         * コンストラクタ
         */
        private MyMessageViewHolder(final View itemView) {
            super(itemView);
        }
    }

    /**
     * 相手のメッセージを表示するViewの{@link RecyclerView.ViewHolder}
     */
    public static class HerMessageViewHolder extends MessageViewHolder {
        private static final int LAYOUT = R.layout.chat_detail_list_item_her;

        private AppCompatImageView mPicture;

        /**
         * コンストラクタ
         */
        private HerMessageViewHolder(View itemView) {
            super(itemView);

            mPicture = itemView.findViewById(R.id.image_picture);
        }

        /**
         * {@inheritDoc}
         */
        public void setValue(final Message message) {
            super.setValue(message);

            // プロフィール画像
            Optional.ofNullable(message)
                    .map(Message::getProfileUrl)
                    .ifPresent(url -> Picasso.with(ChatApplication.getInstance()).load(url).transform(OVAL).into(mPicture));
        }
    }

    /**
     * {@link RecyclerView}の{@link RecyclerView.Adapter}実装クラス
     */
    private static class ChatAdapter extends RecyclerView.Adapter<MessageViewHolder> {
        private final LayoutInflater mInflater;
        private List<Message> mData = new ArrayList<>();

        /**
         * コンストラクタ
         */
        private ChatAdapter() {
            mInflater = LayoutInflater.from(new ContextThemeWrapper(ChatApplication.getInstance(), R.style.AppTheme));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, @Message.MessageType int viewType) {
            if (viewType == Message.TYPE_ME) {
                return new MyMessageViewHolder(mInflater.inflate(MyMessageViewHolder.LAYOUT, parent, false));
            } else if (viewType == Message.TYPE_HER) {
                return new HerMessageViewHolder(mInflater.inflate(HerMessageViewHolder.LAYOUT, parent, false));
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {
            holder.setValue(getItem(position));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getItemCount() {
            return mData.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getItemViewType(int position) {
            return Optional.ofNullable(mData.get(position)).map(Message::getType).orElse(Message.TYPE_ME);
        }

        /**
         * データを取得します。
         *
         * @param position インデックス
         * @return データ
         */
        Message getItem(final int position) {
            return mData.get(position);
        }

        /**
         * データを追加します。
         *
         * @param data データ
         */
        void addItem(final Message data) {
            if (data == null) {
                return;
            }

            mData.add(data);

            notifyItemInserted(mData.size());
        }
    }
}
