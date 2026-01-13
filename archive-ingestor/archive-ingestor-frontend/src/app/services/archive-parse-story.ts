import { inject, Injectable, DOCUMENT } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ArchiveServerRequestData } from '../models/archive-server-request-data';
import { ArchiveServerResponseData } from '../models/archive-server-response-data';
import { ArchiveBaseRef } from './archive-base-ref';

@Injectable({
  providedIn: 'root',
})
export class ArchiveParseStoryService {
    document: Document = inject(DOCUMENT);
    httpClient: HttpClient = inject(HttpClient);
    parseStoryURL!: string; 
    
    constructor() {
        let baseRef: ArchiveBaseRef = new ArchiveBaseRef(this.document);
        this.parseStoryURL = baseRef.baseRef + "api/v1/parse/story";
    }

    postParseStoryRequest(archiveServerRequestData: ArchiveServerRequestData) {
        return this.httpClient.post<ArchiveServerResponseData>(
            this.parseStoryURL, archiveServerRequestData
        );
    }
}
