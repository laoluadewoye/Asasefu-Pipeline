import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ArchiveServerResponseData } from '../models/archive-server-response-data';

@Injectable({
  providedIn: 'root',
})
export class ArchiveSessionCancelService {
    httpClient: HttpClient = inject(HttpClient);

    getCancelSessionResponse(sessionId: string) {
        return this.httpClient.get<ArchiveServerResponseData>(
            `http://localhost:8080/api/v1/parse/session/${sessionId}/cancel`
        );
    }
}
