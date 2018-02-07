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
package org.fenixedu.academic.domain.util.email;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.academic.util.Bundle;
import org.fenixedu.academic.util.EMail;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.core.domain.Message;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

public class EmailBean implements Serializable {

    private org.fenixedu.messaging.core.domain.Sender sender;
    private Set<Group> recipients;
    private Set<String> tos, ccs, bccs;
    private LocalizedString subject, message, htmlMessage;
    private String replyTos;
    private DateTime createdDate;

    public EmailBean() {
    }

    public EmailBean(final Message message) {
        this.subject = message.getSubject();
        this.message = message.getTextBody();
        this.htmlMessage = message.getHtmlBody();
        this.bccs = message.getBccs();
        this.createdDate = message.getCreated();
    }

    public org.fenixedu.messaging.core.domain.Sender getSender() {
        return sender;
    }

    public void setSender(final org.fenixedu.messaging.core.domain.Sender sender) {
        this.sender = sender;
    }

    public Set<Group> getRecipients() {
        return recipients;
    }

    public void setRecipients(Set<Group> recipients) {
        this.recipients = recipients;
    }

    public String getReplyTos() {
        return this.replyTos;
    }

    public void setReplyTos(String replyTos) {
        this.replyTos = replyTos;
    }

    public Set<String> getTos() {
        return tos;
    }

    public void setTos(Set<String> tos) {
        this.tos = tos;
    }

    public Set<String> getCcs() {
        return ccs;
    }

    public void setCcs(Set<String> ccs) {
        this.ccs = ccs;
    }

    public Set<String> getBccs() {
        return bccs;
    }

    public void setBccs(Set<String> bccs) {
        this.bccs = bccs;
    }

    public LocalizedString getSubject() {
        return subject;
    }

    public void setSubject(LocalizedString subject) {
        this.subject = subject;
    }

    public LocalizedString getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = this.message.with(I18N.getLocale(),message);
    }

    public LocalizedString getHtmlMessage() {
        return htmlMessage;
    }

    public void setHtmlMessage(final LocalizedString htmlMessage) {
        this.htmlMessage = htmlMessage;
    }

    public String validate() {
        String bccs = getBccs().toString();
        if (getRecipients() == null && StringUtils.isEmpty(bccs)) {
            return BundleUtil.getString(Bundle.APPLICATION, "error.email.validation.no.recipients");
        }

        if (!StringUtils.isEmpty(bccs)) {
            String[] emails = bccs.split(",");
            for (String emailString : emails) {
                final String email = emailString.trim();
                if (!email.matches(EMail.W3C_EMAIL_SINTAX_VALIDATOR)) {
                    StringBuilder builder =
                            new StringBuilder(BundleUtil.getString(Bundle.APPLICATION, "error.email.validation.bcc.invalid"));
                    builder.append(email);
                    return builder.toString();
                }
            }
        }

        if (getSubject().isEmpty()) {
            return BundleUtil.getString(Bundle.APPLICATION, "error.email.validation.subject.empty");
        }

        if (getMessage().isEmpty() && getHtmlMessage().isEmpty()) {
            return BundleUtil.getString(Bundle.APPLICATION, "error.email.validation.message.empty");
        }

        return null;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Atomic
    public org.fenixedu.messaging.core.domain.Message send() {
        final StringBuilder message = new StringBuilder();
        if (getMessage() != null && !getMessage().toString().trim().isEmpty()) {
            message.append(getMessage());
            message.append("\n\n---\n");
            message.append(BundleUtil.getString(Bundle.APPLICATION, "message.email.footer.prefix"));
            message.append(" ");
            message.append(getSender().getName());
            message.append(" ");
            message.append(BundleUtil.getString(Bundle.APPLICATION, "message.email.footer.prefix.suffix").toLowerCase());
            for (final Group group : getRecipients()) {
                message.append("\n\t");
                message.append(group.getPresentationName());
            }
            message.append("\n");
        }
        final String bccs = getBccs() == null ? null : getBccs().toString().replace(" ", "");

        return Message.from(getSender())
                .replyTo(getSender().getReplyTo())
                .to(getRecipients())
                .singleBccs(bccs)
                .subject(getSubject())
                .textBody(message.toString())
                .htmlBody(getHtmlMessage())
                .send();
    }

}
