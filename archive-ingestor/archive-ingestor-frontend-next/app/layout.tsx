import type { Metadata } from "next";
import "./globals.css";
import Header from "./lib/server/ui/Header";
import ThemedClientWrapper from "./lib/client/ui/ThemedClientWrapper";

export const metadata: Metadata = {
    title: "Archive Ingestor",
    description: "Landing page for my Archive Ingestor front end."
};

export default function RootLayout({children,}: Readonly<{children: React.ReactNode;}>) {
    return (
        <html lang="en">
            <body className="antialiased">
                <Header />
                <ThemedClientWrapper>
                    {children}
                </ThemedClientWrapper>
                {/*Add a footer here*/}
            </body>
        </html>
    );
}
