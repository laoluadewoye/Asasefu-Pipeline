import { inject, Injectable, DOCUMENT } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ArchiveServerTestData } from '../models/archive-server-test-data';
import { ArchiveBaseRef } from './archive-base-ref';

@Injectable({
  providedIn: 'root',
})
export class ArchiveServerTestService {
    document: Document = inject(DOCUMENT);
    httpClient: HttpClient = inject(HttpClient);
    testURL!: string;

    constructor() {
        let baseRef: ArchiveBaseRef = new ArchiveBaseRef(this.document);
        this.testURL = baseRef.baseRef + "api/v1";
    }

    getArchiveServerTestData() {
        return this.httpClient.get<ArchiveServerTestData>(this.testURL);
    }
}
