package com.laoluade;

import java.io.IOException;

public class Main {
    static void main() throws IOException {
        ArchiveIngestor ai = new ArchiveIngestor();
        System.out.println(ai.storyLinks.toString(4));
        System.out.println(ai.versionTable.toString(4));
    }
}
