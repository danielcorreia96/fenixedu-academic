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
package org.fenixedu.academic.domain.candidacyProcess.mobility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.ExecutionInterval;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.QueueJob;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.domain.accessControl.academicAdministration.AcademicAccessRule;
import org.fenixedu.academic.domain.accessControl.academicAdministration.AcademicOperationType;
import org.fenixedu.academic.domain.candidacyProcess.CandidacyProcessBean;
import org.fenixedu.academic.domain.candidacyProcess.CandidacyProcessState;
import org.fenixedu.academic.domain.candidacyProcess.IndividualCandidacyPersonalDetails;
import org.fenixedu.academic.domain.candidacyProcess.IndividualCandidacyProcess;
import org.fenixedu.academic.domain.candidacyProcess.erasmus.ErasmusApplyForSemesterType;
import org.fenixedu.academic.domain.candidacyProcess.erasmus.ErasmusCoordinatorBean;
import org.fenixedu.academic.domain.candidacyProcess.erasmus.ErasmusVacancyBean;
import org.fenixedu.academic.domain.candidacyProcess.erasmus.ReceptionEmailExecutedAction;
import org.fenixedu.academic.domain.candidacyProcess.erasmus.SendReceptionEmailBean;
import org.fenixedu.academic.domain.candidacyProcess.erasmus.reports.ErasmusCandidacyProcessReport;
import org.fenixedu.academic.domain.candidacyProcess.secondCycle.SecondCycleIndividualCandidacyProcess;
import org.fenixedu.academic.domain.caseHandling.Activity;
import org.fenixedu.academic.domain.caseHandling.PreConditionNotValidException;
import org.fenixedu.academic.domain.caseHandling.Process;
import org.fenixedu.academic.domain.caseHandling.StartActivity;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.academic.domain.period.MobilityApplicationPeriod;
import org.fenixedu.academic.domain.person.RoleType;
import org.fenixedu.academic.util.Bundle;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.joda.time.DateTime;

public class MobilityApplicationProcess extends MobilityApplicationProcess_Base {

    static final public Comparator<IndividualCandidacyProcess> COMPARATOR_BY_CANDIDACY_PERSON =
            new Comparator<IndividualCandidacyProcess>() {
                @Override
                public int compare(IndividualCandidacyProcess o1, IndividualCandidacyProcess o2) {
                    return IndividualCandidacyPersonalDetails.COMPARATOR_BY_NAME_AND_ID.compare(o1.getPersonalDetails(),
                            o2.getPersonalDetails());
                }
            };

    static private List<Activity> activities = new ArrayList<Activity>();
    static {
        activities.add(new ViewMobilityQuota());
        activities.add(new InsertMobilityQuota());
        activities.add(new RemoveMobilityQuota());
        activities.add(new ViewErasmusCoordinators());
        activities.add(new AssignCoordinator());
        activities.add(new RemoveTeacherFromCoordinators());
        activities.add(new ViewChildProcessWithMissingRequiredDocumentFiles());
        activities.add(new SendEmailToMissingRequiredDocumentsProcesses());
        activities.add(new SendEmailToMissingShiftsProcesses());
        activities.add(new EditCandidacyPeriod());
        activities.add(new SendReceptionEmail());
        activities.add(new EditReceptionEmailMessage());
    }

    public MobilityApplicationProcess() {
        super();
    }

    private MobilityApplicationProcess(final ExecutionYear executionYear, final DateTime start, final DateTime end,
            final ErasmusApplyForSemesterType forSemester) {
        this();
        checkParameters(executionYear, start, end);
        setState(CandidacyProcessState.STAND_BY);
        setForSemester(forSemester);
        new MobilityApplicationPeriod(this, executionYear, start, end);
    }

