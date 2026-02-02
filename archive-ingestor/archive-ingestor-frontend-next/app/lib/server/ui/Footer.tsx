import Link from "next/link";
import { headerBgColor, headerLinkBgColor, headerLinkOutlineColor, headerLinkTextColor, headerTextColor, ThemePrimaryFont } from "../theme";

export default function Footer() {
    const archiveIngestorVersion: string = "[Not Available]";
    const latestOTWArchiveVersion: string = "[Not Available]";
    const linkClasses: string = `${headerLinkBgColor} ${headerLinkTextColor} ${headerLinkOutlineColor}`

    return (
        <div id="footer" className={`${ThemePrimaryFont.className} ${headerBgColor} ${headerTextColor}`}>
            <div id="footer-main" className="grow-3">
                <p>
                This app is running Archive Ingestor Version {archiveIngestorVersion}.
                The latest version of OTW Archive that this app supports is {latestOTWArchiveVersion}.
                </p>
                <p>
                    Site development and design © 2025 - 2026 Laolu Ade.
                    This project is backed by a GNU General Public License Version 3,
                    so if you are going to fork my work just keep it publically available so we can all have fun!
                </p>
                <p>Links to my Github and the project repos are to the right (or bottom if on mobile).</p>
            </div>
            <div id="footer-links" className="grow">
                <Link href="https://github.com/laoluadewoye" target="_blank" className={linkClasses}>
                    Laolu Ade's GitHub
                </Link>
                <Link 
                    href="https://github.com/laoluadewoye/Asasefu-Pipeline"
                    target="_blank"
                    className={linkClasses}
                >
                    Asasefu Pipeline Project
                </Link>
                <Link 
                    href="https://github.com/laoluadewoye/Asasefu-Pipeline/tree/main/archive-ingestor"
                    target="_blank" 
                    className={linkClasses}
                >
                    Archive Ingestor Project
                </Link>
            </div>
        </div>
    );
}
