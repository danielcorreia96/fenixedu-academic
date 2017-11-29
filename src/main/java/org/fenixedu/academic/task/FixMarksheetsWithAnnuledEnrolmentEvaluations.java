package org.fenixedu.academic.task;

import org.fenixedu.academic.domain.EnrolmentEvaluation;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.MarkSheet;
import org.fenixedu.academic.util.EnrolmentEvaluationState;
import org.fenixedu.bennu.scheduler.custom.CustomTask;

public class FixMarksheetsWithAnnuledEnrolmentEvaluations extends CustomTask {

    @Override
    public void runTask() throws Exception {
        final ExecutionSemester semester = ExecutionSemester.readActualExecutionSemester();
        taskLog("Checking marksheets for %s\n", semester.getQualifiedName());
        semester.getAssociatedExecutionCoursesSet().forEach(this::validate);
    }

    private void validate(final ExecutionCourse ec) {
        ec.getAssociatedMarkSheets().forEach(this::validate);
    }

    private void validate(final MarkSheet ms) {
        taskLog("Validating marksheet with id %s\n", ms.getExternalId());
        ms.getEnrolmentEvaluationsSet().forEach(this::validate);
        if (ms.getEnrolmentEvaluationsSet().isEmpty()) {
            taskLog("Marksheet became empty after validation...deleting it");
            ms.delete();
        }
    }

    private void validate(final EnrolmentEvaluation ee) {
        if (ee.getEnrolmentEvaluationState().equals(EnrolmentEvaluationState.ANNULED_OBJ)) {
            taskLog("\tFound a student with annulled enrolment evaluation - id: %s\n", ee.getExternalId());
            taskLog("\tCurrent grade: %s\n", ee.getGradeValue());
            taskLog("\tStudent number: %d\n", ee.getStudentCurricularPlan().getStudent().getStudent().getStudentNumber().getNumber());
            ee.removeFromMarkSheet();
            taskLog("Removed student from marksheet");
        }
    }
}