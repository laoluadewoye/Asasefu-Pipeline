import { ArchiveChapterData } from "./archive-chapter-data";
import { ArchiveStoryInfoData } from "./archive-story-info-data";

export class ArchiveResultUnit {
    id!: string;
    nickname!: string;
    data!: ArchiveStoryInfoData | ArchiveChapterData;

    constructor(obj: any) {
        obj && Object.assign(this, obj);
    }
}
