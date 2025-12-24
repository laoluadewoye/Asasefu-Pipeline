import { Component, input, InputSignal, OnInit, output, OutputEmitterRef, signal, WritableSignal } from '@angular/core';
import { ArchiveCommentsData } from '../../../../models/archive-comments-data';
import { ArchiveCommentUnit } from '../../../../models/archive-comment-unit';
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
    inputComments: InputSignal<ArchiveCommentsData | undefined> = input.required<ArchiveCommentsData | undefined>();
    chapterName: InputSignal<string | undefined> = input.required<string | undefined>();

    // Output properties
    formattingFinished: OutputEmitterRef<ArchiveCommentUnit[]> = output<ArchiveCommentUnit[]>();

    // Defaults properties
    defaultThreadDepthLimit: number = 10;
    defaultThreadDisplayLimit: number = 10;
    someThreadsExceedDepthLimit: WritableSignal<boolean> = signal<boolean>(false);
    threadsExceedDisplayLimit: WritableSignal<boolean> = signal<boolean>(false);

    // General properties
    commentThreads: WritableSignal<ArchiveCommentUnit[]> = signal<ArchiveCommentUnit[]>([]);
    commentThreadsDisplay: WritableSignal<ArchiveCommentUnit[]> = signal<ArchiveCommentUnit[]>([]);
    commentsSelected: WritableSignal<boolean> = signal<boolean>(false);

    ngOnInit(): void {
        if (this.inputComments()) {
            // Create the comment threads
            let pageNumbers = Object.keys(this.inputComments()?.pages);
            let ct = this.commentThreads();
            
            pageNumbers.forEach((pageNumber) => {
                let pageContent = this.inputComments()?.pages[pageNumber]
                let pageContentIds = Object.keys(pageContent);
                pageContentIds.forEach((pageContentId) => {
                    let comment: any = pageContent[pageContentId];
                    ct.push(this.createNewArchiveCommentUnit(comment, 1));
                })
            });

            this.commentThreads.set(ct);

            // Create a display version of the threads
            if (this.commentThreads().length > this.defaultThreadDisplayLimit) {
                this.commentThreadsDisplay.set(this.commentThreads().slice(0, this.defaultThreadDisplayLimit));
                this.threadsExceedDisplayLimit.set(true);
            }
            else {
                this.commentThreadsDisplay.set(this.commentThreads());
            }

            // Emit the formatted comment threads
            this.formattingFinished.emit(this.commentThreads());
        }
    }

    createNewArchiveCommentUnit(comment: any, newDepth: number) {
        // Get comment itself
        let newUser: string = comment["user"] ? comment["user"] : "No user found";
        let newPosted: string = comment["posted"] ? comment["posted"] : "No timestamp found";
        let newText: string[] = comment["text"] ? comment["text"] : "No comment text found";

        // Get replies
        let newReplies: ArchiveCommentUnit[] = [];
        if (comment["threads"]) {
            let pageContentIds = Object.keys(comment["threads"]);
            pageContentIds.forEach((pageContentId) => {
                let subcomment: any = comment["threads"][pageContentId];
                newReplies.push(this.createNewArchiveCommentUnit(subcomment, newDepth+1));
            })
        }

        // Check the thread depth flag if needed
        if (newDepth > this.defaultThreadDepthLimit) {
            this.someThreadsExceedDepthLimit.set(true);
        }

        return new ArchiveCommentUnit({
            user: newUser, posted: newPosted, text: newText, depth:newDepth, replies: newReplies
        })
    }

    flipCommentsSelected() {
        this.commentsSelected.set(!this.commentsSelected());
    }
}
