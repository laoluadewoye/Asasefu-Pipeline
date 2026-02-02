import { NextFont } from "next/dist/compiled/@next/font";
import { Noto_Sans, Cabin_Sketch } from "next/font/google"
import { ThemeColors } from "./schema";

// Fonts
export const ThemePrimaryFont: NextFont = Noto_Sans({
    weight: "400",
    subsets: ["latin"],
});

export const ThemeTitleFont: NextFont = Cabin_Sketch({
    weight: "400",
    subsets: ["latin"],
});

// Colors
export const headerBgColor: string = "bg-red-800";
export const headerTextColor: string = "text-rose-100";
export const headerLinkBgColor: string = "bg-rose-200";
export const headerLinkTextColor: string = "text-red-800";
export const headerLinkOutlineColor: string = "outline-neutral-950";

const lightTheme: ThemeColors = {
    isDark: false,
    mainBgColor: "bg-rose-100",
    mainTextColor: "text-neutral-950",
    primaryBgColor: "bg-red-800",
    primaryTextColor: "text-red-800"
};

const darkTheme: ThemeColors = {
    isDark: true,
    mainBgColor: "bg-neutral-950",
    mainTextColor: "text-rose-100",
    primaryBgColor: "bg-red-800",
    primaryTextColor: "text-red-800"
};

export const DARK: string = "dark";
export const LIGHT: string = "light";

export function getThemeColors(theme: string): ThemeColors {
    return theme === DARK ? darkTheme : lightTheme;
}
