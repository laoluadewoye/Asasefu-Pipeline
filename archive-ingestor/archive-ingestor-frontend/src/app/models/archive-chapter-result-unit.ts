import { ArchiveChapterData } from "./archive-chapter-data";
import { ArchiveMetadataResultUnit } from "./archive-metadata-result-unit";

export class ArchiveChapterResultUnit {
    id!: string;
    nickname!: string;
    data!: ArchiveChapterData;
    storyMetadata?: ArchiveMetadataResultUnit;

    constructor(obj: any) {
        obj && Object.assign(this, obj);
    }
}
