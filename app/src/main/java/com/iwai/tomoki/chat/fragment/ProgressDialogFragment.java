package com.iwai.tomoki.chat.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iwai.tomoki.chat.R;

/**
 * プログレスダイアログクラス
 */
public class ProgressDialogFragment extends DialogFragment {
    public static final String TAG = ProgressDialogFragment.class.getSimpleName();

    private static boolean mIsShown = false;

    /**
     * Create new instance.
     */
    private static ProgressDialogFragment newInstance() {
        return new ProgressDialogFragment();
    }

    /**
     * コンストラクタ
     */
    public ProgressDialogFragment() {
    }

    /**
     * Called when a fragment is first attached to its activity.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    /**
     * Called to do initial creation of a fragment.  This is called after
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.ChatTheme_Dialog);
    }

    /**
     * Create dialog container.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // ダイアログ外タップで消えないように設定
        dialog.setCanceledOnTouchOutside(false);

        setCancelable(false);

        return dialog;
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * プログレスダイアログを表示します。このメソッドでダイアログを表示した場合は、必ず対応するdismiss()メソッドでダイアログを閉じてください。
     *
     * @param fm {@link FragmentManager}
     */
    public static void show(FragmentManager fm) {
        Fragment prev = fm.findFragmentByTag(ProgressDialogFragment.TAG);
        if (prev != null) {
            return;
        }

        synchronized (ProgressDialogFragment.class) {
            if (mIsShown) {
                return;
            }

            mIsShown = true;

            ProgressDialogFragment fragment = ProgressDialogFragment.newInstance();
            fragment.setShowsDialog(true);
            fragment.show(fm, ProgressDialogFragment.TAG);
        }
    }

    /**
     * プログレスダイアログを消します。
     *
     * @param fm {@link FragmentManager}
     */
    public static void dismiss(FragmentManager fm) {
        Fragment prev = fm.findFragmentByTag(ProgressDialogFragment.TAG);
        if (prev == null) {
            return;
        }

        synchronized (ProgressDialogFragment.class) {
            if (!mIsShown) {
                return;
            }

            mIsShown = false;

            fm.beginTransaction().remove(prev).commit();
        }
    }
}
