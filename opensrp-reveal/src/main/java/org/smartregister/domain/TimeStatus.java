package org.smartregister.domain;

import org.smartregister.reveal.R;


public enum TimeStatus {
    OK(R.string.device_time_ok), TIMEZONE_MISMATCH(R.string.timezone_mismatch), TIME_MISMATCH(
            R.string.time_mismatch), ERROR(R.string.time_error);

    private final int message;

    TimeStatus(int message) {
        this.message = message;
    }

    public int getMessage() {
        return this.message;
    }
}
