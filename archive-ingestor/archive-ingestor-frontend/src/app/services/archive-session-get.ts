import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ArchiveServerResponseData } from '../models/archive-server-response-data';
import { generate, interval, Observable, of, Subject, Subscription } from 'rxjs';
import { catchError, takeUntil, takeWhile, timeout } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ArchiveSessionGetService {
    // Http client
    httpClient: HttpClient = inject(HttpClient);

    // Monitoring stuff
    monitoringSubject: Subject<ArchiveServerResponseData> = new Subject<ArchiveServerResponseData>();
    monitoringInterval!: Observable<number>;
    monitoringSubscription!: Subscription;

    // Stopping object
    stopSubject: Subject<any> = new Subject();

    lastSessionId: string = "";
    errorMessage: string = "Recieved error result from ArchiveSessionGetService.";

    constructor() {
        // Defines subscription handling
        this.monitoringSubject.subscribe({
            next: (result: ArchiveServerResponseData) => {
                console.log("Obtained result for session" + this.lastSessionId);
                this.lastSessionId = result.sessionId;
            },
            error: (err: ArchiveServerResponseData) => {
                console.log("Obtained error for session " + this.lastSessionId);
                console.log(err);
                this.monitoringSubscription.unsubscribe();
                this.stopSubject.next(0);
            },
            complete: () => {
                console.log("Completed monitoring loop for session " + this.lastSessionId);
            }
        });
    }

    getGetSessionResponse(sessionId: string) {
        return this.httpClient.get<ArchiveServerResponseData>(
            `http://localhost:8080/api/v1/parse/session/${sessionId}`
        );
    }
    
    monitorSessionProgress(sessionId: string, callIntervalMilli: number) {
        // Create starter object
        let monitoringState = {id: sessionId, message: ""};

        // Create generator
        generate({
            initialState: monitoringState,
            condition: (monitoringState) => {
                let isError: boolean = monitoringState.message === this.errorMessage;
                let noSessionId: boolean = monitoringState.id === "";
                let validError: boolean = isError && !noSessionId;
                return !validError;
            },
            iterate: (monitoringState) => {
                // Run session response
                this.getGetSessionResponse(monitoringState.id).pipe(
                    catchError((err) => {
                        // Log the error
                        console.log(err);

                        // Build an exception response
                        let errorResponse: ArchiveServerResponseData = new ArchiveServerResponseData();
                        errorResponse.sessionId = this.lastSessionId;
                        errorResponse.sessionNickname = "";
                        errorResponse.sessionFinished = false;
                        errorResponse.sessionCanceled = false;
                        errorResponse.sessionException = true;
                        errorResponse.parseChaptersCompleted = 0;
                        errorResponse.parseChaptersTotal = 0;
                        errorResponse.parseResult = "";
                        errorResponse.responseMessage = this.errorMessage;

                        // Return that obserable instead
                        return of(errorResponse);
                    })
                ).subscribe((result) => {
                    monitoringState.id = result.sessionId;
                    monitoringState.message = result.responseMessage;
                    this.monitoringSubject.next(result);
                });

                // Return an updated state
                return monitoringState;
            }
        }).subscribe((ms) => console.log(ms));

        // Return the subject as an observable
        return this.monitoringSubject.asObservable();
    }
}


/*
// Wait for a second and set references for the interval subscription
        setTimeout(() => {this.lastSessionId = sessionId;}, 3000)
        this.monitoringInterval = interval(callIntervalMilli)

        // Start subscription
        this.monitoringSubscription = this.monitoringInterval.pipe(
            takeUntil(this.stopSubject)
        ).subscribe(() => {
            this.getGetSessionResponse(sessionId).pipe(
                catchError((err) => {
                    // Log the error
                    console.log(err);

                    // Build an exception response
                    let errorResponse: ArchiveServerResponseData = new ArchiveServerResponseData();
                    errorResponse.sessionId = this.lastSessionId;
                    errorResponse.sessionNickname = "";
                    errorResponse.sessionFinished = false;
                    errorResponse.sessionCanceled = false;
                    errorResponse.sessionException = true;
                    errorResponse.parseChaptersCompleted = 0;
                    errorResponse.parseChaptersTotal = 0;
                    errorResponse.parseResult = "";
                    errorResponse.responseMessage = this.errorMessage;

                    // Return that obserable instead
                    return of(errorResponse);
                })
            ).subscribe((result) => {
                if (result.responseMessage === this.errorMessage) { // Error out and complete
                    this.monitoringSubject.error(result);
                }
                else { // Send the next result
                    this.monitoringSubject.next(result);
                }
            });
        });
*/
