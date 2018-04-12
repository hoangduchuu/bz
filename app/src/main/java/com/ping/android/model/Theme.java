package com.ping.android.model;

import com.ping.android.model.enums.Color;

public class Theme {
    public int mainColor;

    public Color getColor() {
        return Color.from(mainColor);
    }
}
