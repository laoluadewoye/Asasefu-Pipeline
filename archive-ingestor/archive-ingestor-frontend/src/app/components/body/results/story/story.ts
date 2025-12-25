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
    storyResultUnit: InputSignal<ArchiveStoryResultUnit | undefined> = input.required<ArchiveStoryResultUnit | undefined>();
    latestUnit: InputSignal<boolean> = input.required<boolean>();
    storySelected: WritableSignal<boolean> = signal<boolean>(false);

    storyMetadataUnit: WritableSignal<ArchiveMetadataResultUnit | undefined> = signal<ArchiveMetadataResultUnit | undefined>(undefined);
    storyChapterUnits: WritableSignal<ArchiveChapterResultUnit[]> = signal<ArchiveChapterResultUnit[]>([]);

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
    }

    flipStorySelected() {
        this.storySelected.set(!this.storySelected());
    }
}
