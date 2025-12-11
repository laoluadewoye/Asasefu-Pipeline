import { Component, inject, signal } from '@angular/core';
import { TestAPIService } from '../../../services/test';
import { TestAPIInfo } from '../../../models/test_api_info';
import { catchError } from 'rxjs';

@Component({
  selector: 'app-testapi',
  imports: [],
  templateUrl: './testapi.html',
  styleUrl: './testapi.css',
})
export class TestAPI {
    testAPIService = inject(TestAPIService);
    testAPIInfo = signal<TestAPIInfo>({info: ""})

    onTestAPIButtonClick() {
        this.testAPIService.getArchiveIngestorTestAPI().pipe(
            catchError((err) => {
                console.log(err);
                throw err;
            })
        ).subscribe((result) => {
            this.testAPIInfo.set(result);
        })
    }
}
