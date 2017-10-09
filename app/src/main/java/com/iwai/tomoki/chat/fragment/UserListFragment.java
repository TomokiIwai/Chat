package com.iwai.tomoki.chat.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionInflater;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Optional;
import com.iwai.tomoki.chat.R;
import com.iwai.tomoki.chat.api.randomuser.RandomUserApi;
import com.iwai.tomoki.chat.api.randomuser.response.RandomUserResponse;
import com.iwai.tomoki.chat.app.ChatApplication;
import com.jakewharton.rxbinding2.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding2.view.RxView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.AllArgsConstructor;

import static android.widget.LinearLayout.VERTICAL;

/**
 * ユーザーリストフラグメントクラス
 */
public class UserListFragment extends Fragment {
    /**
     * RecyclerViewのスクロールが最下部に達していることを判定する{@link Predicate}
     */
    private static final Predicate<RecyclerViewScrollEvent> REACH_BOTTOM = e -> Optional.of(e)
            // viewを取得
            .map(RecyclerViewScrollEvent::view)
            // レイアウトマネージャーを取得
            .map(RecyclerView::getLayoutManager).map(LinearLayoutManager.class::cast)
            // 要素が1つ以上ある場合
            .filter(lm -> lm.getItemCount() > 0)
            // 最後の要素が見えている場合
            .map(lm -> lm.getItemCount() - 1 <= lm.findLastCompletelyVisibleItemPosition())
            .orElse(false);

    /**
     * {@link SwipeRefreshLayout}
     */
    private SwipeRefreshLayout mRefreshLayout;
    /**
     * ユーザーリストを表示する{@link RecyclerView}
     */
    private RecyclerView mRecyclerView;
    /**
     * ユーザーリストが取得できない場合のメッセージを表示する{@link android.widget.TextView}
     */
    private AppCompatTextView mEmptyText;

    /**
     * ユーザーリスト表示{@link RecyclerView}のデータアダプター
     */
    private UserListAdapter mUserListAdapter = new UserListAdapter();

    /**
     * 表示しているユーザーリストデータのページ番号
     */
    private int mCurrentPageNumber;
    /**
     * ユーザーリストデータロード完了通知オブジェクト
     */
    private Subject<RandomUserResponse> mDataLoadedNotification = PublishSubject.create();

