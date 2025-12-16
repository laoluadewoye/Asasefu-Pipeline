import { AbstractControl, ValidatorFn, ValidationErrors, FormGroup } from "@angular/forms";

export function linkPatternValidator(control: AbstractControl) {
    const settingsFormGroup = control.parent;
    if (!settingsFormGroup) {
      return null;
    }
    const parseTypeValue: string = settingsFormGroup.get('parseType')?.value;

    const storyLinkPattern: RegExp = new RegExp("^https://archiveofourown\\.org/works/[0-9]+$");
    const chapterLinkPattern: RegExp = new RegExp("^https://archiveofourown\\.org/works/[0-9]+/chapters/[0-9]+$");

    let result: boolean;
    if (parseTypeValue === "story") {
        result = !storyLinkPattern.test(control.value);
    }
    else {
        result = !(storyLinkPattern.test(control.value) || chapterLinkPattern.test(control.value));
    }
    return result ? {value : control.value} : null;
}
