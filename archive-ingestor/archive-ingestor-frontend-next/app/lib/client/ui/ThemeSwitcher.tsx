"use client"

import { DARK, getThemeColors, LIGHT } from "../theme";
import { RedisResponse, ThemeColors } from "../schema";
import { Dispatch, SetStateAction, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AppRouterInstance } from "next/dist/shared/lib/app-router-context.shared-runtime";

export default function ThemeSwitcher(
    {currentTheme, setCurrentTheme}: 
    {currentTheme: string, setCurrentTheme: Dispatch<SetStateAction<string>>}
) { 
    // Define constants
    const router: AppRouterInstance = useRouter();
    const buttonThemeColors: ThemeColors = getThemeColors(DARK);

    // Define theme context updating
    const updateClientThemeContext = async () => {
        fetch("/api/v1/get-current-theme").then((res) => res.json()).then((res: RedisResponse) => {
            let t = res.response;
            t ? setCurrentTheme(t) : setCurrentTheme(LIGHT);
        });
    }

    // Update theme context
    const [mounted, setMounted]: [boolean, Dispatch<SetStateAction<boolean>>] = useState<boolean>(false);
    useEffect(() => {
        updateClientThemeContext().finally(() => setMounted(true));
    }, [])

    // Return placeholder if needed
    if (!mounted) {
        return (
            <button
                id="theme-switcher"
                className={`${buttonThemeColors.primaryBgColor} ${buttonThemeColors.mainTextColor}`} 
                disabled={true}
            >
                Detecting Theme...
            </button>
        )
    }

    // Define render variables
    let switchEvent: () => Promise<void>;
    let btnText: string;

    // Set variables
    if (currentTheme && currentTheme === DARK) {
        switchEvent = async () => {
            await fetch("/api/v1/set-theme-light");
            await updateClientThemeContext();
            router.refresh();
        }
        btnText = "Switch to Light Theme";
    }
    else {
        switchEvent = async () => {
            await fetch("/api/v1/set-theme-dark");
            await updateClientThemeContext();
            router.refresh();
        }
        btnText = "Switch to Dark Theme";
    }

    // Render button
    return (
        <button
            id="theme-switcher"
            className={`${buttonThemeColors.primaryBgColor} ${buttonThemeColors.mainTextColor}`} 
            onClick={switchEvent}
        >
            {btnText}
        </button>
    );
}
