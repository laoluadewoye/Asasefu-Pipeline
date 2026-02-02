"use client"

import { DARK, getThemeColors, LIGHT } from "../theme";
import { RedisResponse, ThemeColors } from "../schema";
import { Dispatch, SetStateAction } from "react";
import { useRouter } from "next/navigation";

export default function ThemeSwitcher(
    {currentTheme, setCurrentTheme}: 
    {currentTheme: string, setCurrentTheme: Dispatch<SetStateAction<string>>}
) {
    const router = useRouter();
    
    // Get current colors
    const currentThemeColors: ThemeColors = getThemeColors(DARK);

    // Define theme updating
    const updateTheme = async () => {
        fetch("/api/v1/get-current-theme").then((res) => res.json()).then((res: RedisResponse) => {
            let t = res.response;
            t ? setCurrentTheme(t) : setCurrentTheme(LIGHT);
            router.refresh();
        });
    }

    // Define variables
    let switchEvent: () => Promise<void>;
    let btnText: string;

    // Set variables
    if (currentTheme && currentTheme === DARK) {
        switchEvent = async () => {
            await fetch("/api/v1/set-theme-light");
            await updateTheme();
        }
        btnText = "Switch to Light Theme";
    }
    else {
        switchEvent = async () => {
            await fetch("/api/v1/set-theme-dark");
            await updateTheme();
        }
        btnText = "Switch to Dark Theme";
    }

    // Render button
    return (
        <button
            id="theme-switcher"
            className={`${currentThemeColors.primaryBgColor} ${currentThemeColors.mainTextColor}`} 
            onClick={switchEvent}
        >
            {btnText}
        </button>
    );
}
