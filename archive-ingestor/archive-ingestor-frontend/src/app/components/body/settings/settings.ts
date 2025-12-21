import { Component, inject, output, OutputEmitterRef, signal, WritableSignal } from '@angular/core';
import { FormControl, Validators, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { linkPatternValidator } from './settings-link-validator';
import { ArchiveSessionGetService } from '../../../services/archive-session-get';
import { ArchiveParseChapterService } from '../../../services/archive-parse-chapter';
import { ArchiveParseStoryService } from '../../../services/archive-parse-story';
import { ArchiveServerRequestData } from '../../../models/archive-server-request-data';
import { catchError, Subscription } from 'rxjs';
import { ArchiveServerResponseData } from '../../../models/archive-server-response-data';
import { Progress } from "./progress/progress";

@Component({
  selector: 'app-settings',
  imports: [ReactiveFormsModule, Progress],
  templateUrl: './settings.html',
  styleUrl: './settings.css',
})
export class Settings {
    // Form and Form control
    isDisabled: WritableSignal<boolean> = signal(false);
    settingsFormGroup: FormGroup = new FormGroup({
        parseType: new FormControl<string>('', Validators.required),
        parseLink: new FormControl<string>('', [Validators.required, linkPatternValidator]),
        parseNickname: new FormControl<string>('', Validators.pattern("^[a-zA-Z0-9_-]*$")),
        parseAdvancedToggled: new FormControl<string>('no', Validators.required),
        parseMaxCommentThreadDepth: new FormControl<number>(10, {nonNullable: true}),
        parseMaxCommentPageLimit: new FormControl<number>(3, {nonNullable: true}),
        parseMaxKudosPageLimit: new FormControl<number>(3, {nonNullable: true}),
        parseMaxBookmarkPageLimit: new FormControl<number>(3, {nonNullable: true}),
    });

    // Services
    archiveParseChapterService: ArchiveParseChapterService = inject(ArchiveParseChapterService);
    archiveParseStoryService: ArchiveParseStoryService = inject(ArchiveParseStoryService);
    archiveSessionGetService: ArchiveSessionGetService = inject(ArchiveSessionGetService);

    // Response management
    getLatestSubscription!: Subscription;
    latestResponse: WritableSignal<ArchiveServerResponseData> = signal<ArchiveServerResponseData>({
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
    curSessionId: WritableSignal<string> = signal("");
    sessionFinished: OutputEmitterRef<string> = output<string>();

    resetAdvancedIfNeeded() {
        const formValues: string[] = [
            'parseMaxCommentThreadDepth', 
            'parseMaxCommentPageLimit', 
            'parseMaxKudosPageLimit', 
            'parseMaxBookmarkPageLimit'
        ]
        for (let i = 0; i < formValues.length; i++) {
            let curFormSetting = this.settingsFormGroup.get(formValues[i]);
            if (curFormSetting?.value === null) curFormSetting?.reset();
        }
    }

    async callParse() {
        // Setup waiting check
        let lastSessionId: string = this.curSessionId();

        // Create request
        let newArchiveServerRequest: ArchiveServerRequestData = new ArchiveServerRequestData();
        newArchiveServerRequest.pageLink = this.settingsFormGroup.get('parseLink')?.value;
        newArchiveServerRequest.sessionNickname = this.settingsFormGroup.get('parseNickname')?.value;
        newArchiveServerRequest.maxCommentThreadDepth = this.settingsFormGroup.get('parseMaxCommentThreadDepth')?.value;
        newArchiveServerRequest.maxCommentPageLimit = this.settingsFormGroup.get('parseMaxCommentPageLimit')?.value;
        newArchiveServerRequest.maxKudosPageLimit = this.settingsFormGroup.get('parseMaxKudosPageLimit')?.value;
        newArchiveServerRequest.maxBookmarkPageLimit = this.settingsFormGroup.get('parseMaxBookmarkPageLimit')?.value;
        
        // Get confirmation response
        if (this.settingsFormGroup.get('parseType')?.value === "chapter") {
            this.archiveParseChapterService.postParseChapterRequest(newArchiveServerRequest).pipe(
                catchError((err) => {
                    console.log(err);
                    throw err;
                })
            ).subscribe((result) => this.latestResponse.set(result));
        }
        else if (this.settingsFormGroup.get('parseType')?.value === "story") {
            this.archiveParseStoryService.postParseStoryRequest(newArchiveServerRequest).pipe(
                catchError((err) => {
                    console.log(err);
                    throw err;
                })
            ).subscribe((result) => this.latestResponse.set(result));
        }
        else {
            throw new TypeError("Bad type of parseType.");
        }

        // Wait for confirmation response
        let waitingForResponse: boolean = true;
        while (waitingForResponse) {
            await new Promise(resolve => setTimeout(resolve, 1000));
            waitingForResponse = lastSessionId === this.latestResponse().sessionId;
        }

        // Monitor session
        this.curSessionId.set(this.latestResponse().sessionId);
        this.getLatestSubscription = this.archiveSessionGetService.getSessionInformationLive(
            this.curSessionId()
        ).subscribe((result) => {
            // Set latest response
            this.latestResponse.set(result);

            // Check flags
            let sF: boolean = this.latestResponse().sessionFinished;
            let sC: boolean = this.latestResponse().sessionCanceled;
            let sE: boolean = this.latestResponse().sessionException;

            // Manually end the session if any flag set
            if (sF || sC || sE) {
                this.isDisabled.set(false);
                this.settingsFormGroup.enable();
                this.getLatestSubscription.unsubscribe();
                this.archiveSessionGetService.unsubscribeFromStomp();
                this.sessionFinished.emit(this.curSessionId());
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
