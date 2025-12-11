import { Component, inject, OnInit, signal } from '@angular/core';
import { VersionService } from '../../services/spec';
import { VersionInfo } from '../../models/spec_info';
import { catchError } from 'rxjs';

@Component({
  selector: 'app-footer',
  imports: [],
  templateUrl: './footer.html',
  styleUrl: './footer.css',
})
export class Footer implements OnInit {
    versionService = inject(VersionService);
    versionInfo = signal<VersionInfo>({
        archiveIngestorVersion: "[Not Available]", 
        latestOTWArchiveSupported: "[Not Available]"
    });

    ngOnInit(): void {
        this.versionService.getArchiveIngestorInfo().pipe(
            catchError((err) => {
                console.log(err);
                throw err;
            })
        ).subscribe((result) => {
            this.versionInfo.set(result);
        });
    }
}
