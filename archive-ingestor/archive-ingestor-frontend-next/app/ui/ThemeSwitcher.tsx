"use client"

import { DARK, getThemeColors, LIGHT } from "../lib/client/theme";
import { RedisResponse, ThemeColors } from "../lib/client/schema";
import { Dispatch, SetStateAction } from "react";

export default function ThemeSwitcher(
    {currentTheme, setCurrentTheme}: 
    {currentTheme: string, setCurrentTheme: Dispatch<SetStateAction<string>>}
) {
    // Get current colors
    const currentThemeColors: ThemeColors = getThemeColors(DARK);

    // Define theme updating
    const updateTheme = async () => {
        fetch("/api/v1/get-current-theme").then((res) => res.json()).then((res: RedisResponse) => {
            let t = res.response;
            t ? setCurrentTheme(t) : setCurrentTheme(LIGHT);
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
