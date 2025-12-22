import { ArchiveChapterData } from "./archive-chapter-data";
import { ArchiveStoryInfoData } from "./archive-story-info-data";

export class ArchiveStoryData {
    creationTimestamp!: string;
    creationHash!: string;
    archiveStoryInfo!: ArchiveStoryInfoData;
    archiveChapters!: ArchiveChapterData[];

    constructor(obj: any) {
        obj && Object.assign(this, obj);
    }
}
