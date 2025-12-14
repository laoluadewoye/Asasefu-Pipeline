import { inject, Injectable } from '@angular/core';
import { ArchiveServerSpecData } from '../models/archive-server-spec-data';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class ArchiveServerSpecService {
    httpClient = inject(HttpClient);
    
    getArchiveServerSpecData() {
        return this.httpClient.get<ArchiveServerSpecData>("http://localhost:8080/api/v1/spec")
    }
}
