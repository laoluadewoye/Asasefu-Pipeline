import { Component, input, InputSignal, OnInit, signal, WritableSignal } from '@angular/core';
import { OutputIcons } from "../output-icons/output-icons";
import { ArchiveMetadataResultUnit } from '../../../../models/archive-metadata-result-unit';
import { OutputList } from '../output-list/output-list';

@Component({
  selector: 'app-story-metadata',
  imports: [OutputIcons, OutputList],
  templateUrl: './story-metadata.html',
  styleUrl: './story-metadata.css',
})
export class StoryMetadata implements OnInit {
    // Result Unit
    storyMetadataResultUnit: InputSignal<ArchiveMetadataResultUnit | undefined> = input.required<ArchiveMetadataResultUnit | undefined>();
    latestUnit: InputSignal<boolean> = input.required<boolean>();

    // Display signals
    parentOutputListDisplayLimit: InputSignal<number> = input.required<number>();
    outputListDisplayLimit: WritableSignal<number> = signal<number>(0);

    parentNicknameDisplayLimit: InputSignal<number> = input.required<number>();
    displayNickname: WritableSignal<string> = signal<string>("");

    // Selection booleans
    metadataSelected: WritableSignal<boolean> = signal<boolean>(false);

    ngOnInit(): void {
        this.outputListDisplayLimit.set(this.parentOutputListDisplayLimit());

        // Set up the display name
        let n = this.storyMetadataResultUnit()?.nickname;
        if (n && n.length > this.parentNicknameDisplayLimit()) {
            this.displayNickname.set(n.slice(0, this.parentNicknameDisplayLimit()) + "...");
        }
        else if (n) {
            this.displayNickname.set(n);
        }
    }

    flipMetadataSelected() {
        this.metadataSelected.set(!this.metadataSelected());
    }
}
