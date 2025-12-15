export class ArchiveServerResponseData {
    sessionId!: string;
    sessionNickname!: string;
    sessionFinished!: boolean;
    sessionCanceled!: boolean;
    sessionException!: boolean;
    parseChaptersCompleted!: number;
    parseChaptersTotal!: number;
    parseResult!: string;
    responseMessage!: string;
}
