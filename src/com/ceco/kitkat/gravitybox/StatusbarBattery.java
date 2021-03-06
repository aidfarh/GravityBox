/*
 * Copyright (C) 2013 Peter Gregus for GravityBox Project (C3C076@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ceco.kitkat.gravitybox;

import com.ceco.kitkat.gravitybox.StatusBarIconManager.ColorInfo;
import com.ceco.kitkat.gravitybox.StatusBarIconManager.IconManagerListener;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import android.graphics.Paint;
import android.view.View;

public class StatusbarBattery implements IconManagerListener {
    private static final String TAG = "GB:StatusbarBattery";

    private View mBattery;
    private int mDefaultColor;
    private int mDefaultFrameColor;
    private int mFrameAlpha;
    private int mDefaultChargeColor;

    private static void log(String message) {
        XposedBridge.log(TAG + ": " + message);
    }

    public StatusbarBattery(View batteryView) {
        mBattery = batteryView;
        try {
            final int[] colors = (int[]) XposedHelpers.getObjectField(mBattery, "mColors");
            mDefaultColor = colors[colors.length-1];
            final Paint framePaint = (Paint) XposedHelpers.getObjectField(mBattery, "mFramePaint");
            mDefaultFrameColor = framePaint.getColor();
            mFrameAlpha = framePaint.getAlpha();
            mDefaultChargeColor = XposedHelpers.getIntField(mBattery, "mChargeColor");
        } catch (Throwable t) {
            log("Error backing up original colors: " + t.getMessage());
        }
    }

    public View getView() {
        return mBattery;
    }

    public void setColors(int mainColor, int frameColor, int chargeColor) {
        if (mBattery != null) {
            try {
                final int[] colors = (int[]) XposedHelpers.getObjectField(mBattery, "mColors");
                colors[colors.length-1] = mainColor;
                final Paint framePaint = (Paint) XposedHelpers.getObjectField(mBattery, "mFramePaint");
                framePaint.setColor(frameColor);
                framePaint.setAlpha(mFrameAlpha);
                XposedHelpers.setIntField(mBattery, "mChargeColor", chargeColor);
            } catch (Throwable t) {
                log("Error setting colors: " + t.getMessage());
            }
        }
    }

    @Override
    public void onIconManagerStatusChanged(int flags, ColorInfo colorInfo) {
        if ((flags & StatusBarIconManager.FLAG_ICON_COLOR_CHANGED) != 0) {
            if (colorInfo.coloringEnabled) {
                setColors(colorInfo.iconColor[0], colorInfo.iconColor[0], colorInfo.iconColor[0]);
            } else {
                setColors(mDefaultColor, mDefaultFrameColor, mDefaultChargeColor);
            }
            mBattery.invalidate();
        }
    }
}
