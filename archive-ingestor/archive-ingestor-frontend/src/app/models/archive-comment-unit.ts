export class ArchiveCommentUnit {
    user!: string;
    posted!: string;
    text!: string[];
    depth!: number;
    replies!: ArchiveCommentUnit[];

    constructor(obj: any) {
        obj && Object.assign(this, obj);
    }
}
