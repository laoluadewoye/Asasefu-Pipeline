import { inject, Injectable, DOCUMENT } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ArchiveServerResponseData } from '../models/archive-server-response-data';

@Injectable({
  providedIn: 'root',
})
export class ArchiveSessionCancelService {
    document: Document = inject(DOCUMENT);
    httpClient: HttpClient = inject(HttpClient);
    baseHref!: string;

    constructor() {
        this.baseHref = this.document.location.href;
    }

    getCancelSessionResponse(sessionId: string) {
        return this.httpClient.get<ArchiveServerResponseData>(
            this.baseHref + `api/v1/parse/session/${sessionId}/cancel`
        );
    }
}