    public void delete() {
        if (getChildProcessesSet().size() > 0) {
            throw new DomainException("error.mobiliy.application.proccess.cant.be.deleted.it.has.individual.application");
        }
        if (getCoordinatorsSet().size() > 0) {
            throw new DomainException("error.mobiliy.application.proccess.cant.be.deleted.it.has.coordinators");
        }
        if (getCandidacyPeriod() != null) {
            throw new DomainException("error.mobiliy.application.proccess.cant.be.deleted.it.defined.period");
        }
        if (getErasmusCandidacyProcessExecutedActionSet().size() > 0) {
            throw new DomainException("error.mobiliy.application.proccess.cant.be.deleted.it.has.executed.actions");
        }
        if (getErasmusCandidacyProcessReportsSet().size() > 0) {
            throw new DomainException("error.mobiliy.application.proccess.cant.be.deleted.it.has.reports");
        }
        if (getProcessLogsSet().size() > 0) {
            throw new DomainException("error.mobiliy.application.proccess.cant.be.deleted.it.has.logs");
        }
        setForSemester(null);
        setRootDomainObject(null);
        deleteDomainObject();
    }

    public void resetConfigurations() {
        if (getChildProcessesSet().size() > 0) {
            throw new DomainException("error.mobility.application.process.cant.delete.configurations.it.has.applications");
        }
        for (MobilityQuota quota : getCandidacyPeriod().getMobilityQuotasSet()) {
            quota.delete();
        }
        for (MobilityCoordinator coord : getCoordinatorsSet()) {
            coord.delete();
        }
        for (MobilityEmailTemplate template : getApplicationPeriod().getEmailTemplatesSet()) {
            template.delete();
        }
    }

    public void preLoadLastConfigurations() {
        // Get very last process (independently of its season, 1st or 2nd
        // semester)
        MobilityApplicationProcess lastProcess = getLastSeasonProcess(null);

        // Copy all openings from previous process
        Set<MobilityQuota> lastSeasonQuotas = lastProcess.getCandidacyPeriod().getMobilityQuotasSet();
        for (MobilityQuota quota : lastSeasonQuotas) {
            new MobilityQuota(getApplicationPeriod(), quota.getDegree(), quota.getMobilityAgreement(),
                    quota.getNumberOfOpenings());
        }

        // Copy all coordinators from previous process
        Set<MobilityCoordinator> lastSeasonCoordinators = lastProcess.getCoordinatorsSet();
        for (MobilityCoordinator coord : lastSeasonCoordinators) {
            new MobilityCoordinator(this, coord.getTeacher(), coord.getDegree());
        }

        // Copy all email templates
        for (MobilityEmailTemplate template : lastProcess.getApplicationPeriod().getEmailTemplatesSet()) {
            MobilityEmailTemplate.create(getApplicationPeriod(), template.getMobilityProgram(), template.getType(),
                    template.getSubject(), template.getBody());
        }
    }

    private MobilityApplicationProcess getLastSeasonProcess(ErasmusApplyForSemesterType forSemester) {
        MobilityApplicationProcess lastProcess = null;
        Boolean lookForSameSeasonType = (forSemester != null);
        for (Process proc : Bennu.getInstance().getProcessesSet()) {
            if (proc instanceof MobilityApplicationProcess) {
                MobilityApplicationProcess mobAppProc = ((MobilityApplicationProcess) proc);
                if (mobAppProc == this) {
                    continue;
                }
                if (lookForSameSeasonType && mobAppProc.getForSemester() != forSemester) {
                    continue;
                }
                if (lastProcess == null) {
                    lastProcess = mobAppProc;
                    continue;
                }
                if (mobAppProc.getCandidacyStart().isAfter(lastProcess.getCandidacyEnd())) {
                    lastProcess = mobAppProc;
                }
            }
        }
        return lastProcess;
    }

    public List<MobilityIndividualApplicationProcess> getValidErasmusIndividualCandidacies() {
        final List<MobilityIndividualApplicationProcess> result = new ArrayList<MobilityIndividualApplicationProcess>();
        for (final IndividualCandidacyProcess child : getChildProcessesSet()) {
            final MobilityIndividualApplicationProcess process = (MobilityIndividualApplicationProcess) child;
            if (process.isCandidacyValid()) {
                result.add(process);
            }
        }
        return result;
    }

    public List<MobilityIndividualApplicationProcess> getValidMobilityIndividualCandidacies(MobilityProgram mobilityProgram) {
        final List<MobilityIndividualApplicationProcess> result = new ArrayList<MobilityIndividualApplicationProcess>();
        for (final IndividualCandidacyProcess child : getChildProcessesSet()) {
            final MobilityIndividualApplicationProcess process = (MobilityIndividualApplicationProcess) child;
            if (process.isCandidacyValid() && process.getMobilityProgram() == mobilityProgram) {
                result.add(process);
            }
        }
        return result;
    }

