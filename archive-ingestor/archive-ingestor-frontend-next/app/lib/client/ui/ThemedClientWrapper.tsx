"use client"

import { useState } from "react";
import { LIGHT, ClientThemeContext, getThemeColors } from "../theme";
import ThemeSwitcher from "./ThemeSwitcher";
import { Dispatch, SetStateAction } from "react";
import { ThemeColors } from "../schema";

export default function ThemedClientWrapper({children,}: Readonly<{children: React.ReactNode;}>) {
    const [currentTheme, setCurrentTheme]: [string, Dispatch<SetStateAction<string>>] = useState<string>(LIGHT);
    const currentThemeColors: ThemeColors = currentTheme ? getThemeColors(currentTheme) : getThemeColors(LIGHT);

    return (
        <div id="themed-body" className={`${currentThemeColors.mainBgColor} ${currentThemeColors.mainTextColor}`}>
            <ClientThemeContext.Provider value={currentTheme}>
                {children}
            </ClientThemeContext.Provider>
            <ThemeSwitcher currentTheme={currentTheme} setCurrentTheme={setCurrentTheme} />
        </div>
    );
}
