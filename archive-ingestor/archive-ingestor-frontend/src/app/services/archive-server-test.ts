import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ArchiveServerTestData } from '../models/archive-server-test-data';

@Injectable({
  providedIn: 'root',
})
export class ArchiveServerTestService {
    httpClient: HttpClient = inject(HttpClient);

    getArchiveServerTestData() {
        return this.httpClient.get<ArchiveServerTestData>("http://localhost:8080/api/v1");
    }
}