    public List<MobilityIndividualApplicationProcess> getValidErasmusIndividualCandidacies(final Degree degree) {
        if (degree == null) {
            return Collections.emptyList();
        }
        final List<MobilityIndividualApplicationProcess> result = new ArrayList<MobilityIndividualApplicationProcess>();
        for (final IndividualCandidacyProcess child : getChildProcessesSet()) {
            final MobilityIndividualApplicationProcess process = (MobilityIndividualApplicationProcess) child;
            if (process.isCandidacyValid() && process.hasCandidacyForSelectedDegree(degree)) {
                result.add(process);
            }
        }
        return result;
    }

    public Map<Degree, SortedSet<MobilityIndividualApplicationProcess>> getValidErasmusIndividualCandidaciesByDegree() {
        final Map<Degree, SortedSet<MobilityIndividualApplicationProcess>> result =
                new TreeMap<Degree, SortedSet<MobilityIndividualApplicationProcess>>(Degree.COMPARATOR_BY_NAME_AND_ID);
        for (final IndividualCandidacyProcess child : getChildProcessesSet()) {
            final MobilityIndividualApplicationProcess process = (MobilityIndividualApplicationProcess) child;
            if (process.isCandidacyValid()) {
                SortedSet<MobilityIndividualApplicationProcess> values = result.get(process.getCandidacySelectedDegree());
                if (values == null) {
                    result.put(process.getCandidacySelectedDegree(), values =
                            new TreeSet<MobilityIndividualApplicationProcess>(
                                    SecondCycleIndividualCandidacyProcess.COMPARATOR_BY_CANDIDACY_PERSON));
                }
                values.add(process);
            }
        }
        return result;
    }

    private void checkParameters(final ExecutionInterval executionInterval, final DateTime start, final DateTime end) {
        if (executionInterval == null) {
            throw new DomainException("error.SecondCycleCandidacyProcess.invalid.executionInterval");
        }

        if (start == null || end == null || start.isAfter(end)) {
            throw new DomainException("error.SecondCycleCandidacyProcess.invalid.interval");
        }
    }

    @Override
    public boolean canExecuteActivity(User userView) {
        return isAllowedToManageProcess(userView)
                || RoleType.INTERNATIONAL_RELATION_OFFICE.isMember(userView.getPerson().getUser())
                || RoleType.COORDINATOR.isMember(userView.getPerson().getUser());
    }

    private static final java.util.function.Predicate<DegreeType> ALLOWED_DEGREE_TYPES = DegreeType.oneOf(
            DegreeType::isBolonhaMasterDegree, DegreeType::isIntegratedMasterDegree);

    static private boolean isAllowedToManageProcess(User userView) {
        return AcademicAccessRule
                .getProgramsAccessibleToFunction(AcademicOperationType.MANAGE_CANDIDACY_PROCESSES, userView.getPerson().getUser())
                .anyMatch(program -> ALLOWED_DEGREE_TYPES.test(program.getDegreeType()));
    }

    static private boolean isInternationalRelationsOfficer(User userView) {
        return RoleType.INTERNATIONAL_RELATION_OFFICE.isMember(userView.getPerson().getUser());
    }

    static private boolean isManager(User userView) {
        return Group.managers().isMember(userView);
    }

    @Override
    public List<Activity> getActivities() {
        return activities;
    }

    @Override
    public ExecutionYear getCandidacyExecutionInterval() {
        return (ExecutionYear) super.getCandidacyExecutionInterval();
    }

    private void edit(final DateTime start, final DateTime end) {
        checkParameters(getCandidacyPeriod().getExecutionInterval(), start, end);
        getCandidacyPeriod().edit(start, end);
    }

    @Override
    public String getDisplayName() {
        return BundleUtil.getString(Bundle.CASE_HANDLEING, getClass().getSimpleName());
    }

