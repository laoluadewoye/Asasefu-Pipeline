import { ThemeFontTitle, themeColorRed, themeColorBlack } from "../lib/theme"

function Dropdown() {
    return (
        <div id="dropdown">
            <button className={`${themeColorBlack}`}>Settings</button>
        </div>
    )
}

export default function Header() {
    return (
        <div id="header" className={`${themeColorRed} p-5 ${ThemeFontTitle.className}`}>
            <h1>Archive Ingestor!</h1>
            <Dropdown />
        </div>
    )
}
