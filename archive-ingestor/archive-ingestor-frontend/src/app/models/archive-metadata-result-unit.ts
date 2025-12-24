import { ArchiveStoryInfoData } from "./archive-story-info-data";

export class ArchiveMetadataResultUnit {
    id!: string;
    nickname!: string;
    data!: ArchiveStoryInfoData;

    constructor(obj: any) {
        obj && Object.assign(this, obj);
    }
}
