package com.ping.android.model;

import com.ping.android.model.enums.Color;

public class Theme {
    public int mainColor = -1;
    public String backgroundUrl;

    public Color getColor() {
        return Color.from(mainColor);
    }
}
