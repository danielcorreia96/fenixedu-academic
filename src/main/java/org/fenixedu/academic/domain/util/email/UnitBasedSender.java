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

import org.fenixedu.academic.domain.accessControl.UnitGroup;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.bennu.core.groups.Group;

import pt.ist.fenixframework.Atomic;

public class UnitBasedSender extends UnitBasedSender_Base {

    public void init(final Unit unit, final String fromAddress, final Group members) {
        setUnit(unit);
        setAddress(fromAddress);
        setMembers(members);
    }

    public UnitBasedSender(final Unit unit, final String fromAddress, final Group members) {
        super();
        init(unit, fromAddress, members);
    }

    public UnitBasedSender() {
        super();
    }

    @Override
    public void delete() {
        setUnit(null);
        super.delete();
    }

    @Override
    public String getName() {
        return String.format("%s (%s)", Unit.getInstitutionAcronym(), getUnit().getName());
    }

    public String getReplyTosSet() {
        if (super.getReplyTo().isEmpty()) {
            setReplyTo(new CurrentUserReplyTo().getReplyToAddress());
        }
        return super.getReplyTo();
    }

    @Atomic
    private void createCurrentUserReplyTo() {
        setReplyTo(new CurrentUserReplyTo().getReplyToAddress());
    }

    @Override
    public void setName(final String fromName) {
        throw new Error("method.not.available.for.this.type.of.sender");
    }

    @Atomic
    @Override
    public void addRecipient(final Group recipients) {
        super.addRecipient(recipients);
    }

    @Atomic
    @Override
    public void removeRecipient(final Group recipients) {
        super.removeRecipient(recipients);
    }

    protected void createRecipient(final Group group) {
        addRecipient(group);
    }

    @Atomic
    public static UnitBasedSender newInstance(Unit unit) {
        return new UnitBasedSender(unit, Sender.getNoreplyMail(), UnitGroup.recursiveWorkers(unit));
    }

}
