package org.fenixedu.academic.task;

import org.fenixedu.academic.domain.EnrolmentEvaluation;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.MarkSheet;
import org.fenixedu.academic.util.EnrolmentEvaluationState;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixMarksheetsWithAnnuledEnrolmentEvaluations extends CustomTask {

    private static final Logger logger = LoggerFactory.getLogger(FixMarksheetsWithAnnuledEnrolmentEvaluations.class);

    @Override
    public void runTask() throws Exception {
        final ExecutionSemester semester = ExecutionSemester.readActualExecutionSemester();
        logger.info("Checking marksheets for " + semester.getQualifiedName());
        semester.getAssociatedExecutionCoursesSet().forEach(this::validate);
    }

    private void validate(final ExecutionCourse ec) {
        ec.getAssociatedMarkSheets().forEach(this::validate);
    }

    private void validate(final MarkSheet ms) {
        ms.getEnrolmentEvaluationsSet().forEach(this::validate);
        logger.info("Validating marksheet with id " + ms.getExternalId());
    }

    private void validate(final EnrolmentEvaluation ee) {
        if (ee.getEnrolmentEvaluationState().equals(EnrolmentEvaluationState.ANNULED_OBJ)) {
            logger.error("Found a student with annulled enrolment evaluation - id: " + ee.getExternalId());
            logger.error("Current grade: " + ee.getGradeValue());
            if(ee.getSpecialSeasonEnrolmentEvent() != null){
                logger.error("Special Season Event with id: " + ee.getSpecialSeasonEnrolmentEvent().getExternalId());
            }
        }
    }

}