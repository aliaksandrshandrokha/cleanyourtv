package com.gathin.tvhome;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.tv.TvInputInfo;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Process;
import android.os.UserManager;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.Locale;

/**
 * API 22 port: every platform call the reference launcher (minSdk 26) makes that Android 5.1 lacks goes
 * through here, with the nearest older behaviour as the fallback. The call sites stay one-liners, so a
 * newer upstream launcher can be re-ported by re-running lint and routing the new calls the same way.
 */
final class Compat {
    private static final int SDK = Build.VERSION.SDK_INT;

    private Compat() {}

    /** Direct boot arrived with Android 7; before it the user's storage is always available. */
    static boolean unlocked(UserManager users) {
        return SDK < 24 || users == null || users.isUserUnlocked();
    }

    static int checkSelfPermission(Context context, String permission) {
        return context.checkPermission(permission, Process.myPid(), Process.myUid());
    }

    /** Before Android 7 a service cannot take itself off the enabled list; it simply stays bound. */
    static void disableSelf(AccessibilityService service) {
        if (SDK >= 24) service.disableSelf();
    }

    static void noOverlappingRendering(View view) {
        if (SDK >= 24) view.forceHasOverlappingRendering(false);
    }

    static ColorStateList colorStateList(Context context, int id) {
        return SDK >= 23 ? context.getColorStateList(id) : context.getResources().getColorStateList(id);
    }

    static ColorStateList drawableTint(TextView view) {
        if (SDK >= 23) return view.getCompoundDrawableTintList();
        return view instanceof PillTextView ? ((PillTextView) view).tint() : null;
    }

    /** Android 5 has no compound-drawable tint on TextView; there the pills (PillTextView) tint themselves. */
    static void setDrawableTint(TextView view, ColorStateList tint) {
        if (SDK >= 23) {
            view.setCompoundDrawableTintList(tint);
        } else if (view instanceof PillTextView) {
            ((PillTextView) view).setTint(tint);
        }
    }

    static Locale locale(Resources res) {
        return SDK >= 24 ? res.getConfiguration().getLocales().get(0) : res.getConfiguration().locale;
    }

    static boolean inputHidden(TvInputInfo info, Context context) {
        return SDK >= 24 && info.isHidden(context);
    }

    static CharSequence inputCustomLabel(TvInputInfo info, Context context) {
        return SDK >= 24 ? info.loadCustomLabel(context) : null;
    }

    static void registerDefaultNetworkCallback(ConnectivityManager cm, ConnectivityManager.NetworkCallback callback) {
        if (SDK >= 24) {
            cm.registerDefaultNetworkCallback(callback);
        } else {
            cm.registerNetworkCallback(new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(), callback);
        }
    }

    /** NET_CAPABILITY_VALIDATED is Android 6; before it "has internet" is the closest answer. */
    static boolean validated(NetworkCapabilities caps) {
        return caps.hasCapability(SDK >= 23
                ? NetworkCapabilities.NET_CAPABILITY_VALIDATED : NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    static boolean online(ConnectivityManager cm) {
        if (cm == null) return false;
        if (SDK >= 23) {
            android.net.Network n = cm.getActiveNetwork();
            NetworkCapabilities caps = n != null ? cm.getNetworkCapabilities(n) : null;
            return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        }
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /** res/font is Android 8; AndroidX reads the same font XML on older releases (without variation axes). */
    static Typeface font(Context context, int fontId) {
        if (SDK >= 26) return context.getResources().getFont(fontId);
        try {
            Typeface face = ResourcesCompat.getFont(context, fontId);
            return face != null ? face : Typeface.DEFAULT;
        } catch (RuntimeException unreadable) {
            return Typeface.DEFAULT;
        }
    }

    /** A GPU-only copy of the bitmap, or null where hardware bitmaps do not exist (before Android 8). */
    static Bitmap hardwareCopy(Bitmap decoded) {
        return SDK >= 26 ? decoded.copy(Bitmap.Config.HARDWARE, false) : null;
    }

    /** PackageManager's change sequence is Android 8; before it the package broadcasts alone mark tiles stale. */
    static boolean packagesChanged(Context context, int since) {
        if (SDK < 26) return false;
        android.content.pm.ChangedPackages changed = context.getPackageManager().getChangedPackages(since);
        return changed != null && !changed.getPackageNames().isEmpty();
    }

    static int packageSequence(Context context, int since) {
        if (SDK < 26) return since;
        android.content.pm.ChangedPackages changed = context.getPackageManager().getChangedPackages(since);
        return changed != null ? changed.getSequenceNumber() : since;
    }
}
