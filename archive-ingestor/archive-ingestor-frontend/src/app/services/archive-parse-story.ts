import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ArchiveServerRequestData } from '../models/archive-server-request-data';
import { ArchiveServerResponseData } from '../models/archive-server-response-data';

@Injectable({
  providedIn: 'root',
})
export class ArchiveParseStoryService {
    httpClient = inject(HttpClient);

    postParseStoryRequest(archiveServerRequestData: ArchiveServerRequestData) {
        return this.httpClient.post<ArchiveServerResponseData>(
            "http://localhost:8080/api/v1/parse/story", archiveServerRequestData
        );
    }
}
