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
package org.fenixedu.academic.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 
 * @author - Shezad Anavarali (shezad@ist.utl.pt)
 * 
 */
public class CurricularCourseScopesForPrintDTO extends DataTranferObject {

    private List<DegreeCurricularPlanForPrintDTO> degreeCurricularPlans;

    public CurricularCourseScopesForPrintDTO() {
        super();
        degreeCurricularPlans = new ArrayList<DegreeCurricularPlanForPrintDTO>();
    }

    public void add(final InfoCurricularCourseScope scope) {

        DegreeCurricularPlanForPrintDTO selectedCurricularPlan = getSelectedCurricularPlan(scope);

        CurricularYearForPrintDTO selectedCurricularYear = getSelectedCurricularYear(scope, selectedCurricularPlan);

        BranchForPrintDTO selectedBranch = getSelectedBranch(scope, selectedCurricularYear);

        CurricularSemesterForPrintDTO selectedSemester = getSelectedSemester(scope, selectedBranch);

        addScope(scope, selectedSemester);

    }

    private void addScope(final InfoCurricularCourseScope scope, CurricularSemesterForPrintDTO selectedSemester) {
        selectedSemester.getScopes().add(scope);
        sortScopes(selectedSemester.getScopes());
    }

    private static final Comparator<InfoCurricularCourseScope> COMPARATOR_BY_ANOTATION_AND_CURRICULAR_COURSE_NAME =
            new Comparator<InfoCurricularCourseScope>() {
                @Override
                public int compare(InfoCurricularCourseScope o1, InfoCurricularCourseScope o2) {
                    if (o1.getAnotation() == null) {
                        return -1; // o1 is less than o2
                    }
                    if (o2.getAnotation() == null) {
                        return 1; // o1 is greater than o2
                    }
                    int result = o1.getAnotation().compareTo(o2.getAnotation());
                    return (result == 0) ? o1.getInfoCurricularCourse().getName()
                            .compareTo(o2.getInfoCurricularCourse().getName()) : result;
                }
            };

    private void sortScopes(List<InfoCurricularCourseScope> scopes) {
        Collections.sort(scopes, COMPARATOR_BY_ANOTATION_AND_CURRICULAR_COURSE_NAME);
    }

    private CurricularSemesterForPrintDTO getSelectedSemester(final InfoCurricularCourseScope scope,
            BranchForPrintDTO selectedBranch) {
        selectedBranch.getSemesters().stream()
                .filter(semesterForPrintDTO -> semesterForPrintDTO.getSemester().equals(scope.getInfoCurricularSemester().getSemester()))
                .findFirst().orElse(null);
        CurricularSemesterForPrintDTO selectedSemester = selectedBranch.getSemesters().stream()
                .filter(semesterForPrintDTO -> semesterForPrintDTO.getSemester().equals(scope.getInfoCurricularSemester().getSemester()))
                .findFirst().orElse(null);
        if (selectedSemester == null) {
            selectedSemester = new CurricularSemesterForPrintDTO(scope.getInfoCurricularSemester().getSemester());
            selectedBranch.getSemesters().add(selectedSemester);
        }

        return selectedSemester;
    }

    private BranchForPrintDTO getSelectedBranch(final InfoCurricularCourseScope scope,
            CurricularYearForPrintDTO selectedCurricularYear) {
        BranchForPrintDTO selectedBranch = selectedCurricularYear.getBranches().stream()
                .filter(branchForPrintDTO -> branchForPrintDTO.getName().equals(scope.getInfoBranch().getName()))
                .findFirst().orElse(null);

        if (selectedBranch == null) {
            selectedBranch = new BranchForPrintDTO(scope.getInfoBranch().getName());
            selectedCurricularYear.getBranches().add(selectedBranch);

        }

        return selectedBranch;
    }

    private CurricularYearForPrintDTO getSelectedCurricularYear(final InfoCurricularCourseScope scope,
            DegreeCurricularPlanForPrintDTO selectedCurricularPlan) {
        CurricularYearForPrintDTO selectedCurricularYear = selectedCurricularPlan.getYears().stream()
                .filter(yearForPrintDTO -> yearForPrintDTO.getYear().equals(scope.getInfoCurricularSemester().getInfoCurricularYear().getYear()))
                .findFirst().orElse(null);

        if (selectedCurricularYear == null) {
            selectedCurricularYear =
                    new CurricularYearForPrintDTO(scope.getInfoCurricularSemester().getInfoCurricularYear().getYear());
            selectedCurricularPlan.getYears().add(selectedCurricularYear);

        }

        return selectedCurricularYear;
    }

