/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Academic.
 *
 * FenixEdu Academic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Academic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Academic.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.academic.service.services.scientificCouncil.thesis;

import java.util.Calendar;
import java.util.Locale;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.thesis.Thesis;
import org.fenixedu.academic.domain.thesis.ThesisState;
import org.fenixedu.academic.predicate.AccessControl;
import org.fenixedu.academic.util.Bundle;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.template.DeclareMessageTemplate;
import org.fenixedu.messaging.core.template.TemplateParameter;

import pt.ist.fenixframework.Atomic;

@DeclareMessageTemplate(id = "thesis.proposal.jury.approve",
        description = "thesis.proposal.jury.approve.description",
        subject = "thesis.proposal.jury.approve.subject",
        text = "thesis.proposal.jury.approve.body",
        parameters = {
            @TemplateParameter(id = "degreeName", description = "thesis.proposal.jury.approve.param.degreeName"),
            @TemplateParameter(id = "thesis", description = "thesis.proposal.jury.approve.param.thesis"),
            @TemplateParameter(id = "orientationName", description = "thesis.proposal.jury.approve.param.orientationName"),
            @TemplateParameter(id = "institutionName", description = "thesis.proposal.jury.approve.param.institutionName"),
            @TemplateParameter(id = "day", description = "thesis.proposal.jury.approve.param.day"),
            @TemplateParameter(id = "month", description = "thesis.proposal.jury.approve.param.month"),
            @TemplateParameter(id = "year", description = "thesis.proposal.jury.approve.param.year"),
            @TemplateParameter(id = "currentPerson", description = "thesis.proposal.jury.approve.param.currentPerson"),
        },
        bundle = Bundle.MESSAGING
)
public class ApproveThesisProposal extends ThesisServiceWithMailNotification {

    @Override
    void process(Thesis thesis) {
        if (thesis.getState() != ThesisState.APPROVED) {
            thesis.approveProposal();
        }
    }

    @Override
    public void sendEmail(Thesis thesis) {
        // Prepare template parameters
        Locale locale = I18N.getLocale();
        ExecutionYear executionYear = ExecutionYear.readCurrentExecutionYear();
        String degreeName = thesis.getDegree().getNameFor(executionYear).getContent();
        String orientationName = thesis.getOrientation().stream().map(p -> p.getName() + ", " + p.getAffiliation()).collect(Collectors.joining("\n"));
        String institutionName = Bennu.getInstance().getInstitutionUnit().getPartyName().getContent(locale);
        Calendar today = Calendar.getInstance(locale);

        // Send e-mail message using template
        Message.from(AccessControl.getPerson().getSender())
                .singleBcc(getReceiversEmails(thesis))
                .template("thesis.proposal.jury.approve")
                    .parameter("degreeName", degreeName)
                    .parameter("thesis", thesis)
                    .parameter("orientationName", orientationName)
                    .parameter("institutionName", institutionName)
                    .parameter("day", today.get(Calendar.DAY_OF_MONTH))
                    .parameter("month", today.getDisplayName(Calendar.MONTH, Calendar.LONG, locale))
                    .parameter("year", today.get(Calendar.YEAR))
                    .parameter("currentPerson", AccessControl.getPerson())
                .and()
                .send();
    }

    @Override
    protected String getSubject(Thesis thesis) {
        return "";
    }

    @Override
    protected String getMessage(Thesis thesis) {
        return "";
    }

    // Service Invokers migrated from Berserk

    private static final ApproveThesisProposal serviceInstance = new ApproveThesisProposal();

    @Atomic
    public static void runApproveThesisProposal(Thesis thesis) {
        serviceInstance.run(thesis);
    }

}