    public MobilityIndividualApplicationProcess getProcessByEIdentifier(String eIdentifier) {
        List<MobilityIndividualApplicationProcess> childProcesses =
                new java.util.ArrayList<MobilityIndividualApplicationProcess>((List) this.getChildProcessesSet());

        return childProcesses.stream()
                .filter(process -> eIdentifier.equals(process.getPersonalDetails().getPerson().getEidentifier()))
                .findFirst().orElse(null);

    }

    public MobilityIndividualApplicationProcess getOpenProcessByEIdentifier(String eIdentifier) {
        List<MobilityIndividualApplicationProcess> childProcesses =
                new java.util.ArrayList<MobilityIndividualApplicationProcess>((List) this.getChildProcessesSet());

        return childProcesses.stream()
                .filter(process -> !process.isCandidacyCancelled())
                .filter(process -> !StringUtils.isEmpty(process.getPersonalDetails().getEidentifier()))
                .filter(process -> eIdentifier.equals(process.getPersonalDetails().getEidentifier()))
                .findFirst().orElse(null);

    }

    @Override
    public MobilityApplicationPeriod getCandidacyPeriod() {
        return (MobilityApplicationPeriod) super.getCandidacyPeriod();
    }

    public MobilityApplicationPeriod getApplicationPeriod() {
        return getCandidacyPeriod();
    }

    public List<MobilityCoordinator> getErasmusCoordinatorForTeacher(final Teacher teacher) {
        return getCoordinatorsSet()
                .stream()
                .filter(coordinator -> coordinator.getTeacher() == teacher)
                .collect(Collectors.toList());
    }

    public MobilityCoordinator getCoordinatorForTeacherAndDegree(final Teacher teacher, final Degree degree) {
        return getErasmusCoordinatorForTeacher(teacher)
                .stream()
                .filter(coordinator -> coordinator.getDegree() == degree)
                .findFirst().orElse(null);
    }

    public List<Degree> getDegreesAssociatedToTeacherAsCoordinator(final Teacher teacher) {
        return getErasmusCoordinatorForTeacher(teacher)
                .stream()
                .map(MobilityCoordinator::getDegree)
                .collect(Collectors.toList());
    }

    public boolean isTeacherErasmusCoordinatorForDegree(final Teacher teacher, final Degree degree) {
        return getCoordinatorForTeacherAndDegree(teacher, degree) != null;
    }

    public List<MobilityIndividualApplicationProcess> getProcessesWithNotViewedApprovedLearningAgreements() {
        return getChildProcessesSet()
                .stream()
                .map(individualProcess -> (MobilityIndividualApplicationProcess) individualProcess)
                .filter(individualProcess -> !individualProcess.isCandidacyCancelled())
                .filter(individualProcess -> individualProcess.getCandidacy().isMostRecentApprovedLearningAgreementNotViewed())
                .collect(Collectors.toList());
    }

    public List<MobilityIndividualApplicationProcess> getProcessesWithNotViewedAlerts() {
        return getChildProcessesSet()
                .stream()
                .map(individualProcess -> (MobilityIndividualApplicationProcess) individualProcess)
                .filter(MobilityIndividualApplicationProcess::isProcessWithMostRecentAlertMessageNotViewed)
                .collect(Collectors.toList());
    }

    public List<ErasmusCandidacyProcessReport> getDoneReports() {
        return getErasmusCandidacyProcessReportsSet()
                .stream()
                .filter(QueueJob::getDone)
                .collect(Collectors.toList());
    }

    public List<ErasmusCandidacyProcessReport> getUndoneReports() {
        return new ArrayList(CollectionUtils.subtract(getErasmusCandidacyProcessReportsSet(), getDoneReports()));
    }

    public List<ErasmusCandidacyProcessReport> getPendingReports() {
        return getErasmusCandidacyProcessReportsSet()
                .stream()
                .filter(QueueJob::getIsNotDoneAndNotCancelled)
                .collect(Collectors.toList());
    }

    public boolean isAbleToLaunchReportGenerationJob() {
        return getPendingReports().isEmpty();
    }

    @StartActivity
    static public class CreateCandidacyPeriod extends Activity<MobilityApplicationProcess> {

