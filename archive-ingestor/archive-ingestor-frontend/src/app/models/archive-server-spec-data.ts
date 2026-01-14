export class ArchiveServerSpecData {
    archiveIngestorVersion!: string;
    latestOTWArchiveVersion!: string;

    constructor(obj: any) {
        obj && Object.assign(this, obj);
    }
}
