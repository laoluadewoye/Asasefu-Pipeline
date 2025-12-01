package com.laoluade.ingestor.ao3.server.models;

public class ArchiveIngestorInfo {
    private String archiveIngestorVersion;
    private String latestOTWArchiveSupported;

    public ArchiveIngestorInfo(String archiveIngestorVersion, String latestOTWArchiveSupported) {
        this.archiveIngestorVersion = archiveIngestorVersion;
        this.latestOTWArchiveSupported = latestOTWArchiveSupported;
    }

    public void setArchiveIngestorVersion(String archiveIngestorVersion) { this.archiveIngestorVersion = archiveIngestorVersion; }

    public String getArchiveIngestorVersion() { return this.archiveIngestorVersion; }

    public void setLatestOTWArchiveSupported(String latestOTWArchiveSupported) { this.latestOTWArchiveSupported = latestOTWArchiveSupported; }

    public String getLatestOTWArchiveSupported() { return this.latestOTWArchiveSupported; }
}
