import { Component, input } from '@angular/core';
import { ArchiveResultUnit } from '../../../../models/archive-result-unit';

@Component({
  selector: 'app-story-metadata',
  imports: [],
  templateUrl: './story-metadata.html',
  styleUrl: './story-metadata.css',
})
export class StoryMetadata {
    storyMetadataResultUnit = input<ArchiveResultUnit>();
}
