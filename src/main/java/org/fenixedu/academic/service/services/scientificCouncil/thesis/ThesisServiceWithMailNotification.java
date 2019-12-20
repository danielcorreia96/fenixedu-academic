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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.ScientificCommission;
import org.fenixedu.academic.domain.thesis.Thesis;
import org.fenixedu.academic.domain.thesis.ThesisEvaluationParticipant;
import org.fenixedu.academic.domain.thesis.ThesisParticipationType;
import org.fenixedu.academic.predicate.AccessControl;
import org.fenixedu.academic.util.Bundle;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.messaging.core.domain.Message;

public abstract class ThesisServiceWithMailNotification {

    public void run(Thesis thesis) {
        process(thesis);
        sendEmail(thesis);
    }

    abstract void process(Thesis thesis);

    abstract void sendEmail(Thesis thesis);

    protected String getMessage(String key, Object... args) {
        return getMessage(I18N.getLocale(), key, args);
    }

    protected String getMessage(Locale locale, String key, Object... args) {
        String template = BundleUtil.getString(Bundle.MESSAGING, locale, key);
        return MessageFormat.format(template, args);
    }

    protected abstract String getSubject(Thesis thesis);

    protected abstract String getMessage(Thesis thesis);

    protected Collection<String> getReceiversEmails(Thesis thesis) {
        ThesisParticipationType[] thesisParticipants = {
                ThesisParticipationType.ORIENTATOR, ThesisParticipationType.COORIENTATOR,
                ThesisParticipationType.PRESIDENT, ThesisParticipationType.VOWEL
        };
        Set<String> persons =
                thesis.getAllParticipants(thesisParticipants)
                        .stream()
                        .map(ThesisEvaluationParticipant::getEmail)
                        .collect(Collectors.toSet());
        persons.add(thesis.getStudent().getPerson().getProfile().getEmail());

        // also send proposal approval to the contact team
        ExecutionYear executionYear = thesis.getEnrolment().getExecutionYear();
        for (ScientificCommission member : thesis.getDegree().getScientificCommissionMembers(executionYear)) {
            if (member.isContact()) {
                persons.add(member.getPerson().getProfile().getEmail());
            }
        }

        return persons;
    }

    //
    // Utility methods
    //

    protected static Set<Person> personSet(Person... persons) {
        return Arrays.stream(persons).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    protected static Person getPerson(ThesisEvaluationParticipant participant) {
        return participant == null ? null : participant.getPerson();
    }

}
