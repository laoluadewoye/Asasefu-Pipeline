import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { TestAPIInfo } from '../models/test_api_info';

@Injectable({
  providedIn: 'root',
})
export class TestAPIService {
    httpClient = inject(HttpClient)

    getArchiveIngestorTestAPI() {
        return this.httpClient.get<TestAPIInfo>("http://localhost:8080/api/v1")
    }
}
