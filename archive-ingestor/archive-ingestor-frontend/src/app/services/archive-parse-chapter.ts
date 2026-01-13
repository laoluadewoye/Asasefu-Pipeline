import { inject, Injectable, DOCUMENT } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ArchiveServerRequestData } from '../models/archive-server-request-data';
import { ArchiveServerResponseData } from '../models/archive-server-response-data';
import { ArchiveBaseRef } from './archive-base-ref';

@Injectable({
  providedIn: 'root',
})
export class ArchiveParseChapterService {
    document: Document = inject(DOCUMENT);
    httpClient: HttpClient = inject(HttpClient);
    parseChapterURL!: string;

    constructor() {
        let baseRef: ArchiveBaseRef = new ArchiveBaseRef(this.document);
        this.parseChapterURL = baseRef.baseRef + "api/v1/parse/chapter";
    }

    postParseChapterRequest(archiveServerRequestData: ArchiveServerRequestData) {
        return this.httpClient.post<ArchiveServerResponseData>(
            this.parseChapterURL, archiveServerRequestData
        );
    }
}
