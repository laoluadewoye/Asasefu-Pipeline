import { ThemeColors } from "./lib/server/schema";
import { getThemeColors, LIGHT } from "./lib/server/theme";
import { getCurrentTheme } from "./lib/server/redis";

export default async function Page() {
    // Get current colors
    const currentTheme = await getCurrentTheme();
    const currentThemeColors: ThemeColors = currentTheme ? getThemeColors(currentTheme) : getThemeColors(LIGHT);

    return (
        <div id="home" className={`${currentThemeColors.mainBgColor} ${currentThemeColors.mainTextColor}`}>Hello Index</div>
    );
}
