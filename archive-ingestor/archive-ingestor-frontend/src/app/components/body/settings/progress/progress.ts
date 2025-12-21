import { Component, input, InputSignal} from '@angular/core';
import { ArchiveServerResponseData } from '../../../../models/archive-server-response-data';

@Component({
  selector: 'app-progress',
  imports: [],
  templateUrl: './progress.html',
  styleUrl: './progress.css',
})
export class Progress {
    currentResponse: InputSignal<ArchiveServerResponseData> = input.required<ArchiveServerResponseData>();
}