        @Override
        public void checkPreConditions(MobilityApplicationProcess process, User userView) {
            if (!isAllowedToManageProcess(userView)) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected MobilityApplicationProcess executeActivity(MobilityApplicationProcess process, User userView, Object object) {
            final MobilityApplicationProcessBean bean = (MobilityApplicationProcessBean) object;
            return new MobilityApplicationProcess((ExecutionYear) bean.getExecutionInterval(), bean.getStart(), bean.getEnd(),
                    bean.getForSemester());
        }
    }

    static private class EditCandidacyPeriod extends Activity<MobilityApplicationProcess> {

        @Override
        public void checkPreConditions(MobilityApplicationProcess process, User userView) {
            if (!isAllowedToManageProcess(userView)) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected MobilityApplicationProcess executeActivity(MobilityApplicationProcess process, User userView, Object object) {
            final CandidacyProcessBean bean = (CandidacyProcessBean) object;
            process.edit(bean.getStart(), bean.getEnd());
            return process;
        }

        @Override
        public Boolean isVisibleForAdminOffice() {
            return true;
        }

        @Override
        public Boolean isVisibleForCoordinator() {
            return false;
        }

        @Override
        public Boolean isVisibleForGriOffice() {
            return false;
        }

    }

    private static class ViewMobilityQuota extends Activity<MobilityApplicationProcess> {
        @Override
        public void checkPreConditions(MobilityApplicationProcess process, User userView) {
            if (!isInternationalRelationsOfficer(userView) && !isManager(userView)) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected MobilityApplicationProcess executeActivity(MobilityApplicationProcess process, User userView, Object object) {
            return process;
        }
    }

    static private class InsertMobilityQuota extends Activity<MobilityApplicationProcess> {

        @Override
        public void checkPreConditions(MobilityApplicationProcess process, User userView) {
            if (!isInternationalRelationsOfficer(userView) && !isManager(userView)) {
                throw new PreConditionNotValidException();
            }

        }

        @Override
        protected MobilityApplicationProcess executeActivity(MobilityApplicationProcess process, User userView, Object object) {
            ErasmusVacancyBean bean = (ErasmusVacancyBean) object;

            for (Degree degree : bean.getDegrees()) {
                MobilityQuota.createVacancy(process.getCandidacyPeriod(), degree, bean.getMobilityProgram(),
                        bean.getUniversity(), bean.getNumberOfVacancies());
            }

            return process;
        }

        @Override
        public Boolean isVisibleForAdminOffice() {
            return false;
        }

        @Override
        public Boolean isVisibleForCoordinator() {
            return false;
        }

        @Override
        public Boolean isVisibleForGriOffice() {
            return false;
        }

    }

    static private class RemoveMobilityQuota extends Activity<MobilityApplicationProcess> {

        @Override
        public void checkPreConditions(MobilityApplicationProcess process, User userView) {
            if (!isInternationalRelationsOfficer(userView) && !isManager(userView)) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected MobilityApplicationProcess executeActivity(MobilityApplicationProcess process, User userView, Object object) {
            ErasmusVacancyBean bean = (ErasmusVacancyBean) object;
            MobilityQuota quota = bean.getQuota();

            if (quota.isQuotaAssociatedWithAnyApplication()) {
                throw new DomainException("error.mobility.quota.is.associated.with.applications");
            }

            quota.delete();

            return process;
        }

        @Override
        public Boolean isVisibleForAdminOffice() {
            return false;
        }

        @Override
        public Boolean isVisibleForCoordinator() {
            return false;
        }

        @Override
        public Boolean isVisibleForGriOffice() {
            return false;
        }

    }

    static private class ViewErasmusCoordinators extends Activity<MobilityApplicationProcess> {
        @Override
        public void checkPreConditions(MobilityApplicationProcess process, User userView) {
            if (!isInternationalRelationsOfficer(userView) && !isManager(userView)) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected MobilityApplicationProcess executeActivity(MobilityApplicationProcess process, User userView, Object object) {
            return process;
        }
    }

    static private class AssignCoordinator extends Activity<MobilityApplicationProcess> {

