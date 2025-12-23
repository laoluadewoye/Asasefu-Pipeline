import { ArchiveChapterData } from "./archive-chapter-data";
import { ArchiveStoryData } from "./archive-story-data";

export class ArchiveSessionData {
    id!: string;
    nickname!: string;
    outcome!: string;
    data?: ArchiveStoryData | ArchiveChapterData; 
}
