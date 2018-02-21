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
package org.fenixedu.academic.service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.util.MessageResources;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.academic.domain.WrittenTest;
import org.fenixedu.academic.domain.person.RoleType;
import org.fenixedu.academic.util.Bundle;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.template.DeclareMessageTemplate;
import org.fenixedu.messaging.core.template.TemplateParameter;
import org.fenixedu.spaces.core.service.NotificationService;
import org.fenixedu.spaces.domain.Space;
import org.fenixedu.spaces.domain.occupation.Occupation;
import org.fenixedu.spaces.domain.occupation.requests.OccupationComment;
import org.fenixedu.spaces.domain.occupation.requests.OccupationRequest;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;

import com.google.common.base.Strings;

@DeclareMessageTemplate(id = "requestRoom.message.template",
        description = "requestRoom.message.description",
        subject = "requestRoom.message.subject",
        text = "requestRoom.message.body",
        parameters = {
                @TemplateParameter(id = "test", description = "requestRoom.message.parameter.test"),
                @TemplateParameter(id = "courseNames", description = "requestRoom.message.parameter.courseNames"),
                @TemplateParameter(id = "degreeNames", description = "requestRoom.message.parameter.degreeNames"),
        },
        bundle = Bundle.RESOURCE_ALLOCATION
)
@DeclareMessageTemplate(id = "requestChangeRoom.message.template",
        description = "requestChangeRoom.message.description",
        subject = "requestChangeRoom.message.subject",
        text = "requestChangeRoom.message.body",
        parameters = {
                @TemplateParameter(id = "test", description = "requestChangeRoom.message.parameter.test"),
                @TemplateParameter(id = "courseNames", description = "requestChangeRoom.message.parameter.courseNames"),
                @TemplateParameter(id = "degreeNames", description = "requestChangeRoom.message.parameter.degreeNames"),
                @TemplateParameter(id = "oldTestDates", description = "requestChangeRoom.message.parameter.oldTestDates")
        },
        bundle = Bundle.RESOURCE_ALLOCATION
)
public class GOPSendMessageService implements NotificationService {


    private static final Logger logger = LoggerFactory.getLogger(GOPSendMessageService.class);

    private static org.fenixedu.messaging.core.domain.Sender GOP_SENDER = null;

    private static org.fenixedu.messaging.core.domain.Sender getGOPSender() {
        if (GOP_SENDER == null) {
            GOP_SENDER = initGOPSender();
            if (GOP_SENDER == null) {
                logger.warn("WARN: GOPSender couldn't be found, using SystemSender ...");
                GOP_SENDER = org.fenixedu.messaging.core.domain.MessagingSystem.systemSender();
            }
        }
        return GOP_SENDER;
    }

    private static org.fenixedu.messaging.core.domain.Sender initGOPSender() {
        for (org.fenixedu.messaging.core.domain.Sender sender : org.fenixedu.messaging.core.domain.Sender.available()) {
            final Group members = sender.getMembers();
            if (members.equals(RoleType.RESOURCE_ALLOCATION_MANAGER.actualGroup())) {
                return sender;
            }
        }
        return null;
    }

    @Atomic
    public static void requestRoom(WrittenTest test) {

        final Set<String> courseNames = new HashSet<>();
        final Set<String> degreeNames = new HashSet<>();
        final Set<ExecutionDegree> degrees = new HashSet<>();
        for (ExecutionCourse course : test.getAssociatedExecutionCoursesSet()) {
            courseNames.add(course.getName());
            degreeNames.add(course.getDegreePresentationString());
            degrees.addAll(course.getExecutionDegrees());
        }

        for (String email : getGOPEmail(degrees)) {
            Message.from(getGOPSender())
                    .replyToSender()
                    .singleBccs(email)
                    .template("requestRoom.message.template")
                        .parameter("test",test)
                        .parameter("courseNames", courseNames)
                        .parameter("degreeNames", degreeNames)
                        .and()
                    .wrapped()
                    .send();
        }
        test.setRequestRoomSentDate(new DateTime());
    }

