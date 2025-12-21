import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ArchiveServerResponseData } from '../models/archive-server-response-data';
import { Subject, Subscription } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { IMessage, ReconnectionTimeMode, RxStomp } from '@stomp/rx-stomp';

@Injectable({
  providedIn: 'root',
})
export class ArchiveSessionGetService {
    // Http client
    httpClient: HttpClient = inject(HttpClient);

    // Websocket client
    stompClient: RxStomp = new RxStomp();
    stompSubject: Subject<ArchiveServerResponseData> = new Subject<ArchiveServerResponseData>();
    stompSubscription!: Subscription;
    
    constructor() {
        // Configure STOMP client
        this.stompClient.configure({
            brokerURL: "ws://localhost:8080/api/v1/websocket",
            reconnectDelay: 0,
            debug: (msg) => console.log(new Date(), msg)
        });
        this.stompClient.activate();

        // Configure STOMP subject
        this.stompSubject.subscribe({
            next: (result) => {
                console.log("Processing result for " + result.sessionId);
                console.log(result);
            },
            error: (err) => {
                console.log("Processing error for " + err.sessionId);
                console.log(err);
            },
        })
    }

    getSessionInformation(sessionId: string) {
        return this.httpClient.get<ArchiveServerResponseData>(
            `http://localhost:8080/api/v1/parse/session/${sessionId}`
        );
    }
    
    getSessionInformationLive(sessionId: string) {
        // Start the feed
        this.httpClient.get<ArchiveServerResponseData>(
            `http://localhost:8080/api/v1/parse/session/${sessionId}/live`
        ).pipe(
            catchError((err) => {
                console.log(err);
                throw err;
            })
        ).subscribe((result) => this.stompSubject.next(result));

        // Create a subscription to this feed
        this.stompSubscription = this.stompClient.watch(
            {destination: "/api/v1/websocket/topic/get-session-live"}
        ).subscribe((msg: IMessage) => {
            let responseData: ArchiveServerResponseData = JSON.parse(msg.body);
            this.stompSubject.next(responseData);
        });

        // Return the subject as an observable
        return this.stompSubject.asObservable();
    }

    unsubscribeFromStomp() {
        this.stompSubscription.unsubscribe();
    }
}
