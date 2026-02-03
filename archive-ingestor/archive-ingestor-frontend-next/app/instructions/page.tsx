import { Metadata } from "next";
import { getCurrentTheme } from "../lib/server/redis";
import { ThemeColors } from "../lib/server/schema";
import { getThemeColors, LIGHT } from "../lib/server/theme";

export const metadata: Metadata = {
    title: "Instructions | Archive Ingestor",
    description: "Instructions page for my Archive Ingestor front end."
};

export default async function Page() {
    // Get current colors
    const currentTheme = await getCurrentTheme();
    const currentThemeColors: ThemeColors = currentTheme ? getThemeColors(currentTheme) : getThemeColors(LIGHT);

    return (
        <div id="instructions" className={`${currentThemeColors.mainBgColor} ${currentThemeColors.mainTextColor}`}>
            <h2 className="underline">Site Instructions</h2>
        </div>
    );
}
