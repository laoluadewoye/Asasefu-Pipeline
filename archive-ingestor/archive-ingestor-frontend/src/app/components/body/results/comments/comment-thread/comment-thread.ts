import { Component, input, InputSignal, OnInit, signal, WritableSignal } from '@angular/core';
import { ArchiveCommentUnit } from '../../../../../models/archive-comment-unit';
import { OutputIcons } from "../../output-icons/output-icons";

@Component({
  selector: 'app-comment-thread',
  imports: [OutputIcons],
  templateUrl: './comment-thread.html',
  styleUrl: './comment-thread.css',
})
export class CommentThread implements OnInit {
    inputCommentUnit: InputSignal<ArchiveCommentUnit> = input.required<ArchiveCommentUnit>();
    parentDefaultThreadDepthLimit: InputSignal<number> = input.required<number>();
    defaultThreadDepthLimit: WritableSignal<number> = signal<number>(-1);
    commentThreadSelected: WritableSignal<boolean> = signal<boolean>(false);

    ngOnInit(): void {
        this.defaultThreadDepthLimit.set(this.parentDefaultThreadDepthLimit());
    }

    flipCommentThreadSelected() {
        this.commentThreadSelected.set(!this.commentThreadSelected());
    }
}
