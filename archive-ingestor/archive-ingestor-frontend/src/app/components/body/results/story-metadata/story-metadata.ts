import { Component, input, InputSignal } from '@angular/core';
import { ArchiveResultUnit } from '../../../../models/archive-result-unit';

@Component({
  selector: 'app-story-metadata',
  imports: [],
  templateUrl: './story-metadata.html',
  styleUrl: './story-metadata.css',
})
export class StoryMetadata {
    storyMetadataResultUnit: InputSignal<ArchiveResultUnit | undefined> = input.required<ArchiveResultUnit | undefined>();
    latestUnit: InputSignal<boolean> = input.required<boolean>();
}
