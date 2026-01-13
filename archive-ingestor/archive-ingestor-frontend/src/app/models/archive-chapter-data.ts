import { ArchiveCommentData } from "./archive-comment-data";
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
    foundComments!: ArchiveCommentData[];

    constructor(obj: any) {
        obj && Object.assign(this, obj);
    }
}
