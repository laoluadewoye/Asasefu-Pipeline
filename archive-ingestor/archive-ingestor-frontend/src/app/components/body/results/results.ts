import { Component, inject, input, InputSignal, OnChanges, signal, SimpleChanges, WritableSignal } from '@angular/core';
import { ArchiveServerResponseData } from '../../../models/archive-server-response-data';
import { ArchiveStoryData } from '../../../models/archive-story-data';
import { ArchiveChapterData } from '../../../models/archive-chapter-data';
import { ArchiveSessionData } from '../../../models/archive-session-data';
import { ArchiveCompletedSession } from '../../../models/archive-completed-session';
import { ArchiveSessionGetService } from '../../../services/archive-session-get';
import { catchError } from 'rxjs';
import { StoryMetadata } from './story-metadata/story-metadata';
import { Chapter } from './chapter/chapter';
import { Session } from "./session/session";
import { ArchiveMetadataResultUnit } from '../../../models/archive-metadata-result-unit';
import { ArchiveChapterResultUnit } from '../../../models/archive-chapter-result-unit';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-results',
  imports: [StoryMetadata, Chapter, Session, ReactiveFormsModule],
  templateUrl: './results.html',
  styleUrl: './results.css',
})
export class Results implements OnChanges {
    // General properties
    activeTab: WritableSignal<string> = signal<string>("");
    archiveSessionGetService: ArchiveSessionGetService = inject(ArchiveSessionGetService);

    // Session management properties
    parentCompletedSessionIds: InputSignal<string[]> = input.required<string[]>();
    parentLatestCompletedSessionId: InputSignal<string> = input.required<string>();
    completedSessionMap: WritableSignal<Map<string, ArchiveCompletedSession>> = signal<Map<string, ArchiveCompletedSession>>(new Map());
    sessionIdToHashMap: Map<string, string> = new Map();

    // Response cache properties
    responseCache: Map<string, ArchiveServerResponseData> = new Map();
    endResponseMessagePhrases: string[] = [
        "Sent link was not a proper URL format.",
        "Sent nickname was not a proper URL format.",
        "Sent link was a chapter-specific link.",
        "The driver service could not create a driver for parsing.",
        "The execution was unexpectedly interrupted during Thread.sleep()",
        "chapter's paragraphs could not be found.",
        "The archive ingestor could not find a required element during parsing.",
        "The archive ingestor's task was canceled from parent service.",
        "The archive ingestor came across the archive's 404 page and stopped parsing.",
        "Successfully retrieved JSON representation"
    ];

    // Display mangement properties
    isRefreshing: WritableSignal<boolean> = signal<boolean>(false);
    unaddressedUpdatedSessions: string[] = [];
    parentDefaultServiceWaitMilli: InputSignal<number> = input.required<number>();

    // Display mangement signals
    storyMetadataMap: WritableSignal<Map<string, ArchiveMetadataResultUnit>> = signal<Map<string, ArchiveMetadataResultUnit>>(new Map());
    chapterMap: WritableSignal<Map<string, ArchiveChapterResultUnit>> = signal<Map<string, ArchiveChapterResultUnit>>(new Map());

    latestStoryMetadataUnit: WritableSignal<ArchiveMetadataResultUnit | undefined> = signal<ArchiveMetadataResultUnit | undefined>(undefined);
    latestChapterUnit: WritableSignal<ArchiveChapterResultUnit | undefined> = signal<ArchiveChapterResultUnit | undefined>(undefined);

    // Search properties
    resultSearch: FormControl =  new FormControl<string>("");

    ngOnChanges(changes: SimpleChanges): void {
        console.log("Changes were made to result component");
        console.log(changes);
        this.refreshView();
    }

    selectTab(tabOption: string) {
        this.activeTab.set(tabOption);
    }

    formatParseResult(parseResult: string) {
        let parseResultJSON = JSON.parse(parseResult);
        let parseResultFinal;
        if (Object.keys(parseResultJSON).includes("parentArchiveStoryInfo")) {
            parseResultFinal = new ArchiveChapterData(parseResultJSON);
        }
        else if (Object.keys(parseResultJSON).includes("archiveStoryInfo")) {
            parseResultFinal = new ArchiveStoryData(parseResultJSON);
        }

        return parseResultFinal;
    }

    getCrypto() {
        try {
            return window.crypto;
        } catch {
            return crypto;
        }
    }

