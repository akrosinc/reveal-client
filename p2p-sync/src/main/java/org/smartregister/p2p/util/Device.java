package org.smartregister.p2p.util;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 26/03/2019
 */

public class Device {

    /**
     * Returns a stable, unique identifier for this device.
     * <p>
     * Historically this returned the device's WLAN MAC address. Since Android 6.0 (API 23)
     * apps can no longer read the real hardware MAC address - {@link #getMacAddress()} returns
     * {@code null} or the anonymized {@code 02:00:00:00:00:00} - so relying on it caused the
     * "error occurred trying to get your WiFi Mac address" failure on modern devices.
     * <p>
     * We now attempt the legacy MAC address first (still valid on older/rooted devices for
     * backwards compatibility with previously paired peers) and otherwise fall back to a
     * randomly generated UUID that is persisted in {@link Settings}, guaranteeing a stable
     * unique identifier without depending on privileged network information.
     *
     * @return a non-null unique device identifier, or {@code null} only if no context is provided
     */
    @Nullable @WorkerThread
    public static final String generateUniqueDeviceId(Context context) {
        if (context == null) {
            return null;
        }

        String macAddress = getMacAddress();
        if (isValidMacAddress(macAddress)) {
            return macAddress;
        }

        return getOrCreatePersistedDeviceId(context);
    }

    /**
     * The real hardware MAC address is unavailable on Android 6+. When it cannot be read the
     * OS returns either {@code null} or the fixed placeholder {@code 02:00:00:00:00:00}. Treat
     * both as invalid so we fall back to a persisted identifier.
     */
    static boolean isValidMacAddress(@Nullable String macAddress) {
        return macAddress != null && !macAddress.replace(":", "").matches("0*");
    }

    @NonNull
    private static String getOrCreatePersistedDeviceId(@NonNull Context context) {
        Settings settings = new Settings(context);
        String deviceId = settings.getDeviceId();
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            settings.saveDeviceId(deviceId);
        }
        return deviceId;
    }

    /**
     * This method returns WLAN0's MAC Address
     *
     * @return  WLAN0 MAC address or NULL if unable to get the mac address
     */
    @Nullable
    public static String getMacAddress() {
        String macAddress = null;
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            NetworkInterface wifiInterface = null;
            for (NetworkInterface nif : all) {
                if (nif.getName().equalsIgnoreCase("wlan0")) {
                    wifiInterface = nif;
                    break;
                }
            }

            if (wifiInterface != null) {
                byte[] macBytes = wifiInterface.getHardwareAddress();
                if (macBytes == null) {
                    return null;
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF));
                    res1.append(":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }

                macAddress = res1.toString();
            }

        } catch (SocketException ex) {
            Timber.e(ex);
        }

        return macAddress;
    }

}