    private DegreeCurricularPlanForPrintDTO getSelectedCurricularPlan(final InfoCurricularCourseScope scope) {
        DegreeCurricularPlanForPrintDTO selectedCurricularPlan = getDegreeCurricularPlans().stream()
                .filter(planForPrintDTO -> planForPrintDTO.name.equals(scope.getInfoCurricularCourse().getInfoDegreeCurricularPlan().getName()))
                .findFirst().orElse(null);

        if (selectedCurricularPlan == null) {
            InfoDegreeCurricularPlan degreeCurricularPlan = scope.getInfoCurricularCourse().getInfoDegreeCurricularPlan();
            selectedCurricularPlan =
                    new DegreeCurricularPlanForPrintDTO(degreeCurricularPlan.getName(), degreeCurricularPlan.getInfoDegree()
                            .getNome(), degreeCurricularPlan.getAnotation());
            this.getDegreeCurricularPlans().add(selectedCurricularPlan);
        }

        return selectedCurricularPlan;
    }

    public static class DegreeCurricularPlanForPrintDTO extends DataTranferObject {

        private String name;

        private String degreeName;

        private String anotation;

        private List<CurricularYearForPrintDTO> years;

        public DegreeCurricularPlanForPrintDTO(String name, String degreeName, String anotation) {
            super();

            this.years = new ArrayList<CurricularYearForPrintDTO>();
            this.name = name;
            this.degreeName = degreeName;
            this.anotation = anotation;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<CurricularYearForPrintDTO> getYears() {
            return years;
        }

        public void setYears(List<CurricularYearForPrintDTO> years) {
            this.years = years;
        }

        public String getAnotation() {
            return anotation;
        }

        public void setAnotation(String anotation) {
            this.anotation = anotation;
        }

        public String getDegreeName() {
            return degreeName;
        }

        public void setDegreeName(String degreeName) {
            this.degreeName = degreeName;
        }

    }

    public static class CurricularYearForPrintDTO extends DataTranferObject {

        private Integer year;

        private List<BranchForPrintDTO> branches;

        public CurricularYearForPrintDTO(Integer year) {
            super();

            this.branches = new ArrayList<BranchForPrintDTO>();
            this.year = year;
        }

        public List<BranchForPrintDTO> getBranches() {
            return branches;
        }

        public void setBranches(List<BranchForPrintDTO> branches) {
            this.branches = branches;
        }

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

    }

    public static class BranchForPrintDTO extends DataTranferObject {

        private String name;

        private List<CurricularSemesterForPrintDTO> semesters;

        public BranchForPrintDTO(String name) {
            super();

            this.semesters = new ArrayList<CurricularSemesterForPrintDTO>();
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<CurricularSemesterForPrintDTO> getSemesters() {
            return semesters;
        }

        public void setSemesters(List<CurricularSemesterForPrintDTO> semester) {
            this.semesters = semester;
        }

    }

    public static class CurricularSemesterForPrintDTO extends DataTranferObject {

        private Integer semester;

        private List<InfoCurricularCourseScope> scopes;

        public CurricularSemesterForPrintDTO(Integer semester) {
            super();

            this.scopes = new ArrayList<InfoCurricularCourseScope>();
            this.semester = semester;
        }

        public void add(InfoCurricularCourseScope scope) {
            getScopes().add(scope);

        }

        public List<InfoCurricularCourseScope> getScopes() {
            return scopes;
        }

        public void setScopes(List<InfoCurricularCourseScope> scopes) {
            this.scopes = scopes;
        }

        public Integer getSemester() {
            return semester;
        }

        public void setSemester(Integer semester) {
            this.semester = semester;
        }

    }

    public List<DegreeCurricularPlanForPrintDTO> getDegreeCurricularPlans() {
        return degreeCurricularPlans;
    }

    public void setDegreeCurricularPlans(List<DegreeCurricularPlanForPrintDTO> degreeCurricularPlans) {
        this.degreeCurricularPlans = degreeCurricularPlans;
    }

}
