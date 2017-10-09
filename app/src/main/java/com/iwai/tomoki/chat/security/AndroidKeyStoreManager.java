package com.iwai.tomoki.chat.security;

import android.security.KeyPairGeneratorSpec;
import android.util.Base64;

import com.annimon.stream.Optional;
import com.iwai.tomoki.chat.app.ChatApplication;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

import timber.log.Timber;

/**
 * AndroidKeyStoreプロバイダが提供する機能へアクセスするためのマネージャークラス
 */
public class AndroidKeyStoreManager {
    // エイリアス
    private static final String KEY_STORE_ALIAS = "ChatApplication";
    // アルゴリズム(Android 6.0未満もサポートするならRSAにする)
    private static final String KEY_STORE_ALGORITHM = "RSA";
    // プロバイダ名
    private static final String KEY_STORE_PROVIDER = "AndroidKeyStore";
    // 暗号化アルゴリズム
    private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";

    private static AndroidKeyStoreManager mInstance;

    private KeyStore mKeyStore;

    /**
     * コンストラクタ
     */
    private AndroidKeyStoreManager() {
        try {
            mKeyStore = KeyStore.getInstance(KEY_STORE_PROVIDER);
            mKeyStore.load(null);
        } catch (Exception e) {
            Timber.e(e);
            throw new IllegalStateException("Device does not support AndroidKeyStore.");
        }
    }

    /**
     * インスタンスを取得します。
     */
    public static AndroidKeyStoreManager getInstance() {
        if (mInstance == null) {
            mInstance = new AndroidKeyStoreManager();
        }
        return mInstance;
    }

    /**
     * AndroidKeyStoreに、公開鍵認証基盤における公開鍵と秘密鍵を生成します。生成された鍵はAndroidKeyStoreに保存されます。
     *
     * @return {@link KeyPair}
     */
    private KeyPair createKeyPair() {
        try {
            final KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_STORE_ALGORITHM, KEY_STORE_PROVIDER);
            generator.initialize(AndroidKeyStoreManager.createCryptographicParam());
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("Device does not support AndroidKeyStore.", e);
        }
    }

    /**
     * 暗号化パラメータを生成します。
     * {@link KeyPairGeneratorSpec}は@{@link Deprecated}に指定されていますが、Android 6.0未満をサポート対象とするので当該クラスの利用は不可避です。
     *
     * @return {@link KeyPairGeneratorSpec}
     */
    @SuppressWarnings("deprecation")
    private static KeyPairGeneratorSpec createCryptographicParam() {
        final Calendar start = Calendar.getInstance();
        final Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 20);

        return new KeyPairGeneratorSpec.Builder(ChatApplication.getInstance())
                .setAlias(KEY_STORE_ALIAS)
                .setSubject(new X500Principal(String.format("CN=%s", KEY_STORE_ALIAS)))
                .setSerialNumber(BigInteger.valueOf(1))
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .setKeySize(1024)
                .build();
    }

    /**
     * 公開鍵を取得します。
     *
     * @return {@link PublicKey}
     */
    private PublicKey getPublicKey() {
        try {
            return Optional.ofNullable(mKeyStore.getEntry(KEY_STORE_ALIAS, null)).map(KeyStore.PrivateKeyEntry.class::cast)
                    // 証明書
                    .map(KeyStore.PrivateKeyEntry::getCertificate)
                    // 公開鍵
                    .map(Certificate::getPublicKey)
                    // 新規生成
                    .orElseGet(() -> createKeyPair().getPublic());
        } catch (Exception e) {
            throw new IllegalStateException("Public-key could not be found in android key store.", e);
        }
    }

    /**
     * 秘密鍵を取得します。
     *
     * @return {@link PrivateKey}
     */
    private PrivateKey getPrivateKey() {
        try {
            if (!mKeyStore.containsAlias(KEY_STORE_ALIAS)) {
                throw new IllegalStateException("key store was not initialized.");
            }

            return ((KeyStore.PrivateKeyEntry) mKeyStore.getEntry(KEY_STORE_ALIAS, null)).getPrivateKey();
        } catch (Exception e) {
            throw new IllegalStateException("Private-key could ot be found in android key store.", e);
        }
    }

    /**
     * テキストを暗号化します。
     *
     * @param plain 文字列
     * @return 暗号化済み文字列
     */
    public String encrypt(final String plain) {
        final byte[] encrypted = encrypt(plain.getBytes());
        return Base64.encodeToString(encrypted, Base64.NO_WRAP);
    }

    /**
     * バイト列を暗号化します。
     *
     * @param bytes byte列
     * @return 暗号化済みbyte列
     */
    private byte[] encrypt(final byte[] bytes) {
        try {
            final Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKey());
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            Timber.e(e);
            throw new RuntimeException("Failed to encrypt.", e);
        }
    }

    /**
     * テキストを復号化します。
     *
     * @param encrypted 暗号化済み文字列(Base64エンコード)
     * @return 文字列
     */
    public String decrypt(final String encrypted) {
        final byte[] enc = Base64.decode(encrypted.getBytes(), Base64.NO_WRAP);
        return new String(decrypt(enc));
    }

    /**
     * byte列を復号化します。
     *
     * @param bytes 暗号化済みbyte列
     * @return byte列
     */
    private byte[] decrypt(byte[] bytes) {
        try {
            final Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());

            return cipher.doFinal(bytes);
        } catch (Exception e) {
            Timber.e(e);
            throw new RuntimeException("Failed to decrypt.", e);
        }
    }

    /**
     * キーストアをクリアします。
     */
    public void clear() {
        try {
            mKeyStore.deleteEntry(KEY_STORE_ALIAS);
        } catch (KeyStoreException ignored) {
        }
    }
}
