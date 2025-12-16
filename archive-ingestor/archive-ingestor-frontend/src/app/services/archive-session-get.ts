import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ArchiveServerResponseData } from '../models/archive-server-response-data';
import { BehaviorSubject, interval, of } from 'rxjs';
import { retry, catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ArchiveSessionGetService {
    httpClient: HttpClient = inject(HttpClient);
    archiveServerResponseSubject$ = new BehaviorSubject<ArchiveServerResponseData | null>(null);
    callIntervalMilli: number = 200;

    getGetSessionResponse(sessionId: string) {
        return this.httpClient.get<ArchiveServerResponseData>(
            `http://localhost:8080/api/v1/parse/session/${sessionId}`
        );
    }

    monitorSessionProgress(sessionId: string) {
        interval(this.callIntervalMilli).pipe(
            catchError(() => of(null))
        ).subscribe(() => {
            this.getGetSessionResponse(sessionId).pipe(
                catchError((err) => {
                    console.log(err);
                    return of(null);
                })
            ).subscribe((result) => {
                this.archiveServerResponseSubject$.next(result);
            });
        });

        return this.archiveServerResponseSubject$.asObservable();
    }
}
