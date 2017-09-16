package com.htpu.bletracker.data;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.htpu.bletracker.BR;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel(Parcel.Serialization.BEAN)
public class Ble extends BaseObservable {
    @Nullable
    final String rssi;

    @Nullable
    final String deviceName;

    @NonNull
    final String deviceId;


    @ParcelConstructor
    public Ble(@Nullable String rssi, @Nullable String deviceName, @NonNull String deviceId) {
        this.rssi = rssi;
        this.deviceName = deviceName;
        this.deviceId = deviceId;
    }

    @Nullable
    public String getRssi() {
        return rssi;
    }

    @Nullable
    public String getDeviceName() {
        return deviceName;
    }

    @NonNull
    public String getDeviceId() {
        return deviceId;
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(deviceId, deviceName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ble ble = (Ble) obj;
        return Objects.equal(deviceId, ble.deviceId) &&
                Objects.equal(deviceName, ble.deviceName);
    }



    @Override
    public String toString() {
        return "Ble device " + deviceId + " with name " + deviceName + " and RSSI " + rssi;
    }
}
