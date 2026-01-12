import { Component, input, InputSignal, OnInit, signal, WritableSignal } from '@angular/core';
import { ArchiveStoryResultUnit } from '../../../../models/archive-story-result-unit';
import { OutputIcons } from "../output-icons/output-icons";
import { StoryMetadata } from "../story-metadata/story-metadata";
import { ArchiveMetadataResultUnit } from '../../../../models/archive-metadata-result-unit';
import { ArchiveChapterResultUnit } from '../../../../models/archive-chapter-result-unit';
import { Chapter } from "../chapter/chapter";

@Component({
  selector: 'app-story',
  imports: [OutputIcons, StoryMetadata, Chapter],
  templateUrl: './story.html',
  styleUrl: './story.css',
})
export class Story implements OnInit {
    // Result Units
    storyResultUnit: InputSignal<ArchiveStoryResultUnit | undefined> = input.required<ArchiveStoryResultUnit | undefined>();
    latestUnit: InputSignal<boolean> = input.required<boolean>();
    storySelected: WritableSignal<boolean> = signal<boolean>(false);

    storyMetadataUnit: WritableSignal<ArchiveMetadataResultUnit | undefined> = signal<ArchiveMetadataResultUnit | undefined>(undefined);
    storyChapterUnits: WritableSignal<ArchiveChapterResultUnit[]> = signal<ArchiveChapterResultUnit[]>([]);

    // Display signals
    parentNicknameDisplayLimit: InputSignal<number> = input.required<number>();
    nicknameDisplayLimit: WritableSignal<number> = signal<number>(0);
    displayNickname: WritableSignal<string> = signal<string>("");

    parentOutputListDisplayLimit: InputSignal<number> = input.required<number>();
    parentOutputParagraphDisplayLimit: InputSignal<number> = input.required<number>();

    outputListDisplayLimit: WritableSignal<number> = signal<number>(0);
    outputParagraphDisplayLimit: WritableSignal<number> = signal<number>(0);
    
    ngOnInit(): void {
        // Create metadata unit
        this.storyMetadataUnit.set(new ArchiveMetadataResultUnit({
            id: this.storyResultUnit()?.id,
            nickname: this.storyResultUnit()?.nickname,
            data: this.storyResultUnit()?.data.archiveStoryInfo
        }));

        // Create chapter units
        this.storyResultUnit()?.data.archiveChapters.forEach((archiveChapter, index) => {
            this.storyChapterUnits().push(new ArchiveChapterResultUnit({
                id: this.storyResultUnit()?.id,
                nickname: `(Chapter ${index+1}) ${this.storyResultUnit()?.nickname}`,
                data: archiveChapter,
            }));
        });

        this.outputListDisplayLimit.set(this.parentOutputListDisplayLimit());
        this.outputParagraphDisplayLimit.set(this.parentOutputParagraphDisplayLimit());
        this.nicknameDisplayLimit.set(this.parentNicknameDisplayLimit());

        // Set up the display name
        let n = this.storyResultUnit()?.nickname;
        if (n && n.length > this.nicknameDisplayLimit()) {
            this.displayNickname.set(n.slice(0, this.nicknameDisplayLimit()) + "...");
        }
        else if (n) {
            this.displayNickname.set(n);
        }
    }

    flipStorySelected() {
        this.storySelected.set(!this.storySelected());
    }
}
