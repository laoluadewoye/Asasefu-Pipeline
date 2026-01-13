export class ArchiveCommentData {
    creationTimestamp!: string;
    creationHash!: string;
    user!: string;
    posted!: string;
    text!: string[];
    depth!: number;
    page!: number;
    replies!: ArchiveCommentData[];

    constructor(obj: any) {
        obj && Object.assign(this, obj);
    }
}
