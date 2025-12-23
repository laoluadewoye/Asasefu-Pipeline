import { Component, input, InputSignal } from '@angular/core';
import { ArchiveResultUnit } from '../../../../models/archive-result-unit';

@Component({
  selector: 'app-story',
  imports: [],
  templateUrl: './story.html',
  styleUrl: './story.css',
})
export class Story {
    storyResultUnit: InputSignal<ArchiveResultUnit | undefined> = input.required<ArchiveResultUnit | undefined>();
    latestUnit: InputSignal<boolean> = input.required<boolean>();
    topLevelUnit: InputSignal<boolean> = input.required<boolean>();
}
