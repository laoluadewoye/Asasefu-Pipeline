import { inject, Injectable } from '@angular/core';
import { VersionInfo } from '../models/version.type';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class Version {
    httpClient = inject(HttpClient);
    
    getArchiveIngestorInfo() {
        return this.httpClient.get<VersionInfo>("http://localhost:8080/api/v1/info")
    }
}
