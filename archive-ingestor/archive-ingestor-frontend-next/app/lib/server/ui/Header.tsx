import Link from "next/link";
import { headerBgColor, headerBtnBgColor, headerbtnTextColor, headerTextColor, ThemeTitleFont, ThemePrimaryFont } from "../theme"

export default function Header() {
    return (
        <div id="header" className={`${ThemeTitleFont.className} ${headerBgColor} ${headerTextColor}`}>
            <h1>Archive Ingestor!</h1>
            <h2>Yet another web app leveraging Next.js and Redis...</h2>
            <span className={`${ThemePrimaryFont.className}`}>
                <button className={`${headerBtnBgColor} ${headerbtnTextColor}`}>
                    <Link href="/">Home</Link>
                </button>
                <button className={`${headerBtnBgColor} ${headerbtnTextColor}`}>
                    <Link href="/about">About</Link>
                </button>
                <button className={`${headerBtnBgColor} ${headerbtnTextColor}`}>
                    <Link href="/eula">EULA</Link>
                </button>
                <button className={`${headerBtnBgColor} ${headerbtnTextColor}`}>
                    <Link href="/instructions">Instructions</Link>
                </button>
                <button className={`${headerBtnBgColor} ${headerbtnTextColor}`}>
                    <Link href="/parser">Parser</Link>
                </button>
                <button className={`${headerBtnBgColor} ${headerbtnTextColor}`}>
                    <Link href="/results">Results</Link>
                </button>
                <button className={`${headerBtnBgColor} ${headerbtnTextColor}`}>
                    <Link href="/pipeline">Pipeline</Link>
                </button>
            </span>
        </div>
    );
}
