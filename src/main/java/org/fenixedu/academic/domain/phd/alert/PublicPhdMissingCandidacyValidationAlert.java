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
package org.fenixedu.academic.domain.phd.alert;

import org.fenixedu.academic.domain.phd.PhdIndividualProgramProcess;
import org.fenixedu.academic.domain.phd.candidacy.PhdProgramCandidacyProcess;
import org.fenixedu.academic.util.Bundle;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.messaging.core.domain.Message;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;

public class PublicPhdMissingCandidacyValidationAlert extends PublicPhdMissingCandidacyValidationAlert_Base {

    static private final int INTERVAL = 15; // number of days

    private PublicPhdMissingCandidacyValidationAlert() {
        super();
    }

    public PublicPhdMissingCandidacyValidationAlert(final PhdIndividualProgramProcess process) {
        this();
        init(process, generateSubject(process), generateBody(process));
    }

    private LocalizedString generateSubject(final PhdIndividualProgramProcess process) {
        return process.getCandidacyProcess().getPublicPhdCandidacyPeriod()
                .getEmailMessageSubjectForMissingCandidacyValidation(process);
    }

    private LocalizedString generateBody(final PhdIndividualProgramProcess process) {
        return process.getCandidacyProcess().getPublicPhdCandidacyPeriod()
                .getEmailMessageBodyForMissingCandidacyValidation(process);
    }

    @Override
    protected void generateMessage() {
        Message.from(getSender())
                .singleBccs(getEmail())
                .subject(buildMailSubject())
                .textBody(buildMailBody())
                .send();
    }

    private String getEmail() {
        return getProcess().getPerson().getInstitutionalOrDefaultEmailAddressValue();
    }

    @Override
    public String getDescription() {
        return BundleUtil.getString(Bundle.PHD, String.format("message.phd.missing.candidacy.validation.alert", INTERVAL));
    }

    @Override
    protected boolean isToFire() {
        int days = Days.daysBetween(calculateStartDate().toDateMidnight(), new LocalDate().toDateMidnight()).getDays();
        return days >= INTERVAL;
    }

    private LocalDate calculateStartDate() {
        return getFireDate() != null ? getFireDate().toLocalDate() : getCandidacyProcess().getWhenCreated().toLocalDate();
    }

    private PhdProgramCandidacyProcess getCandidacyProcess() {
        return getProcess().getCandidacyProcess();
    }

    @Override
    protected boolean isToDiscard() {
        return getCandidacyProcess().isValidatedByCandidate() || candidacyPeriodIsOver();
    }

    /*
     * Must exist a candidacy period, otherwise candidacy hash code could not be
     * previously created
     */
    private boolean candidacyPeriodIsOver() {
        return new DateTime().isAfter(getCandidacyProcess().getPublicPhdCandidacyPeriod().getEnd());
    }

    @Override
    public boolean isToSendMail() {
        return true;
    }

    @Override
    public boolean isSystemAlert() {
        return true;
    }
}
