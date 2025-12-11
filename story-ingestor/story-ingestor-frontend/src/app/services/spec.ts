import { inject, Injectable } from '@angular/core';
import { SpecInfo } from '../models/spec_info';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class SpecService {
    httpClient = inject(HttpClient);
    
    getArchiveServerSpecData() {
        return this.httpClient.get<SpecInfo>("http://localhost:8080/api/v1/spec")
    }
}
