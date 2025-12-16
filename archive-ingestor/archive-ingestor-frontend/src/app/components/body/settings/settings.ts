import { Component, inject, signal } from '@angular/core';
import { FormControl, Validators, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { linkPatternValidator } from './settings-link-validator';
import { ArchiveSessionGetService } from '../../../services/archive-session-get';
import { ArchiveParseChapterService } from '../../../services/archive-parse-chapter';
import { ArchiveParseStoryService } from '../../../services/archive-parse-story';
import { ArchiveServerRequestData } from '../../../models/archive-server-request-data';
import { catchError, Observable, Subscription } from 'rxjs';
import { ArchiveServerResponseData } from '../../../models/archive-server-response-data';


@Component({
  selector: 'app-settings',
  imports: [ReactiveFormsModule],
  templateUrl: './settings.html',
  styleUrl: './settings.css',
})
export class Settings {
    isDisabled = signal(false);
    settingsFormGroup: FormGroup = new FormGroup({
        parseType: new FormControl<string>('', Validators.required),
        parseLink: new FormControl<string>('', [Validators.required, linkPatternValidator]),
        parseNickname: new FormControl<string>('', Validators.pattern("^[a-zA-Z0-9_-]*$"))
    });
    archiveParseChapterService: ArchiveParseChapterService = inject(ArchiveParseChapterService);
    archiveParseStoryService: ArchiveParseStoryService = inject(ArchiveParseStoryService);
    archiveSessionGetService: ArchiveSessionGetService = inject(ArchiveSessionGetService);
    latestResponse$!: Observable<ArchiveServerResponseData | null>;
    latestSubscription$!: Subscription;
    latestResponse = signal<ArchiveServerResponseData>({
        sessionId: "",
        sessionNickname: "",
        sessionFinished: false,
        sessionCanceled: false,
        sessionException: false,
        parseChaptersCompleted: 0,
        parseChaptersTotal: 0,
        parseResult: "",
        responseMessage: ""
    });
    curSessionId = signal<string>("");


    callParse() {
        // Create request
        let newArchiveServerRequest: ArchiveServerRequestData = new ArchiveServerRequestData();
        newArchiveServerRequest.pageLink = this.settingsFormGroup.get('parseLink')?.value;
        newArchiveServerRequest.sessionNickname = this.settingsFormGroup.get('parseNickname')?.value;
        
        // Get confirmation response
        if (this.settingsFormGroup.get('parseType')?.value === "chapter") {
            this.archiveParseChapterService.postParseChapterRequest(newArchiveServerRequest).pipe(
                catchError((err) => {
                    console.log(err);
                    throw err;
                })
            ).subscribe((result) => {
                this.latestResponse.set(result);
                this.curSessionId.set(this.latestResponse().sessionId);
            })
        }
        else if (this.settingsFormGroup.get('parseType')?.value === "story") {
            this.archiveParseStoryService.postParseStoryRequest(newArchiveServerRequest).pipe(
                catchError((err) => {
                    console.log(err);
                    throw err;
                })
            ).subscribe((result) => {
                this.latestResponse.set(result);
                this.curSessionId.set(this.latestResponse().sessionId);
            })
        }
        else {
            throw new TypeError("Bad type of parseType.");
        }

        // Monitor session
        this.latestResponse$ = this.archiveSessionGetService.monitorSessionProgress(this.curSessionId());
        this.latestSubscription$ = this.latestResponse$.subscribe((result) => {
            if (result) {
                this.latestResponse.set(result);
            }
            else {
                this.latestResponse.set({
                    sessionId: this.curSessionId(),
                    sessionNickname: "",
                    sessionFinished: false,
                    sessionCanceled: false,
                    sessionException: true,
                    parseChaptersCompleted: 0,
                    parseChaptersTotal: 0,
                    parseResult: "",
                    responseMessage: "Recieved null result from ArchiveSessionGetService."
                });
            }
        });
    }

    submitSettingsFormGroup() {
        this.isDisabled.set(true);
        this.settingsFormGroup.disable();

        try {
            this.callParse();
        }
        catch (err) {
            console.log(err);
        }

        this.isDisabled.set(false);
        this.settingsFormGroup.enable();
    }
}
