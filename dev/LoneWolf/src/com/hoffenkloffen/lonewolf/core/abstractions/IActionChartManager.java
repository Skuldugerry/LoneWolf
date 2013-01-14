package com.hoffenkloffen.lonewolf.core.abstractions;

import com.hoffenkloffen.lonewolf.abstractions.BrowserRenderer;

public interface IActionChartManager {

    IActionChartManager set(BrowserRenderer renderer);

    void display();

    void take(String item);

    void discard(String item);

    void use(String item);
}
