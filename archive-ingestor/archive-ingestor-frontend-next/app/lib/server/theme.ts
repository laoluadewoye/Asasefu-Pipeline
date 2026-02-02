import { NextFont } from "next/dist/compiled/@next/font";
import { Noto_Sans, Cabin_Sketch } from "next/font/google"

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
export const headerBtnBgColor: string = "bg-rose-200";
export const headerbtnTextColor: string = "text-red-800";
