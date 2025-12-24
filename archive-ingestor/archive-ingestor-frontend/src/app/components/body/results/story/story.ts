import { Component, input, InputSignal } from '@angular/core';
import { ArchiveStoryResultUnit } from '../../../../models/archive-story-result-unit';

@Component({
  selector: 'app-story',
  imports: [],
  templateUrl: './story.html',
  styleUrl: './story.css',
})
export class Story {
    storyResultUnit: InputSignal<ArchiveStoryResultUnit | undefined> = input.required<ArchiveStoryResultUnit | undefined>();
    latestUnit: InputSignal<boolean> = input.required<boolean>();
    topLevelUnit: InputSignal<boolean> = input.required<boolean>();
}
