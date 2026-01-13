export class ArchiveBaseRef {
    baseRef!: string;

    constructor(document: Document) {
        this.baseRef = document.location.pathname;
    }
}
