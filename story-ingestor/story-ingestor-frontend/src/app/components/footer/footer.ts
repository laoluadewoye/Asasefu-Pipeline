import { Component, inject, OnInit, signal } from '@angular/core';
import { Version } from '../../services/version';
import { VersionInfo } from '../../models/version.type';
import { catchError } from 'rxjs';

@Component({
  selector: 'app-footer',
  imports: [],
  templateUrl: './footer.html',
  styleUrl: './footer.css',
})
export class Footer implements OnInit {
    versionService = inject(Version);
    versionInfo = signal<VersionInfo>({archiveIngestorVersion: "0.0", latestOTWArchiveSupported: "otwarchive v0"});

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
