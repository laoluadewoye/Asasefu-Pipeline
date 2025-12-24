import { Component, input, InputSignal, OnInit, signal, WritableSignal } from '@angular/core';
import { ArchiveChapterResultUnit } from '../../../../models/archive-chapter-result-unit';
import { OutputIcons } from '../output-icons/output-icons';
import { OutputList } from '../output-list/output-list';
import { Comments } from "../comments/comments";
import { ArchiveCommentUnit } from '../../../../models/archive-comment-unit';
import { StoryMetadata } from "../story-metadata/story-metadata";

@Component({
  selector: 'app-chapter',
  imports: [OutputIcons, OutputList, Comments, StoryMetadata],
  templateUrl: './chapter.html',
  styleUrl: './chapter.css',
})
export class Chapter implements OnInit {
    // Defaults
    defaultDisplayLimit: number = 50;
    defaultParagraphDisplayLimit: number = 10;

    // Inputs
    chapterResultUnit: InputSignal<ArchiveChapterResultUnit | undefined> = input.required<ArchiveChapterResultUnit | undefined>();
    latestUnit: InputSignal<boolean> = input.required<boolean>();
    partOfSession: InputSignal<boolean> = input.required<boolean>();

    // Custom downloadable
    chapterDownloadable = signal({});

    // Selection booleans
    chapterSelected: WritableSignal<boolean> = signal<boolean>(false);

    ngOnInit(): void {
        this.chapterDownloadable.set({chapter: this.chapterResultUnit(), formattedComments: null});
    }

    flipChapterSelected() {
        this.chapterSelected.set(!this.chapterSelected());
    }

    updateDownloadable(event: ArchiveCommentUnit[]) {
        this.chapterDownloadable.set({chapter: this.chapterResultUnit(), formattedComments: event});
    }
}
