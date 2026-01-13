import { inject, Injectable, DOCUMENT } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ArchiveServerSpecData } from '../models/archive-server-spec-data';

@Injectable({
  providedIn: 'root',
})
export class ArchiveServerSpecService {
    document: Document = inject(DOCUMENT);
    httpClient: HttpClient = inject(HttpClient);
    specURL!: string;

    constructor() {
        this.specURL = this.document.location.pathname + "api/v1/spec";
    }

    getArchiveServerSpecData() {
        return this.httpClient.get<ArchiveServerSpecData>(this.specURL);
    }
}
