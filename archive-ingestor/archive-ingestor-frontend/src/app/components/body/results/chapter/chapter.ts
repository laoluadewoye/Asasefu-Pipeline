import { Component, input, InputSignal } from '@angular/core';
import { ArchiveResultUnit } from '../../../../models/archive-result-unit';

@Component({
  selector: 'app-chapter',
  imports: [],
  templateUrl: './chapter.html',
  styleUrl: './chapter.css',
})
export class Chapter {
    chapterResultUnit: InputSignal<ArchiveResultUnit | undefined> = input.required<ArchiveResultUnit | undefined>();
    latestUnit: InputSignal<boolean> = input.required<boolean>();
    topLevelUnit: InputSignal<boolean> = input.required<boolean>();
}