    /**
     * Create new instance.
     */
    public static UserListFragment newInstance() {
        UserListFragment fragment = new UserListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * コンストラクタ
     */
    public UserListFragment() {
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
        final View root = inflater.inflate(R.layout.fragment_user_list, container, false);

        bindViews(root);

        initView();

        bindEvents();

        return root;
    }

    /**
     * Viewをバインドします。
     *
     * @param root ルート{@link View}
     */
    private void bindViews(final View root) {
        mRefreshLayout = root.findViewById(R.id.refresh_layout);
        mRecyclerView = root.findViewById(R.id.recycler_view);
        mEmptyText = root.findViewById(R.id.empty_text);
    }

    /**
     * Viewの初期化処理を行います。
     */
    private void initView() {
        // RecyclerViewの区切り線を設定
        mRecyclerView.addItemDecoration(new DividerItemDecoration(ChatApplication.getInstance(), VERTICAL));
        // RecyclerViewにアダプタを設定
        mRecyclerView.setAdapter(mUserListAdapter);
    }

    /**
     * イベントハンドラーを登録します。
     */
    private void bindEvents() {
        // リプレッシュイベント
        mRefreshLayout.setOnRefreshListener(this::onRefresh);

        // 通信完了のリスナーを登録
        mDataLoadedNotification.subscribe(this::onDataLoaded);

        // RecyclerViewのスクロールイベント
        RxRecyclerView.scrollEvents(mRecyclerView)
                // ロード完了までは無視する
                .skipUntil(mDataLoadedNotification)
                // 最下部に達したら
                .filter(REACH_BOTTOM)
                // 一回だけ
                .take(1)
                // completeしたら再度subscribeする
                .repeat()
                // プログレスダイアログ表示
                .doOnNext(e -> ProgressDialogFragment.show(getFragmentManager()))
                // ハンドラーを設定
                .subscribe(this::onReachBottom);
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
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        if (mCurrentPageNumber == 0) {
            loadUserList();
        } else {
            mDataLoadedNotification.onNext(new RandomUserResponse());
        }
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
        EventBus.getDefault().unregister(this);
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
    // イベントハンドラー
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * {@link Event.SelectUser}イベントをハンドリングします。
     *
     * @param e {@link Event.SelectUser}
     */
    @Subscribe
    public void on(final Event.SelectUser e) {
        final ChatDetailFragment fragment = ChatDetailFragment.newInstance(e.viewHolder.mUser);

        getFragmentManager()
                .beginTransaction()
                .addSharedElement(e.viewHolder.mPicture, ViewCompat.getTransitionName(e.viewHolder.mPicture))
                .addToBackStack(ChatDetailFragment.TAG)
                .replace(R.id.container, fragment)
                .commit();
    }

    /**
     * リストのリフレッシュイベントをハンドリングします。
     */
    private void onRefresh() {
        mCurrentPageNumber = 0;
        mUserListAdapter.clear();

        loadUserList();
    }

    /**
     * リストの最下部到達イベントをハンドリングします。
     *
     * @param ignore {@link RecyclerViewScrollEvent}
     */
    private void onReachBottom(@SuppressWarnings("unused") final RecyclerViewScrollEvent ignore) {
        loadUserList();
    }

    /**
     * データのロード完了イベントをハンドリングします。
     *
     * @param data {@link RandomUserResponse}
     */
    private void onDataLoaded(final RandomUserResponse data) {
        // プログレスを停止
        ProgressDialogFragment.dismiss(getFragmentManager());
        // refresh layoutのくるくるを停止
        mRefreshLayout.setRefreshing(false);
        // アダプタへデータを追加
        mUserListAdapter.addItem(data.results);

        mEmptyText.setVisibility(mUserListAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // 通信処理
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * ユーザーリストをロードします。
     */
    private void loadUserList() {
        RandomUserApi.get().list(++mCurrentPageNumber)
                .onErrorReturnItem(new RandomUserResponse())
                .subscribeOn(Schedulers.from(ChatApplication.getInstance().getThreadPoolExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mDataLoadedNotification::onNext);
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // 内部クラス
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * ユーザー情報を表示する{@link RecyclerView.ViewHolder}
     */
    @SuppressWarnings("WeakerAccess")
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private static final int LAYOUT = R.layout.user_list_item;

        private AppCompatImageView mPicture;
        private AppCompatTextView mName;
        private AppCompatTextView mEmail;

        private RandomUserResponse.User mUser;

        /**
         * コンストラクタ
         */
        private UserViewHolder(final View itemView) {
            super(itemView);

            mPicture = itemView.findViewById(R.id.image_picture);
            mName = itemView.findViewById(R.id.text_name);
            mEmail = itemView.findViewById(R.id.text_email);

            bindUiEvents();
        }

        /**
         * UIイベントハンドラーを登録します。
         */
        private void bindUiEvents() {
            // クリックイベントをobservableとして扱う
            RxView.clicks(itemView)
                    // 1度イベントが発生したら、500ミリ秒の間は次のイベントを無視する
                    .debounce(500, TimeUnit.MILLISECONDS)
                    // イベントのハンドリングはMAILスレッドで行う
                    .observeOn(AndroidSchedulers.mainThread())
                    // ハンドラーを設定
                    .subscribe(this::onClick);
        }

        /**
         * 値をセットします。
         *
         * @param user {@link RandomUserResponse.User}
         */
        private void setValue(final RandomUserResponse.User user) {
            mUser = user;

            ViewCompat.setTransitionName(mPicture, user.getFullName());

            // 画像
            Picasso.with(ChatApplication.getInstance()).load(user.getPicture().getLarge()).into(mPicture);
            // 名前
            mName.setText(user.getFullName());
            // メールアドレス
            mEmail.setText(user.getEmail());
        }

        /**
         * 行がクリックされた際に呼び出されます。
         */
        private void onClick(@SuppressWarnings("unused") final Object ignore) {
            // EventBusへ通知
            EventBus.getDefault().post(new Event.SelectUser(this));
        }
    }

    /**
     * {@link RecyclerView}の{@link RecyclerView.Adapter}実装クラス
     */
    private static class UserListAdapter extends RecyclerView.Adapter<UserViewHolder> {
        private final LayoutInflater mInflater;
        private List<RandomUserResponse.User> mData = new ArrayList<>();

        /**
         * コンストラクタ
         */
        private UserListAdapter() {
            mInflater = LayoutInflater.from(new ContextThemeWrapper(ChatApplication.getInstance(), R.style.AppTheme));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new UserViewHolder(mInflater.inflate(UserViewHolder.LAYOUT, parent, false));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onBindViewHolder(UserViewHolder holder, int position) {
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
         * データを取得します。
         *
         * @param position インデックス
         * @return データ
         */
        private RandomUserResponse.User getItem(final int position) {
            return mData.get(position);
        }

        /**
         * データを追加します。
         *
         * @param data データリスト
         */
        private void addItem(final List<RandomUserResponse.User> data) {
            final int prevSize = mData.size();

            mData.addAll(data);

            notifyItemRangeInserted(prevSize, data.size());
        }

        /**
         * データをクリアします。
         */
        private void clear() {
            mData.clear();

            notifyDataSetChanged();
        }
    }

    /**
     * イベント
     */
    private static class Event {
        /**
         * ユーザー選択イベント
         */
        @AllArgsConstructor
        private static class SelectUser {
            private final UserViewHolder viewHolder;
        }
    }
}
