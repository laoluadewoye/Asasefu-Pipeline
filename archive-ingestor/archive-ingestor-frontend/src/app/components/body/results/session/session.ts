import { Component, input, InputSignal } from '@angular/core';
import { ArchiveCompletedSession } from '../../../../models/archive-completed-session';

@Component({
  selector: 'app-session',
  imports: [],
  templateUrl: './session.html',
  styleUrl: './session.css',
})
export class Session {
    completedSession: InputSignal<ArchiveCompletedSession | undefined> = input.required<ArchiveCompletedSession | undefined>();
    latestUnit: InputSignal<boolean> = input.required<boolean>();
    topLevelUnit: InputSignal<boolean> = input.required<boolean>();
}
