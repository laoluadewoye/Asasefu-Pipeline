import { ArchiveSessionData } from "./archive-session-data";

export class ArchiveCompletedSession {
    hash!: string;
    data!: ArchiveSessionData;

    constructor(obj: any) {
        obj && Object.assign(this, obj);
    }
}