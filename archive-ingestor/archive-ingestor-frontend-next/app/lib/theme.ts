import { NextFont } from "next/dist/compiled/@next/font";
import { Noto_Sans, Cabin_Sketch } from "next/font/google"

// Fonts
export const notoSans: NextFont = Noto_Sans({
    weight: "400",
    subsets: ["latin"],
});

export const ThemeFontTitle: NextFont = Cabin_Sketch({
    weight: "400",
    subsets: ["latin"],
});

// Colors
export const themeColorRed: string = "bg-red-800";
export const themeColorBlack: string = "bg-neutral-950";
