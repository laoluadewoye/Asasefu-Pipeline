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
        <div id="eula" className={`${currentThemeColors.mainBgColor} ${currentThemeColors.mainTextColor}`}>
            <h2 className="underline">End User License Agreement (EULA)</h2>
            <p className="justify-self-center italic">
                Last updated: February 2nd, 2026
            </p>
            <p className="justify-self-center italic">
                Licensed under the GNU General Public License v3.0 (GPLv3)
            </p>

            <h3>License Grant</h3>
            <p>
                The Asasefu Project and its Archive Ingestor Frontend are freely available under 
                the <strong>GNU General Public License version 3.0 (GPLv3)</strong>. 
                By using this software, you are granted the following rights under the GPLv3:
            </p>
            <ul>
                <li>
                The right to use, copy, modify, distribute, 
                and share the software in any form.
                </li>
                <li>
                The right to adapt the software for educational, 
                research, or personal non-commercial use.
                </li>
                <li>
                The right to redistribute modified versions, 
                provided that all derivative works are also licensed under GPLv3.
                </li>
            </ul>
            <p>
                This license does not grant any rights to the fanfiction content 
                hosted on platforms such as Archive of Our Own, Wattpad, 
                Fanfiction.net, or Quotev. Access to or use of such content is 
                governed by the terms of those platforms and the original authors' rights.
            </p>
            
            <h3>Use of Fanfiction Content</h3>
            <p>
                The Asasefu Project may ingest, process, and display 
                fanfiction content from public repositories 
                (e.g., Archive of Our Own, Wattpad, Fanfiction.net, or Quotev). 
                By using this service, you acknowledge and agree that:
            </p>
            <ul>
                <li>
                    No commercial use of the fanfiction content (including derivatives, 
                    summaries, or AI-generated outputs) is permitted. 
                    This includes, but is not limited to: selling datasets or summaries 
                    to third parties, training AI models to generate monetized content, 
                    or using fanfiction corpora to build predictive analytics for 
                    entertainment or market research.
                </li>
                <li>
                    You shall not use the content for any activity that directly 
                    benefits a commercial enterprise or generates profit.
                </li>
                <li>
                    You are responsible for ensuring that your use of the software and 
                    its outputs does not infringe upon the intellectual property or 
                    privacy rights of authors.
                </li>
            </ul>
            
            <h3>Responsibilities of Users</h3>
            <p>
                You agree to use the software only for <strong>non-commercial, educational, or personal research purposes</strong>. 
                You must not:
            </p>
            <ul>
                <li>Repurpose, distribute, or redistribute fanfiction content in any form for commercial gain.</li>
                <li>
                    Use the software to train AI models that generate fanfiction for 
                    monetized products, advertising, or subscription services.
                </li>
                <li>Engage in any activity that could be interpreted as exploiting or profiting from fanfiction content.</li>
            </ul>
            
            <h3>No Liability for Downstream Use</h3>
            <p>
                The developers and maintainers of the Asasefu Project do not assume liability 
                for any use, modification, or downstream application of the software 
                or its outputs, including AI-generated content. We make no warranties, 
                express or implied, regarding the accuracy, completeness, or 
                legality of the content processed by the tool. Users are solely responsible 
                for the lawful and ethical use of any data or output generated through this service.
            </p>
            
            <h3>Compliance with Laws</h3>
            <p>This EULA is subject to applicable laws, including but not limited to:</p>
            <ul>
                <li>The <strong>Digital Millennium Copyright Act (DMCA)</strong> (US)</li>
                <li>The <strong>General Data Protection Regulation (GDPR)</strong> (EU)</li>
                <li>International copyright and fair use principles</li>
            </ul>
            <p>All use of fanfiction content must comply with applicable copyright, privacy, and ethical standards.</p>
            
            <h3>Contact</h3>
            <p>
                For questions, concerns, or legal inquiries about this EULA, please
                contact: <strong>Laolu Ade (laoluadewoye@gmail.com)</strong>.
            </p>
        </div>
    );
}
