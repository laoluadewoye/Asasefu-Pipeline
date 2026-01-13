import { inject, Injectable, DOCUMENT } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ArchiveServerResponseData } from '../models/archive-server-response-data';
import { ArchiveBaseRef } from './archive-base-ref';

@Injectable({
  providedIn: 'root',
})
export class ArchiveSessionCancelService {
    document: Document = inject(DOCUMENT);
    httpClient: HttpClient = inject(HttpClient);
    baseRef!: ArchiveBaseRef;

    constructor() {
        this.baseRef = new ArchiveBaseRef(this.document);
    }

    getCancelSessionResponse(sessionId: string) {
        return this.httpClient.get<ArchiveServerResponseData>(
            this.baseRef.baseRef + `api/v1/parse/session/${sessionId}/cancel`
        );
    }
}