        @Override
        public void checkPreConditions(MobilityApplicationProcess process, User userView) {
            if (!isInternationalRelationsOfficer(userView) && !isManager(userView)) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected MobilityApplicationProcess executeActivity(MobilityApplicationProcess process, User userView, Object object) {
            ErasmusCoordinatorBean bean = (ErasmusCoordinatorBean) object;
            new MobilityCoordinator(process, bean);

            return process;
        }

        @Override
        public Boolean isVisibleForAdminOffice() {
            return false;
        }

        @Override
        public Boolean isVisibleForCoordinator() {
            return false;
        }

        @Override
        public Boolean isVisibleForGriOffice() {
            return false;
        }

    }

    static private class RemoveTeacherFromCoordinators extends Activity<MobilityApplicationProcess> {

        @Override
        public void checkPreConditions(MobilityApplicationProcess process, User userView) {
            if (!isInternationalRelationsOfficer(userView) && !isManager(userView)) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected MobilityApplicationProcess executeActivity(MobilityApplicationProcess process, User userView, Object object) {
            ErasmusCoordinatorBean bean = (ErasmusCoordinatorBean) object;

            if (bean.getErasmusCoordinator() != null) {
                bean.getErasmusCoordinator().delete();
            }
            bean.setErasmusCoordinator(null);

            return process;
        }

        @Override
        public Boolean isVisibleForAdminOffice() {
            return false;
        }

        @Override
        public Boolean isVisibleForCoordinator() {
            return false;
        }

        @Override
        public Boolean isVisibleForGriOffice() {
            return false;
        }
    }

    static private class ViewChildProcessWithMissingRequiredDocumentFiles extends Activity<MobilityApplicationProcess> {
        @Override
        public void checkPreConditions(MobilityApplicationProcess process, User userView) {
            if (!isInternationalRelationsOfficer(userView) && !isManager(userView)) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected MobilityApplicationProcess executeActivity(MobilityApplicationProcess process, User userView, Object object) {
            return process;
        }

        @Override
        public Boolean isVisibleForAdminOffice() {
            return false;
        }

        @Override
        public Boolean isVisibleForCoordinator() {
            return false;
        }

        @Override
        public Boolean isVisibleForGriOffice() {
            return true;
        }

    }

    static private class SendEmailToMissingRequiredDocumentsProcesses extends Activity<MobilityApplicationProcess> {

        @Override
        public void checkPreConditions(MobilityApplicationProcess process, User userView) {
            if (!isManager(userView)) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected MobilityApplicationProcess executeActivity(MobilityApplicationProcess process, User userView, Object object) {
            MobilityApplicationPeriod candidacyPeriod = process.getCandidacyPeriod();

            MobilityEmailTemplate emailTemplateFor =
                    candidacyPeriod.getEmailTemplateFor(MobilityEmailTemplateType.MISSING_DOCUMENTS);

            emailTemplateFor.sendMultiEmailFor(process.getProcessesMissingDocuments());

            return process;
        }

        @Override
        public Boolean isVisibleForAdminOffice() {
            return false;
        }

        @Override
        public Boolean isVisibleForCoordinator() {
            return false;
        }

        @Override
        public Boolean isVisibleForGriOffice() {
            return false;
        }

    }

    static private class SendEmailToMissingShiftsProcesses extends Activity<MobilityApplicationProcess> {

        @Override
        public void checkPreConditions(MobilityApplicationProcess process, User userView) {
            if (!isManager(userView)) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected MobilityApplicationProcess executeActivity(MobilityApplicationProcess process, User userView, Object object) {
            MobilityApplicationPeriod candidacyPeriod = process.getCandidacyPeriod();

            MobilityEmailTemplate emailTemplateFor =
                    candidacyPeriod.getEmailTemplateFor(MobilityEmailTemplateType.MISSING_SHIFTS);
            if (emailTemplateFor == null) {
                for (MobilityProgram mobilityProgram : candidacyPeriod.getMobilityPrograms()) {
                    for (MobilityEmailTemplate mobilityEmailTemplate : mobilityProgram.getEmailTemplatesSet()) {
                        if (mobilityEmailTemplate.getType().equals(MobilityEmailTemplateType.MISSING_SHIFTS)) {
                            throw new DomainException("error.missing.shifts.template.not.found", mobilityProgram.getName()
                                    .getContent());
                        }
                    }
                }
            }

            emailTemplateFor.sendMultiEmailFor(process.getProcessesMissingShifts());
            return process;
        }

        @Override
        public Boolean isVisibleForAdminOffice() {
            return false;
        }

        @Override
        public Boolean isVisibleForCoordinator() {
            return false;
        }

        @Override
        public Boolean isVisibleForGriOffice() {
            return false;
        }

    }

