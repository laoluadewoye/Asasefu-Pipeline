import { Component, input, InputSignal, OnInit, signal, WritableSignal } from '@angular/core';
import { ArchiveCommentData } from '../../../../models/archive-comment-data';
import { OutputIcons } from "../output-icons/output-icons";
import { CommentThread } from './comment-thread/comment-thread';

@Component({
  selector: 'app-comments',
  imports: [OutputIcons, CommentThread],
  templateUrl: './comments.html',
  styleUrl: './comments.css',
})
export class Comments implements OnInit {
    // Inputs properties
    inputComments: InputSignal<ArchiveCommentData[] | undefined> = input.required<ArchiveCommentData[] | undefined>();
    chapterName: InputSignal<string | undefined> = input.required<string | undefined>();

    // Defaults properties
    defaultThreadDepthLimit: number = 10;
    defaultThreadDisplayLimit: number = 10;
    someThreadsExceedDepthLimit: WritableSignal<boolean> = signal<boolean>(false);
    threadsExceedDisplayLimit: WritableSignal<boolean> = signal<boolean>(false);

    // General properties
    commentDisplay: WritableSignal<ArchiveCommentData[]> = signal<ArchiveCommentData[]>([]);
    commentsSelected: WritableSignal<boolean> = signal<boolean>(false);

    ngOnInit(): void {
        // Create a display version of the threads
        let ic = this.inputComments();
        if (ic) {
            if (ic.length > this.defaultThreadDisplayLimit) {
                this.commentDisplay.set(ic.slice(0, this.defaultThreadDisplayLimit));
                this.threadsExceedDisplayLimit.set(true);
            }
            else {
                this.commentDisplay.set(ic);
            }
        }
    }

    flipCommentsSelected() {
        this.commentsSelected.set(!this.commentsSelected());
    }
}
