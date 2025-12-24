import { Component, input, InputSignal, OnInit, signal, WritableSignal } from '@angular/core';
import { ArchiveCompletedSession } from '../../../../models/archive-completed-session';
import { OutputIcons } from "../output-icons/output-icons";
import { ArchiveStoryResultUnit } from '../../../../models/archive-story-result-unit';
import { ArchiveChapterResultUnit } from '../../../../models/archive-chapter-result-unit';
import { ArchiveStoryData } from '../../../../models/archive-story-data';
import { ArchiveChapterData } from '../../../../models/archive-chapter-data';
import { Chapter } from "../chapter/chapter";
import { Story } from "../story/story";
import { ArchiveMetadataResultUnit } from '../../../../models/archive-metadata-result-unit';

@Component({
  selector: 'app-session',
  imports: [OutputIcons, Chapter, Story],
  templateUrl: './session.html',
  styleUrl: './session.css',
})
export class Session implements OnInit {
    defaultDisplayLimit: number = 50;

    completedSession: InputSignal<ArchiveCompletedSession | undefined> = input.required<ArchiveCompletedSession | undefined>();
    latestUnit: InputSignal<boolean> = input.required<boolean>();
    sessionSelected: WritableSignal<boolean> = signal<boolean>(false);

    completedStoryUnit: WritableSignal<ArchiveStoryResultUnit | undefined> = signal<ArchiveStoryResultUnit | undefined>(undefined);
    completedChapterUnit: WritableSignal<ArchiveChapterResultUnit | undefined> = signal<ArchiveChapterResultUnit | undefined>(undefined);
    completedUnitType: WritableSignal<string> = signal<string>("");

    ngOnInit(): void {
        if (this.completedSession()?.data.data instanceof ArchiveStoryData) {
            // Create story result unit
            this.completedStoryUnit.set(new ArchiveStoryResultUnit({
                id: this.completedSession()?.data?.id,
                nickname: this.completedSession()?.data?.nickname,
                data: this.completedSession()?.data?.data
            }));
            this.completedUnitType.set("story");
        }
        else if (this.completedSession()?.data.data instanceof ArchiveChapterData) {
            // Create metadata
            let chapterData = this.completedSession()?.data?.data as ArchiveChapterData
            let storyMetadataUnit = new ArchiveMetadataResultUnit({
                id: this.completedSession()?.data?.id,
                nickname: this.completedSession()?.data?.nickname,
                data: chapterData.parentArchiveStoryInfo
            });

            // Create chapter result unit
            this.completedChapterUnit.set(new ArchiveChapterResultUnit({
                id: this.completedSession()?.data?.id,
                nickname: this.completedSession()?.data?.nickname,
                data: this.completedSession()?.data?.data,
                storyMetadata: storyMetadataUnit
            }));
            this.completedUnitType.set("chapter");
        }
    }

    flipSessionSelected() {
        this.sessionSelected.set(!this.sessionSelected());
    }
}
