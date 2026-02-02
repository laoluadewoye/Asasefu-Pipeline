import { Metadata } from "next";
import { getCurrentTheme } from "../lib/server/redis";
import { ThemeColors } from "../lib/server/schema";
import { getThemeColors, LIGHT } from "../lib/server/theme";

export const metadata: Metadata = {
    title: "Pipeline | Archive Ingestor",
    description: "Advanced Analytics page for my Archive Ingestor front end."
};

export default async function Page() {
    // Get current colors
    const currentTheme = await getCurrentTheme();
    const currentThemeColors: ThemeColors = currentTheme ? getThemeColors(currentTheme) : getThemeColors(LIGHT);

    return (
        <div id="pipeline" className={`${currentThemeColors.mainBgColor} ${currentThemeColors.mainTextColor}`}>Hello Pipeline</div>
    );
}
