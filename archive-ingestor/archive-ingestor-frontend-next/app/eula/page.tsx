import { Metadata } from "next";
import { getCurrentTheme } from "../lib/server/redis";
import { ThemeColors } from "../lib/server/schema";
import { getThemeColors, LIGHT } from "../lib/server/theme";

export const metadata: Metadata = {
    title: "EULA | Archive Ingestor",
    description: "End User License Agreement for my Archive Ingestor application."
};

export default async function Page() {
    // Get current colors
    const currentTheme = await getCurrentTheme();
    const currentThemeColors: ThemeColors = currentTheme ? getThemeColors(currentTheme) : getThemeColors(LIGHT);

    return (
        <div id="eula" className={`${currentThemeColors.mainBgColor} ${currentThemeColors.mainTextColor}`}>Hello EULA</div>
    );
}
