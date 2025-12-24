import { Component, input, InputSignal, OnInit, signal, WritableSignal } from '@angular/core';
import { OutputIcons } from '../output-icons/output-icons';

@Component({
  selector: 'app-output-list',
  imports: [OutputIcons],
  templateUrl: './output-list.html',
  styleUrl: './output-list.css',
})
export class OutputList implements OnInit {
    inputList: InputSignal<Array<string> | undefined> = input.required<Array<string> | undefined>();
    displayAsList: InputSignal<boolean> = input.required<boolean>();
    displayAsText: InputSignal<boolean> = input.required<boolean>();
    defaultHeader: InputSignal<string> = input.required<string>();
    displayLimit: InputSignal<number> = input.required<number>();
    displaySeperator: InputSignal<string> = input.required<string>();

    fullList: WritableSignal<Array<string>> = signal<Array<string>>([]);
    subjectList: WritableSignal<Array<string>> = signal<Array<string>>([]);
    subjectText: WritableSignal<string> = signal<string>("");
    listTruncated: WritableSignal<boolean> = signal<boolean>(false);

    ngOnInit(): void {
        let il = this.inputList();
        if (il) {
            this.fullList.set(il);
            if (il.length > this.displayLimit()) {
                this.listTruncated.set(true);
                this.subjectList.set(il.slice(0, this.displayLimit()));
                this.subjectText.set(il.slice(0, this.displayLimit()).join(this.displaySeperator()));
            }
            else {
                this.subjectList.set(il);
                this.subjectText.set(il.join(this.displaySeperator()));
            }
        }
    }
}
