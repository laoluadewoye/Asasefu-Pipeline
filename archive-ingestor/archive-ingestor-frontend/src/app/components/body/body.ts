import { Component, input, InputSignal, OnInit, signal, WritableSignal } from '@angular/core';
import { Tester } from "./tester/tester";
import { Settings } from "./settings/settings";

@Component({
  selector: 'app-body',
  imports: [Tester, Settings],
  templateUrl: './body.html',
  styleUrl: './body.css',
})
export class Body implements OnInit {
    parentDefaultValue: InputSignal<string> = input.required<string>();
    defaultValue: WritableSignal<string> = signal<string>("");
    completedSessionIds: WritableSignal<string[]> = signal<string[]>([]);

    ngOnInit() {
        this.defaultValue.set(this.parentDefaultValue());
    }

    addCompletedSession(curSessionId: string) {
        let curSessionIdList: string[] = this.completedSessionIds();
        curSessionIdList.push(curSessionId);
        this.completedSessionIds.set(curSessionIdList);
    }
}
