import Link from "next/link";
import { ThemeTitleFont, ThemePrimaryFont, headerBgColor, headerTextColor, headerLinkBgColor, headerLinkTextColor, headerLinkOutlineColor } from "../theme"

export default function Header() {
    const linkClasses: string = `${headerLinkBgColor} ${headerLinkTextColor} ${headerLinkOutlineColor}`

    return (
        <div id="header" className={`${ThemeTitleFont.className} ${headerBgColor} ${headerTextColor}`}>
            <h1>Archive Ingestor!</h1>
            <h2>Yet another web app leveraging Next.js and Redis...</h2>
            <span className={`${ThemePrimaryFont.className} mt-4`}>
                <Link href="/" className={linkClasses}>Home</Link>
                <Link href="/about" className={linkClasses}>About</Link>
                <Link href="/eula" className={linkClasses}>EULA</Link>
                <Link href="/instructions" className={linkClasses}>Instructions</Link>
                <Link href="/parser" className={linkClasses}>Parser</Link>
                <Link href="/results" className={linkClasses}>Results</Link>
                <Link href="/pipeline" className={linkClasses}>Pipeline</Link>
            </span>
        </div>
    );
}
