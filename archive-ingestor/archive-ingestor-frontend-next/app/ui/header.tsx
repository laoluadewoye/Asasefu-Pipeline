import { headerBgColor, headerBtnBgColor, headerbtnTextColor, headerTextColor, ThemeTitleFont, ThemePrimaryFont } from "../lib/server/theme"

export default function Header() {
    return (
        <div id="header" className={`${ThemeTitleFont.className} ${headerBgColor} ${headerTextColor}`}>
            <h1>Archive Ingestor!</h1>
            <h2>Yet another web app leveraging Next.js and Redis...</h2>
            <span className={`${ThemePrimaryFont.className}`}>
                <button className={`${headerBtnBgColor} ${headerbtnTextColor}`}>About</button>
                <button className={`${headerBtnBgColor} ${headerbtnTextColor}`}>EULA</button>
                <button className={`${headerBtnBgColor} ${headerbtnTextColor}`}>Instructions</button>
                <button className={`${headerBtnBgColor} ${headerbtnTextColor}`}>Parser</button>
                <button className={`${headerBtnBgColor} ${headerbtnTextColor}`}>Results</button>
                <button className={`${headerBtnBgColor} ${headerbtnTextColor}`}>Pipeline</button>
            </span>
        </div>
    );
}
