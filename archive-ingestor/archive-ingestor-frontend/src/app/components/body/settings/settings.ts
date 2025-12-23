import { Component, inject, input, InputSignal, OnInit, output, OutputEmitterRef, signal, WritableSignal } from '@angular/core';
import { FormControl, Validators, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { linkPatternValidator } from './settings-link-validator';
import { ArchiveSessionGetService } from '../../../services/archive-session-get';
import { ArchiveParseChapterService } from '../../../services/archive-parse-chapter';
import { ArchiveParseStoryService } from '../../../services/archive-parse-story';
import { ArchiveServerRequestData } from '../../../models/archive-server-request-data';
import { catchError, of, Subscription } from 'rxjs';
import { ArchiveServerResponseData } from '../../../models/archive-server-response-data';
import { Progress } from "./progress/progress";

@Component({
  selector: 'app-settings',
  imports: [ReactiveFormsModule, Progress],
  templateUrl: './settings.html',
  styleUrl: './settings.css',
})
export class Settings implements OnInit {
    // Controlling visuals
    isDisabled: WritableSignal<boolean> = signal(false);
    callParseError: WritableSignal<string> = signal("");

    // Form and Form control
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
    sessionComplete: OutputEmitterRef<string> = output<string>();

    // Timeouts
    parentDefaultTimeoutMilli: InputSignal<number> = input.required<number>();
    defaultTimeoutMilli: WritableSignal<number> = signal<number>(0);
    parentDefaultServiceWaitMilli: InputSignal<number> = input.required<number>();

    ngOnInit() {
        this.defaultTimeoutMilli.set(this.parentDefaultTimeoutMilli());
    }

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

    disableSettingsForm() {
        this.isDisabled.set(true);
        this.settingsFormGroup.disable();
    }

    enableSettingsForm() {
        setTimeout(() => {
                this.isDisabled.set(false); 
                this.settingsFormGroup.enable(); 
                this.callParseError.set("")
            }, 
            this.parentDefaultTimeoutMilli()
        );
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
                    this.callParseError.set("Archive Parse Chapter Service encountered an error.");
                    this.enableSettingsForm();
                    throw err;
                })
            ).subscribe((result) => this.latestResponse.set(result));
        }
        else if (this.settingsFormGroup.get('parseType')?.value === "story") {
            this.archiveParseStoryService.postParseStoryRequest(newArchiveServerRequest).pipe(
                catchError((err) => {
                    this.callParseError.set("Archive Parse Story Service encountered an error.");
                    this.enableSettingsForm();
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
            await new Promise(resolve => setTimeout(resolve, this.parentDefaultServiceWaitMilli()));
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
                this.getLatestSubscription.unsubscribe();
                this.archiveSessionGetService.unsubscribeFromStomp();
                this.sessionComplete.emit(this.curSessionId());
                this.enableSettingsForm();
            }
        });
    }

    submitSettingsFormGroup() {
        // Disable settings
        this.disableSettingsForm();

        // Call the parse job
        try {
            this.callParse();
        }
        catch (err) {
            console.log(err);
            this.callParseError.set("General parser logic encountered an error.");
            this.enableSettingsForm();
        }
    }
}
