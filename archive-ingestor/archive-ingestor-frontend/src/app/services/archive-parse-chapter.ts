import { inject, Injectable, DOCUMENT } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ArchiveServerRequestData } from '../models/archive-server-request-data';
import { ArchiveServerResponseData } from '../models/archive-server-response-data';

@Injectable({
  providedIn: 'root',
})
export class ArchiveParseChapterService {
    document: Document = inject(DOCUMENT);
    httpClient: HttpClient = inject(HttpClient);
    parseChapterURL!: string;

    constructor() {
        this.parseChapterURL = this.document.location.pathname + "api/v1/parse/chapter";
    }

    postParseChapterRequest(archiveServerRequestData: ArchiveServerRequestData) {
        return this.httpClient.post<ArchiveServerResponseData>(
            this.parseChapterURL, archiveServerRequestData
        );
    }
}
