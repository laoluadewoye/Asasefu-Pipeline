import { Component, inject, signal } from '@angular/core';
import { ArchiveServerTestService } from '../../../services/archive-server-test';
import { ArchiveServerTestData } from '../../../models/archive-server-test-data';
import { catchError } from 'rxjs';

@Component({
  selector: 'app-tester',
  imports: [],
  templateUrl: './tester.html',
  styleUrl: './tester.css',
})
export class Tester {
    archiveServerTestService = inject(ArchiveServerTestService);
    archiveServerTestData = signal<ArchiveServerTestData>({testData: ""});
    buttonText = "Test API";

    onTesterButtonClick() {
        this.archiveServerTestService.getArchiveServerTestData().pipe(
            catchError((err) => {
                console.log(err);
                throw err;
            })
        ).subscribe((result) => {
            this.archiveServerTestData.set(result);
        })
    }
}
