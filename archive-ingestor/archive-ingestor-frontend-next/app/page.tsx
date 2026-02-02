"use client"

import { useContext } from "react";
import { ThemeColors } from "./lib/client/schema";
import { getThemeColors, ThemeContext } from "./lib/client/theme";

export default function Page() {
    // Get current colors
    const currentThemeColors: ThemeColors = getThemeColors(useContext(ThemeContext));

    return (
        <div id="home" className={`${currentThemeColors.mainBgColor} ${currentThemeColors.mainTextColor}`}>Hello Index</div>
    );
}
