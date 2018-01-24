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

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.accessControl.CoordinatorGroup;
import org.fenixedu.academic.domain.accessControl.StudentGroup;
import org.fenixedu.academic.domain.accessControl.TeacherGroup;
import org.fenixedu.academic.domain.degreeStructure.CycleType;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.person.RoleType;
import org.fenixedu.bennu.core.groups.Group;

import pt.ist.fenixframework.Atomic;

public class CoordinatorSender extends CoordinatorSender_Base {

    public CoordinatorSender(Degree degree) {
        super();
        setDegree(degree);
        setAddress(Sender.getNoreplyMail());
        setReplyTo(new CurrentUserReplyTo().getReplyToAddress());
        setMembers(CoordinatorGroup.get(degree));
        Group current = CoordinatorGroup.get(degree);
        Group teachers = TeacherGroup.get(degree);
        Group students = StudentGroup.get(degree, null);
        for (CycleType cycleType : degree.getDegreeType().getCycleTypes()) {
            addRecipient(StudentGroup.get(degree, cycleType));
        }
        addRecipient(current);
        addRecipient(teachers);
        addRecipient(students);
        addRecipient(RoleType.TEACHER.actualGroup());
        addRecipient(StudentGroup.get());
        setName(createFromName());
    }

    public String createFromName() {
        return String.format("%s (%s: %s)", Unit.getInstitutionAcronym(), getDegree().getSigla(), "Coordenação");
    }

    @Override
    public void delete() {
        setDegree(null);
        super.delete();
    }

    @Atomic
    public static CoordinatorSender newInstance(Degree degree) {
        return degree.getSender() == null ? new CoordinatorSender(degree) : (CoordinatorSender) degree.getSender();
    }

}
