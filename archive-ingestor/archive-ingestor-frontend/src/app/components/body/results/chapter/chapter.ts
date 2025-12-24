import { Component, input, InputSignal } from '@angular/core';
import { ArchiveChapterResultUnit } from '../../../../models/archive-chapter-result-unit';

@Component({
  selector: 'app-chapter',
  imports: [],
  templateUrl: './chapter.html',
  styleUrl: './chapter.css',
})
export class Chapter {
    chapterResultUnit: InputSignal<ArchiveChapterResultUnit | undefined> = input.required<ArchiveChapterResultUnit | undefined>();
    latestUnit: InputSignal<boolean> = input.required<boolean>();
    topLevelUnit: InputSignal<boolean> = input.required<boolean>();
}
