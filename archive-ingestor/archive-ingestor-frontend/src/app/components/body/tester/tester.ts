import { Component, inject, signal, OnInit, input, InputSignal, WritableSignal } from '@angular/core';
import { ArchiveServerTestService } from '../../../services/archive-server-test';
import { ArchiveServerTestData } from '../../../models/archive-server-test-data';
import { catchError, of } from 'rxjs';

@Component({
  selector: 'app-tester',
  imports: [],
  templateUrl: './tester.html',
  styleUrl: './tester.css',
})
export class Tester implements OnInit {
    parentDefaultValue: InputSignal<string> = input.required<string>();
    parentDefaultTimeoutMilli: InputSignal<number> = input.required<number>();

    archiveServerTestService: ArchiveServerTestService = inject(ArchiveServerTestService);
    archiveServerTestData: WritableSignal<ArchiveServerTestData> = signal<ArchiveServerTestData>({testData: ""});
    buttonText: string = "Test API";
    buttonPressed: WritableSignal<boolean> = signal(false);

    ngOnInit(): void {
        this.archiveServerTestData.set({testData: this.parentDefaultValue()});
    }

    onTesterButtonClick() {
        this.archiveServerTestService.getArchiveServerTestData().pipe(
            catchError((err) => {
                let backupTestData = new ArchiveServerTestData();
                backupTestData.testData = "";
                return of(backupTestData);
            })
        ).subscribe((result) => {
            this.archiveServerTestData.set(result);
        })

        this.buttonPressed.set(true);

        setTimeout(() => {
            this.buttonPressed.set(false);
            this.archiveServerTestData.set({testData: this.parentDefaultValue()});
        }, this.parentDefaultTimeoutMilli());
    }
}