    @Atomic
    public static void requestChangeRoom(WrittenTest test, Date oldDay, Date oldBeginning, Date oldEnd) {

        final Set<String> courseNames = new HashSet<String>();
        final Set<String> degreeNames = new HashSet<String>();
        final Set<ExecutionDegree> degrees = new HashSet<ExecutionDegree>();
        for (ExecutionCourse course : test.getAssociatedExecutionCoursesSet()) {
            courseNames.add(course.getName());
            degreeNames.add(course.getDegreePresentationString());
            degrees.addAll(course.getExecutionDegrees());
        }

        for (String email : getGOPEmail(degrees)) {
            Message.from(getGOPSender())
                    .replyToSender()
                    .singleTos(email)
                    .template("requestChangeRoom.message.template")
                        .parameter("test",test)
                        .parameter("courseNames", courseNames)
                        .parameter("degreeNames", degreeNames)
                        .parameter("oldTestDates", Arrays.asList(oldDay,oldBeginning,oldEnd))
                        .and()
                    .wrapped()
                    .send();
        }
        test.setRequestRoomSentDate(new DateTime());
    }

    private static Set<String> getGOPEmail(Collection<ExecutionDegree> degrees) {
        Set<String> emails = new HashSet<String>();
        for (ExecutionDegree executionDegree : degrees) {
            String emailFromApplicationResources =
                    BundleUtil.getString(Bundle.APPLICATION, "email.gop." + executionDegree.getCampus().getName());
            if (!StringUtils.isEmpty(emailFromApplicationResources)) {
                emails.add(emailFromApplicationResources);
            }
        }
        return emails;
    }

    @Override
    public boolean notify(OccupationRequest request) {
        MessageResources messages = MessageResources.getMessageResources("resources/ResourceAllocationManagerResources");
        StringBuilder body = new StringBuilder(messages.getMessage("message.room.reservation.solved") + "\n\n" + messages
                .getMessage("message.room.reservation.request.number") + "\n" + request.getIdentification() + "\n\n");
        body.append(messages.getMessage("message.room.reservation.request")).append("\n");
        if (request.getSubject() != null) {
            body.append(request.getSubject());
        } else {
            body.append("-");
        }
        body.append("\n\n").append(messages.getMessage("label.rooms.reserve.periods")).append(":");
        for (Occupation occupation : request.getOccupationSet()) {
            body.append("\n\t").append(occupation.getSummary()).append(" - ")
                    .append(occupation.getSpaces().stream().map(Space::getName).collect(Collectors.joining(" ")));
        }
        if (request.getOccupationSet().isEmpty()) {
            body.append("\n").append(messages.getMessage("label.rooms.reserve.periods.none"));
        }
        body.append("\n\n").append(messages.getMessage("message.room.reservation.description")).append("\n");
        if (request.getDescription() != null) {
            body.append(request.getDescription());
        } else {
            body.append("-");
        }
        OccupationComment occupationComment =
                request.getCommentSet().stream().sorted(OccupationComment.COMPARATOR_BY_INSTANT.reversed()).findFirst().get();

        body.append("\n\n").append(messages.getMessage("message.room.reservation.last.comment")).append("\n");

        body.append(occupationComment.getDescription());
        sendEmail(request.getRequestor().getPerson().getEmailForSendingEmails(), messages.getMessage("message.room.reservation"),
                body.toString());
        return true;
    }

    @Override
    public boolean sendEmail(String emails, String subject, String body) {
        if (!Strings.isNullOrEmpty(emails)) {
            Message.from(getGOPSender())
                    .replyToSender()
                    .singleBccs(emails)
                    .subject(subject)
                    .textBody(body)
                    .send();
            return true;
        }
        return false;
    }
}
