import { Component, input, InputSignal, OnInit, signal, WritableSignal } from '@angular/core';
import { ArchiveChapterResultUnit } from '../../../../models/archive-chapter-result-unit';
import { OutputIcons } from '../output-icons/output-icons';
import { OutputList } from '../output-list/output-list';
import { Comments } from "../comments/comments";
import { StoryMetadata } from "../story-metadata/story-metadata";

@Component({
  selector: 'app-chapter',
  imports: [OutputIcons, OutputList, Comments, StoryMetadata],
  templateUrl: './chapter.html',
  styleUrl: './chapter.css',
})
export class Chapter implements OnInit {
    // Chapter Unit
    chapterResultUnit: InputSignal<ArchiveChapterResultUnit | undefined> = input.required<ArchiveChapterResultUnit | undefined>();
    latestUnit: InputSignal<boolean> = input.required<boolean>();
    partOfSession: InputSignal<boolean> = input.required<boolean>();

    // Display signals
    parentNicknameDisplayLimit: InputSignal<number> = input.required<number>();
    nicknameDisplayLimit: WritableSignal<number> = signal<number>(0);
    displayNickname: WritableSignal<string> = signal<string>("");

    parentOutputListDisplayLimit: InputSignal<number> = input.required<number>();
    parentOutputParagraphDisplayLimit: InputSignal<number> = input.required<number>();

    outputListDisplayLimit: WritableSignal<number> = signal<number>(0);
    outputParagraphDisplayLimit: WritableSignal<number> = signal<number>(0);

    // Selection booleans
    chapterSelected: WritableSignal<boolean> = signal<boolean>(false);

    ngOnInit(): void {
        this.outputListDisplayLimit.set(this.parentOutputListDisplayLimit());
        this.outputParagraphDisplayLimit.set(this.parentOutputParagraphDisplayLimit());
        this.nicknameDisplayLimit.set(this.parentNicknameDisplayLimit());

        // Set up the display name
        let n = this.chapterResultUnit()?.nickname;
        if (n && n.length > this.nicknameDisplayLimit()) {
            this.displayNickname.set(n.slice(0, this.nicknameDisplayLimit()) + "...");
        }
        else if (n) {
            this.displayNickname.set(n);
        }
    }

    flipChapterSelected() {
        this.chapterSelected.set(!this.chapterSelected());
    }
}