    async createNewCompletedSession(response: ArchiveServerResponseData) {
        // Create a new completed session
        let newOutcome: string;
        if (response.sessionFinished) {
            newOutcome = "Finished";
        }
        else if (response.sessionCanceled) {
            newOutcome = "Canceled";
        }
        else if (response.sessionException) {
            newOutcome = "Exception";
        }
        else {
            newOutcome = "Unexpected Error: No completion flag set.";
        }

        let newSessionData: ArchiveSessionData = new ArchiveSessionData();
        newSessionData.id = response.sessionId;
        newSessionData.nickname = response.sessionNickname;
        newSessionData.outcome = newOutcome;

        if (response.parseResult !== "") {
            newSessionData.data = this.formatParseResult(response.parseResult);
        }   
        else {
            newSessionData.data = undefined;
        }

        if (newSessionData.data === undefined) {
            newSessionData.outcome = newSessionData.outcome + " Unexpected Error: Parse result failed.";
        }
        console.log(newSessionData.outcome);
        console.log(newSessionData.outcome);

        // Do this funky hashing because node:crypto isn't allowed
        let hashInput = new TextEncoder().encode(
            response.sessionId + 
            response.sessionNickname + 
            newSessionData.outcome + 
            response.parseResult
        );
        let hashBuffer = await this.getCrypto().subtle.digest("SHA-256", hashInput);

        let hashArray = Array.from(new Uint8Array(hashBuffer));
        let hashHex = hashArray.map((b) => b.toString(16).padStart(2, "0")).join("");

        return new ArchiveCompletedSession({hash: btoa(hashHex), data: newSessionData})
    }

    async addNewCompletedSession(response: ArchiveServerResponseData) {
        // Create new completed session
        let newCompletedSession: ArchiveCompletedSession = await this.createNewCompletedSession(response);

        // Check id to hash map. Matching hash means the same data was sent in and no change is needed.
        let newHashMatch: boolean = newCompletedSession.hash === this.sessionIdToHashMap.get(newCompletedSession.data.id);

        // Do more things if the checks pass
        if (!newHashMatch) {
            // Add the session id to the id-hash map
            this.sessionIdToHashMap.set(newCompletedSession.data.id, newCompletedSession.hash);

            // Add the new completed session
            let csm = this.completedSessionMap();
            csm.set(newCompletedSession.hash, newCompletedSession);
            this.completedSessionMap.set(csm);

            // Add session id to sessions to address later
            this.unaddressedUpdatedSessions.push(newCompletedSession.data.id);
            console.log(`Added ${newCompletedSession.data.id} to unaddressedUpdatedSessions.`);
        }
    }

    endResponseMessageFound(responseMessage: string | undefined) {
        if (responseMessage) {
            return this.endResponseMessagePhrases.filter(
                (phrase) => responseMessage.includes(phrase)
            ).length > 0;
        }
        else {
            return false;
        }
    }

    async refreshCompletedSessionMap() {
        // Refresh completed session data
        let gsiFinished: boolean[] = Array(this.parentCompletedSessionIds().length).fill(false);
        this.parentCompletedSessionIds().forEach((sessionId: string, index: number) => {
            // Check result cache first
            let responseData: ArchiveServerResponseData | undefined = this.responseCache.get(sessionId);
            if (responseData && this.endResponseMessageFound(responseData?.responseMessage)) {
                gsiFinished[index] = true;
            }
            else {
                // Call the service if needed
                this.archiveSessionGetService.getSessionInformation(sessionId).pipe(
                    catchError((err) => {
                        console.log(err);
                        throw err;
                    })
                ).subscribe((result) => {
                    this.responseCache.set(sessionId, result);
                    console.log(`Added ${sessionId} to unaddressedUpdatedSessions.`);
                    console.log(this.responseCache.get(sessionId));

                    this.addNewCompletedSession(result);
                    gsiFinished[index] = true;
                });
            }
        });

        // Wait for confirmation response
        let waitingForResponse: boolean = true;
        while (waitingForResponse) {
            await new Promise(resolve => setTimeout(resolve, this.parentDefaultServiceWaitMilli()));
            waitingForResponse = !gsiFinished.every((b) => b === true);
        }
    }

    getMetadataAndChapters(completedSession: ArchiveCompletedSession | undefined) {
        // Isolate story metadata and chapters from information
        let storyOrChapter = completedSession?.data.data;
        let metadata: ArchiveMetadataResultUnit;
        let chapters: Array<ArchiveChapterResultUnit> = [];
        if (storyOrChapter instanceof ArchiveStoryData) {
            metadata = new ArchiveMetadataResultUnit({
                id: completedSession?.data.id,
                nickname: completedSession?.data.nickname,
                data: storyOrChapter.archiveStoryInfo
            });
            storyOrChapter.archiveChapters.forEach((chapter, index) => {
                chapters.push(new ArchiveChapterResultUnit({
                    id: `${completedSession?.data.id}_${index+1}`,
                    nickname: `(Chapter ${index+1}) ${completedSession?.data.nickname}`,
                    data: chapter,
                    storyMetadata: metadata
                }));
            });
        }
        else if (storyOrChapter instanceof ArchiveChapterData) {
            metadata = new ArchiveMetadataResultUnit({
                id: completedSession?.data.id,
                nickname: completedSession?.data.nickname,
                data: storyOrChapter.parentArchiveStoryInfo
            });
            chapters.push(new ArchiveChapterResultUnit({
                id: `${completedSession?.data.id}_single`,
                nickname: `(Single Chapter) ${completedSession?.data.nickname}`,
                data: storyOrChapter,
                storyMetadata: metadata
            }));
        }
        else {
            metadata = new ArchiveMetadataResultUnit({});
        }

        return {storyOrChapter: storyOrChapter, metadata: metadata, chapters: chapters}
    }

