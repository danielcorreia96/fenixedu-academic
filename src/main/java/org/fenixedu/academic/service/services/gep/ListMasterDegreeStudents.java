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
package org.fenixedu.academic.service.services.gep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.StudentCurricularPlan;
import org.fenixedu.academic.domain.degree.degreeCurricularPlan.DegreeCurricularPlanState;
import org.fenixedu.academic.domain.studentCurricularPlan.Specialization;
import org.fenixedu.academic.dto.InfoStudentCurricularPlanWithFirstTimeEnrolment;

import pt.ist.fenixframework.Atomic;

/**
 * 
 * @author - Shezad Anavarali (shezad@ist.utl.pt)
 * 
 */
public class ListMasterDegreeStudents {

    @Atomic
    public static Collection run(String executionYearName) {
        final ExecutionYear executionYear = ExecutionYear.readExecutionYearByName(executionYearName);

        final Collection<DegreeCurricularPlan> masterDegreeCurricularPlans = readByDegreeTypeAndState();
        CollectionUtils.filter(masterDegreeCurricularPlans, (Predicate) arg0 -> {
            DegreeCurricularPlan degreeCurricularPlan = (DegreeCurricularPlan) arg0;
            return degreeCurricularPlan.getExecutionDegreesSet().stream()
                    .anyMatch(executionDegree -> executionDegree.getExecutionYear().equals(executionYear));
        });

        final Collection<StudentCurricularPlan> studentCurricularPlans = new ArrayList();
        for (DegreeCurricularPlan degreeCurricularPlan : masterDegreeCurricularPlans) {
            studentCurricularPlans.addAll(degreeCurricularPlan.getStudentCurricularPlansSet());
        }

        final Collection<InfoStudentCurricularPlanWithFirstTimeEnrolment> infoStudentCurricularPlans = new ArrayList();
        for (StudentCurricularPlan studentCurricularPlan : studentCurricularPlans) {

            if (!studentCurricularPlan.isActive()) {
                continue;
            }

            boolean firstTimeEnrolment = true;
            if (studentCurricularPlan.getSpecialization() != null
                    && studentCurricularPlan.getSpecialization().equals(Specialization.STUDENT_CURRICULAR_PLAN_MASTER_DEGREE)) {

                Collection<StudentCurricularPlan> previousStudentCurricularPlans =
                        studentCurricularPlan.getRegistration().getStudentCurricularPlansBySpecialization(
                                Specialization.STUDENT_CURRICULAR_PLAN_MASTER_DEGREE);

                previousStudentCurricularPlans.remove(studentCurricularPlan);
                for (StudentCurricularPlan previousStudentCurricularPlan : previousStudentCurricularPlans) {
                    if (previousStudentCurricularPlan.getDegreeCurricularPlan().getDegree()
                            .equals(studentCurricularPlan.getDegreeCurricularPlan().getDegree())) {
                        firstTimeEnrolment = false;
                        break;
                    }
                }
            } else if (studentCurricularPlan.getSpecialization() != null
                    && studentCurricularPlan.getSpecialization().equals(Specialization.STUDENT_CURRICULAR_PLAN_SPECIALIZATION)) {
                if (!studentCurricularPlan.getDegreeCurricularPlan().getFirstExecutionDegree().getExecutionYear()
                        .equals(executionYear)) {
                    continue;
                }
            }

            if (firstTimeEnrolment) {
                if (!studentCurricularPlan.getDegreeCurricularPlan().getFirstExecutionDegree().getExecutionYear()
                        .equals(executionYear)) {
                    firstTimeEnrolment = false;
                }
            }

            InfoStudentCurricularPlanWithFirstTimeEnrolment infoStudentCurricularPlan =
                    InfoStudentCurricularPlanWithFirstTimeEnrolment.newInfoFromDomain(studentCurricularPlan);
            infoStudentCurricularPlan.setFirstTimeEnrolment(firstTimeEnrolment);
            infoStudentCurricularPlans.add(infoStudentCurricularPlan);
        }

        return infoStudentCurricularPlans;

    }

    private static List<DegreeCurricularPlan> readByDegreeTypeAndState() {
        return DegreeCurricularPlan.readNotEmptyDegreeCurricularPlans()
                .stream()
                .filter(degreeCurricularPlan -> degreeCurricularPlan.getDegree().getDegreeType().isPreBolonhaMasterDegree())
                .filter(degreeCurricularPlan -> degreeCurricularPlan.getState() == DegreeCurricularPlanState.ACTIVE)
                .collect(Collectors.toList());
    }
}