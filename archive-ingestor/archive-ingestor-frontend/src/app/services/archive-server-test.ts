import { inject, Injectable, DOCUMENT } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ArchiveServerTestData } from '../models/archive-server-test-data';

@Injectable({
  providedIn: 'root',
})
export class ArchiveServerTestService {
    document: Document = inject(DOCUMENT);
    httpClient: HttpClient = inject(HttpClient);
    testURL!: string;

    constructor() {
        this.testURL = this.document.location.href + "api/v1";
    }

    getArchiveServerTestData() {
        return this.httpClient.get<ArchiveServerTestData>(this.testURL);
    }
}
