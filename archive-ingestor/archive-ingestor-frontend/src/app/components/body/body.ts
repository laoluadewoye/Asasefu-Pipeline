import { Component, input, InputSignal, OnInit, signal, WritableSignal } from '@angular/core';
import { Tester } from "./tester/tester";
import { Settings } from "./settings/settings";
import { Results } from './results/results';

@Component({
  selector: 'app-body',
  imports: [Tester, Settings, Results],
  templateUrl: './body.html',
  styleUrl: './body.css',
})
export class Body implements OnInit {
    // Default value
    parentDefaultValue: InputSignal<string> = input.required<string>();
    defaultValue: WritableSignal<string> = signal<string>("");

    // Default timer
    defaultTimeoutMilli: WritableSignal<number> = signal<number>(5000);

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
