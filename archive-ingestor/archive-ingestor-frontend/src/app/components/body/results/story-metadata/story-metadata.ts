import { Component, input, InputSignal, signal, WritableSignal } from '@angular/core';
import { OutputIcons } from "../output-icons/output-icons";
import { ArchiveMetadataResultUnit } from '../../../../models/archive-metadata-result-unit';
import { OutputList } from '../output-list/output-list';

@Component({
  selector: 'app-story-metadata',
  imports: [OutputIcons, OutputList],
  templateUrl: './story-metadata.html',
  styleUrl: './story-metadata.css',
})
export class StoryMetadata {
    // Defaults
    defaultDisplayLimit: number = 50;

    // Inputs
    storyMetadataResultUnit: InputSignal<ArchiveMetadataResultUnit | undefined> = input.required<ArchiveMetadataResultUnit | undefined>();
    latestUnit: InputSignal<boolean> = input.required<boolean>();
    topLevelUnit: InputSignal<boolean> = input.required<boolean>();

    // Selection booleans
    topLevelSelected: WritableSignal<boolean> = signal<boolean>(false);

    flipTopLevelSelected() {
        this.topLevelSelected.set(!this.topLevelSelected());
    }
}
