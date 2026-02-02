"use client"

import { useState } from "react";
import { LIGHT, ClientThemeContext } from "../theme";
import ThemeSwitcher from "./ThemeSwitcher";
import { Dispatch, SetStateAction } from "react";

export default function ThemedClientWrapper({children,}: Readonly<{children: React.ReactNode;}>) {
    const [currentTheme, setCurrentTheme]: [string, Dispatch<SetStateAction<string>>] = useState<string>(LIGHT);

    return (
        <div id="themed-body">
            <ClientThemeContext.Provider value={currentTheme}>
                {children}
            </ClientThemeContext.Provider>
            <ThemeSwitcher currentTheme={currentTheme} setCurrentTheme={setCurrentTheme} />
        </div>
    );
}

/*
<Suspense fallback={<button id="theme-switcher" disabled={true}>Detecting Theme...</button>}>
    <ThemeSwitcher currentTheme={currentTheme} setCurrentTheme={setCurrentTheme} />
</Suspense>
*/
