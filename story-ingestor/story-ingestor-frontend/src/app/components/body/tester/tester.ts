import { Component, inject, signal } from '@angular/core';
import { ArchiveServerTestService } from '../../../services/archiveServerTest';
import { ArchiveServerTestData } from '../../../models/archiveServerTestData';
import { catchError } from 'rxjs';

@Component({
  selector: 'app-tester',
  imports: [],
  templateUrl: './tester.html',
  styleUrl: './tester.css',
})
export class Tester {
    archiveServerTestService = inject(ArchiveServerTestService);
    archiveServerTestData = signal<ArchiveServerTestData>({testData: ""})

    onTesterButtonClick() {
        this.archiveServerTestService.getArchiveIngestorTestData().pipe(
            catchError((err) => {
                console.log(err);
                throw err;
            })
        ).subscribe((result) => {
            this.archiveServerTestData.set(result);
        })
    }
}
