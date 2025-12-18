import { Component, inject, signal } from '@angular/core';
import { FormControl, Validators, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { linkPatternValidator } from './settings-link-validator';
import { ArchiveSessionGetService } from '../../../services/archive-session-get';
import { ArchiveParseChapterService } from '../../../services/archive-parse-chapter';
import { ArchiveParseStoryService } from '../../../services/archive-parse-story';
import { ArchiveServerRequestData } from '../../../models/archive-server-request-data';
import { catchError, of, Subscription } from 'rxjs';
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
    getLatestSubscription!: Subscription;
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
        this.getLatestSubscription = this.archiveSessionGetService.monitorSessionProgress(
            this.curSessionId(), 200
        ).subscribe((result) => {
            // Set latest response
            this.latestResponse.set(result);

            // Check flags
            let sF: boolean = this.latestResponse().sessionFinished;
            let sC: boolean = this.latestResponse().sessionCanceled;
            let sE: boolean = this.latestResponse().sessionException;

            // Manually end the session
            if (sF || sC || sE) {
                this.isDisabled.set(false);
                this.settingsFormGroup.enable();
                this.getLatestSubscription.unsubscribe();
            }
        });
    }

    submitSettingsFormGroup() {
        // Disable settings
        this.isDisabled.set(true);
        this.settingsFormGroup.disable();

        // Call the parse job
        try {
            this.callParse();
        }
        catch (err) {
            console.log(err);
        }
    }
}
