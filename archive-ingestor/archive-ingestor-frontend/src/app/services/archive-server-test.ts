import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { ArchiveServerTestData } from '../models/archive-server-test-data';

@Injectable({
  providedIn: 'root',
})
export class ArchiveServerTestService {
    httpClient = inject(HttpClient)

    getArchiveIngestorTestData() {
        return this.httpClient.get<ArchiveServerTestData>("http://localhost:8080/api/v1")
    }
}
