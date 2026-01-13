import { DOCUMENT, inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ArchiveServerSpecData } from '../models/archive-server-spec-data';
import { ArchiveBaseRef } from './archive-base-ref';

@Injectable({
  providedIn: 'root',
})
export class ArchiveServerSpecService {
    document: Document = inject(DOCUMENT);
    httpClient: HttpClient = inject(HttpClient);
    specURL!: string;

    constructor() {
        let baseRef: ArchiveBaseRef = new ArchiveBaseRef(this.document);
        this.specURL = baseRef.baseRef + "api/v1/spec";
    }

    getArchiveServerSpecData() {
        return this.httpClient.get<ArchiveServerSpecData>(this.specURL);
    }
}