    static private class SendReceptionEmail extends Activity<MobilityApplicationProcess> {
        @Override
        public void checkPreConditions(MobilityApplicationProcess process, User userView) {
            if (isManager(userView)) {
                return;
            }

            if (isInternationalRelationsOfficer(userView)) {
                return;
            }

            throw new PreConditionNotValidException();
        }

        @Override
        protected MobilityApplicationProcess executeActivity(MobilityApplicationProcess process, User userView, Object object) {
            SendReceptionEmailBean sendBean = (SendReceptionEmailBean) object;
            ReceptionEmailExecutedAction.createAction(process, sendBean);

            return process;
        }

        @Override
        public Boolean isVisibleForAdminOffice() {
            return false;
        }

        @Override
        public Boolean isVisibleForCoordinator() {
            return false;
        }

        @Override
        public Boolean isVisibleForGriOffice() {
            return true;
        }
    }

    static private class EditReceptionEmailMessage extends Activity<MobilityApplicationProcess> {

        @Override
        public void checkPreConditions(MobilityApplicationProcess process, User userView) {
            if (isManager(userView)) {
                return;
            }

            if (isInternationalRelationsOfficer(userView)) {
                return;
            }

            throw new PreConditionNotValidException();
        }

        @Override
        protected MobilityApplicationProcess executeActivity(MobilityApplicationProcess process, User userView, Object object) {
            process.editReceptionEmailMessage((SendReceptionEmailBean) object);

            return process;
        }

        @Override
        public Boolean isVisibleForAdminOffice() {
            return false;
        }

        @Override
        public Boolean isVisibleForCoordinator() {
            return false;
        }

        @Override
        public Boolean isVisibleForGriOffice() {
            return false;
        }

    }

    public void editReceptionEmailMessage(SendReceptionEmailBean sendReceptionEmailBean) {
        if (StringUtils.isEmpty(sendReceptionEmailBean.getEmailSubject())
                || StringUtils.isEmpty(sendReceptionEmailBean.getEmailBody())) {
            throw new DomainException("error.reception.email.subject.and.body.must.not.be.empty");
        }

        setReceptionEmailSubject(sendReceptionEmailBean.getEmailSubject());
        setReceptionEmailBody(sendReceptionEmailBean.getEmailBody());
    }

    public boolean isReceptionEmailMessageDefined() {
        // DANGEROUS: getEmailTemplateFor picks first element in the collection
        // and returns (it's a 1-to-n relation)
        MobilityEmailTemplate template = getCandidacyPeriod().getEmailTemplateFor(MobilityEmailTemplateType.IST_RECEPTION);
        return !(StringUtils.isEmpty(template.getSubject()) || StringUtils.isEmpty(template.getBody()));
    }

    @Override
    public boolean isMobility() {
        return true;
    }

    public List<MobilityIndividualApplicationProcess> getProcessesMissingDocuments() {
        List<MobilityIndividualApplicationProcess> results = new ArrayList<MobilityIndividualApplicationProcess>();
        for (IndividualCandidacyProcess icp : getChildsWithMissingRequiredDocuments()) {
            if (icp instanceof MobilityIndividualApplicationProcess) {
                MobilityIndividualApplicationProcess miap = ((MobilityIndividualApplicationProcess) icp);
                results.add(miap);
            }
        }
        return results;
    }

    public List<MobilityIndividualApplicationProcess> getProcessesMissingShifts() {
        List<MobilityIndividualApplicationProcess> results = new ArrayList<MobilityIndividualApplicationProcess>();
        for (IndividualCandidacyProcess icp : getChildsWithMissingShifts()) {
            if (icp instanceof MobilityIndividualApplicationProcess) {
                MobilityIndividualApplicationProcess miap = ((MobilityIndividualApplicationProcess) icp);
                results.add(miap);
            }
        }
        return results;
    }

}