    async refreshView() {
        this.isRefreshing.set(true);

        // Empty the array
        this.unaddressedUpdatedSessions = [];
        console.log("Cleared unaddressedUpdatedSessions.");
        console.log(this.unaddressedUpdatedSessions);

        // Refresh completed session data
        await this.refreshCompletedSessionMap();

        // Refresh other management signals
        this.unaddressedUpdatedSessions.forEach((sessionId) => {
            // Get hash
            let correlatedHash = this.sessionIdToHashMap.get(sessionId);

            // Get session if possible
            let completedSession: ArchiveCompletedSession | undefined;
            if (correlatedHash !== undefined) {
                completedSession = this.completedSessionMap().get(correlatedHash);
            }
            console.log(`Got ${sessionId} from completedSessionMap.`);
            console.log(correlatedHash);
            console.log(completedSession);
            console.log(completedSession?.data.data);

            let metadataAndChapters = this.getMetadataAndChapters(completedSession);
            console.log(`Got ${sessionId}'s metadata and chapters.`);
            console.log(metadataAndChapters);
            
            // Update management maps
            let isArchiveStoryData: boolean = metadataAndChapters.storyOrChapter instanceof ArchiveStoryData;
            let isArchiveChapterData: boolean = metadataAndChapters.storyOrChapter instanceof ArchiveChapterData;
            if (isArchiveStoryData || isArchiveChapterData) {
                // Update maps and latest values
                let smm = this.storyMetadataMap();
                smm.set(metadataAndChapters.metadata.id, metadataAndChapters.metadata);
                this.storyMetadataMap.set(smm);
                this.latestStoryMetadataUnit.set(metadataAndChapters.metadata);

                let cm = this.chapterMap();
                metadataAndChapters.chapters.forEach((chapter: ArchiveChapterResultUnit) => {
                    cm.set(chapter.id, chapter);
                    this.latestChapterUnit.set(chapter);
                });
                this.chapterMap.set(cm);
            }
            console.log(`Updated management maps with ${sessionId}'s metadata and chapters.`);
            console.log(this.storyMetadataMap());
            console.log(this.latestStoryMetadataUnit());
            console.log(this.latestChapterUnit());
            console.log(this.chapterMap());
        });

        this.isRefreshing.set(false);
    }

    getLatestCompletedSession() {
        let csmHash = this.sessionIdToHashMap.get(this.parentLatestCompletedSessionId());
        if (csmHash) {
            return this.completedSessionMap().get(csmHash);
        }
        else {
            return undefined;
        }
    }

    filterCompletedSession(session: ArchiveCompletedSession | undefined) {
        if (this.resultSearch.value === "" || session === undefined) {
            return session;
        }
        else {
            if (session.data.id.includes(this.resultSearch.value) || session.data.nickname.includes(this.resultSearch.value)) {
                return session;
            }
            else {
                return undefined;
            }
        }
    }

    filterCompletedSessions(sessions: MapIterator<ArchiveCompletedSession>) {
        return Array.from(sessions).filter((sessions) => this.filterCompletedSession(sessions));
    }

    filterUnit(unit: ArchiveMetadataResultUnit | ArchiveChapterResultUnit | undefined) {
        if (this.resultSearch.value === "" || unit === undefined) {
            return unit;
        }
        else {
            if (unit.id.includes(this.resultSearch.value) || unit.nickname.includes(this.resultSearch.value)) {
                return unit;
            }
            else {
                return undefined;
            }
        }
    }

    filterStoryMetadataUnit(unit: ArchiveMetadataResultUnit | undefined) {
        return this.filterUnit(unit) as ArchiveMetadataResultUnit | undefined;
    }

    filterStoryMetadataUnits(units: MapIterator<ArchiveMetadataResultUnit>) {
        return Array.from(units).filter((unit) => this.filterUnit(unit));
    }

    filterChapterUnit(unit: ArchiveChapterResultUnit | undefined) {
        return this.filterUnit(unit) as ArchiveChapterResultUnit | undefined;
    }

    filterChapterUnits(units: MapIterator<ArchiveChapterResultUnit>) {
        return Array.from(units).filter((unit) => this.filterUnit(unit));
    }
}
