import { useState } from "react";
import Header from "./ui/header";

export default function Home() {
    const [theme, setTheme] = useState('');

    return (
        <div id="home">
            <Header />
        </div>
    );
}
