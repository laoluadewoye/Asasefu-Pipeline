import { ArchiveChapterData } from "./archive-chapter-data";

export class ArchiveChapterResultUnit {
    id!: string;
    nickname!: string;
    data!: ArchiveChapterData;

    constructor(obj: any) {
        obj && Object.assign(this, obj);
    }
}
