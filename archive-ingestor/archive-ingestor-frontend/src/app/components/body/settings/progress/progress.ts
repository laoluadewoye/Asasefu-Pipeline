import { Component, inject, input, InputSignal, OnChanges, signal, SimpleChanges, WritableSignal } from '@angular/core';
import { ArchiveServerResponseData } from '../../../../models/archive-server-response-data';
import { ArchiveSessionCancelService } from '../../../../services/archive-session-cancel';
import { catchError } from 'rxjs';

@Component({
  selector: 'app-progress',
  imports: [],
  templateUrl: './progress.html',
  styleUrl: './progress.css',
})
export class Progress implements OnChanges {
    currentResponse: InputSignal<ArchiveServerResponseData> = input.required<ArchiveServerResponseData>();
    currentParseResult: WritableSignal<string> = signal("");
    currentResponseMessage: WritableSignal<string> = signal("");
    textLimit: number = 70;

    archiveServerCancelService: ArchiveSessionCancelService = inject(ArchiveSessionCancelService);
    cancelResponseMessage: WritableSignal<string> = signal("");
    sessionNotActive: WritableSignal<boolean> = signal(false);
    parentDefaultTimeoutMilli: InputSignal<number> = input.required<number>();

    trimString(s: string): string {
        return (s.length > this.textLimit) ? s.substring(0, this.textLimit) + "..." : s;
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.currentParseResult.set(this.trimString(this.currentResponse().parseResult));
        this.currentResponseMessage.set(this.trimString(this.currentResponse().responseMessage));
        this.sessionNotActive.set(
            this.currentResponse().sessionFinished ||
            this.currentResponse().sessionCanceled ||
            this.currentResponse().sessionException
        )
    }

    onProgressButtonClick() {
        this.archiveServerCancelService.getCancelSessionResponse(this.currentResponse().sessionId).pipe(
            catchError((err) => {
                console.log(err);
                throw err;
            })
        ).subscribe((result) => {
            console.log(result);
            this.cancelResponseMessage.set(result.responseMessage);
        });

        setTimeout(() => {
            this.cancelResponseMessage.set("");
        }, this.parentDefaultTimeoutMilli());
    }
}
