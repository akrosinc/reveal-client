package org.smartregister.service.formsubmissionhandler;

import org.smartregister.domain.form.FormSubmission;
import org.smartregister.service.MotherService;

public class IFAHandler implements FormSubmissionHandler {
    private final MotherService motherService;

    public IFAHandler(MotherService motherService) {
        this.motherService = motherService;
    }

    @Override
    public void handle(FormSubmission submission) {
        motherService.ifaTabletsGiven(submission);
    }
}
