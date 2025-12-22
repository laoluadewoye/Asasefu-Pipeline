import { ArchiveCommentsData } from "./archive-comments-data";
import { ArchiveStoryInfoData } from "./archive-story-info-data";

export class ArchiveChapterData {
    creationTimestamp!: string;
    creationHash!: string;
    parentArchiveStoryInfo?: ArchiveStoryInfoData;
    pageTitle!: string;
    pageLink!: string;
    chapterTitle!: string;
    summary!: string[];
    startNotes!: string[];
    endNotes!: string[];
    paragraphs!: string[];
    foundComments!: ArchiveCommentsData;

    constructor(obj: any) {
        obj && Object.assign(this, obj);
    }
}
