import { inject, Injectable, DOCUMENT } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ArchiveServerResponseData } from '../models/archive-server-response-data';
import { Subject, Subscription } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { IMessage, RxStomp } from '@stomp/rx-stomp';
import { ArchiveSTOMPConfig } from '../models/archive-stomp-config';

@Injectable({
  providedIn: 'root',
})
export class ArchiveSessionGetService {
    // Http client
    document: Document = inject(DOCUMENT);
    httpClient: HttpClient = inject(HttpClient);
    baseHref!: string;

    // Websocket client
    stompClient: RxStomp = new RxStomp();
    stompSubject: Subject<ArchiveServerResponseData> = new Subject<ArchiveServerResponseData>();
    stompSubscription!: Subscription;
    
    constructor() {
        // Create base reference
        this.baseHref = this.document.location.pathname;

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
            this.baseHref + `api/v1/parse/session/${sessionId}`
        );
    }
    
    getSessionInformationLive(sessionId: string) {
        // Start the feed
        this.httpClient.get<ArchiveServerResponseData>(
            this.baseHref + `api/v1/parse/session/${sessionId}/live`
        ).pipe(
            catchError((err) => {
                console.log(err);
                throw err;
            })
        ).subscribe((result) => {
            // Publish result to STOMP subject
            this.stompSubject.next(result);

            // Retrieve STOMP settings
            let stompConfig: ArchiveSTOMPConfig = JSON.parse(result.parseResult);

            // Reconfigure STOMP client
            this.stompClient.deactivate();
            this.stompClient.configure({
                brokerURL: `ws://localhost:${stompConfig.port}${stompConfig.endpointURL}`,
                reconnectDelay: 0,
                debug: (msg) => console.log(new Date(), msg)
            });
            this.stompClient.activate();

            // Create a subscription to live feed
            this.stompSubscription = this.stompClient.watch(
                {destination: `${stompConfig.topicURL}/${sessionId}`}
            ).subscribe((msg: IMessage) => {
                let responseData: ArchiveServerResponseData = JSON.parse(msg.body);
                this.stompSubject.next(responseData);
            });
        });

        // Return the subject as an observable
        return this.stompSubject.asObservable();
    }

    unsubscribeFromStomp() {
        this.stompSubscription.unsubscribe();
    }
}
