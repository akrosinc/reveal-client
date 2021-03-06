package org.smartregister.view.contract;

import android.graphics.Color;

import org.smartregister.reveal.R;

public enum FPAlertStatus {
    EMPTY {
        public int backgroundColorResourceId() {
            return android.R.color.transparent;
        }

        public int fontColor() {
            return Color.BLACK;
        }
    }, UPCOMING {
        public int backgroundColorResourceId() {
            return R.color.alert_upcoming_light_blue;
        }

        public int fontColor() {
            return Color.BLACK;
        }
    }, NORMAL {
        public int backgroundColorResourceId() {
            return R.color.alert_in_progress_blue;
        }

        public int fontColor() {
            return Color.WHITE;
        }
    }, URGENT {
        public int backgroundColorResourceId() {
            return R.color.alert_urgent_red;
        }

        public int fontColor() {
            return Color.WHITE;
        }
    }, COMPLETE {
        public int backgroundColorResourceId() {
            return R.color.alert_complete_green;
        }

        public int fontColor() {
            return Color.WHITE;
        }
    };

    public static FPAlertStatus from(String value) {
        return valueOf(value.toUpperCase());
    }

    public abstract int backgroundColorResourceId();

    public abstract int fontColor();

}
