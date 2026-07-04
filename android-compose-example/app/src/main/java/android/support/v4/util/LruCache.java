package android.support.v4.util;

/**
 * Compatibility shim for legacy libraries that still reference
 * android.support.v4.util.LruCache after the app migrated to AndroidX.
 *
 * ActiveAndroid 3.1.0 loads this class reflectively/at runtime. Delegating to
 * the platform implementation avoids bringing old com.android.support artifacts
 * back into the Compose/AndroidX app.
 */
public class LruCache<K, V> extends android.util.LruCache<K, V> {

    public LruCache(int maxSize) {
        super(maxSize);
    }
}
