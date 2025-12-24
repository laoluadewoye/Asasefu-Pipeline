import { ArchiveStoryData } from "./archive-story-data";

export class ArchiveStoryResultUnit {
    id!: string;
    nickname!: string;
    data!: ArchiveStoryData;

    constructor(obj: any) {
        obj && Object.assign(this, obj);
    }
}
