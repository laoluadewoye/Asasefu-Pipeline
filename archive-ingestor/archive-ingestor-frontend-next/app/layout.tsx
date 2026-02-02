import type { Metadata } from "next";
import "./globals.css";
import Header from "./ui/Header";
import ThemedBody from "./ui/ThemedBody";

export const metadata: Metadata = {
    title: { default: "Archive Ingestor", template: "%s | Archive Ingestor" },
};

export default function RootLayout({children,}: Readonly<{children: React.ReactNode;}>) {
    return (
        <html lang="en">
            <body className="antialiased">
                <Header />
                <ThemedBody>{children}</ThemedBody>
                {/*Add a footer here*/}
            </body>
        </html>
    );
}
