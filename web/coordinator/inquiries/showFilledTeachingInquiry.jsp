<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>
<html:xhtml />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="pt-PT" xml:lang="pt-PT">

<head>
	<title>.IST</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
	<link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/CSS/layout.css"  media="screen"  />
	<link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/CSS/general.css" media="screen" />
	<link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/CSS/color.css" media="screen" />
	<link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/CSS/print.css" media="print" />
	
</head>

<body class="survey">


<style>
body.survey {
background: #fff;
margin: 2em;
font-size: 70%;
}
.acenter { text-align: center !important; }
.aright { text-align: right !important; }
.aleft { text-align: left !important; }
th:first-child {
width: 250px;
}
body.survey table {
}
body.survey table th {
vertical-align: bottom;
}
body.survey table td {
text-align: center;
}
table.td50px td {
width: 60px;
}
table tr.top th { border-top: 4px solid #ddd; }
table tr.top td { border-top: 4px solid #ddd; }

body.survey table {
}
.thtop th { vertical-align: top; }
.vatop { vertical-align: top !important; }
.vamiddle { vertical-align: middle !important; }
.tdright td { text-align: right !important; }
.tdleft td { text-align: left !important; }


a.help {
position: relative;
text-decoration: none;
border: none !important;
width: 20px;
}
a.help span {
display: none;
}
a.help:hover {
z-index: 100;
}
a.help:hover span {
display: block !important;
display: inline-block;
width: 250px;
position: absolute;
top: 10px;
left: 30px;
text-align: left;
padding: 7px 10px;
background: #48869e;
color: #fff;
border: 3px solid #97bac6;
}
a { color: #105c93; }

table th.separatorright {
border-right: 3px solid #ddd;
padding-right: 8px;
}
table td.separatorright {
border-right: 3px solid #ddd;
padding-right: 8px;
}
</style>



<h2><bean:message key="title.teachingInquiries" bundle="INQUIRIES_RESOURCES"/></h2>

<div class="infoop2" style="font-size: 1.3em; padding: 0.5em 1em; margin: 1em 0;">
	<p style="margin: 0.75em 0;">Semestre: 
		<bean:write name="teachingInquiry" property="professorship.executionCourse.executionPeriod.semester"/>
		<bean:message bundle="PUBLIC_DEGREE_INFORMATION" locale="pt_PT" key="public.degree.information.label.ordinal.semester.abbr" />
		<bean:message bundle="APPLICATION_RESOURCES" locale="pt_PT" key="of" /> 
		<bean:write name="teachingInquiry" property="professorship.executionCourse.executionYear.name"/></span></p>	
	<p style="margin: 0.75em 0;">Unidade curricular: <bean:write name="teachingInquiry" property="professorship.executionCourse.nome"/></p>
	<p style="margin: 0.75em 0;">Docente: <bean:write name="teachingInquiry" property="professorship.teacher.category.name"/> - <bean:write name="teachingInquiry" property="professorship.teacher.person.name"/></p>
</div>

<p class="mtop2"><strong><bean:message key="title.teachingInquiries.teachingAndLearningConditions" bundle="INQUIRIES_RESOURCES"/></strong></p>


<table class="tstyle1 thlight thleft tdleft">
	<tr>
		<th><bean:message key="label.teachingInquiries.loadAndClassTypeContributionToFullfilmentOfCUProgram" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.loadAndClassTypeContributionToFullfilmentOfCUProgram}" />/9</td>

	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.teacherNumberSuitableForCUOperation" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.teacherNumberSuitableForCUOperation}" />/9</td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.establishedScheduleSuitable" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.establishedScheduleSuitable}" />/9</td>
	</tr>

	<tr>
		<th><bean:message key="label.teachingInquiries.establishedScheduleNotSuitableReason" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.establishedScheduleNotSuitableReason}" /></td>
	</tr>
</table>

<p class="mtop2"><strong><bean:message key="title.teachingInquiries.studentsEvaluation" bundle="INQUIRIES_RESOURCES"/></strong></p>


<table class="tstyle1 thlight thleft tdleft">
	<tr>

		<th><bean:message key="label.teachingInquiries.studentsReadyForFollowingCU" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.studentsReadyForFollowingCU}" />/9</td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.evaluationMethodSuitableForCUTeachingTypeAndObjective" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.evaluationMethodSuitableForCUTeachingTypeAndObjective}" />/9</td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.disturbingEventsInClasses" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.disturbingEventsInClasses}" />/9</td>

	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.disturbingEventsInClassesDescription" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.disturbingEventsInClassesDescription}" /></td>
	</tr>
</table>

<p class="mtop2"><strong><bean:message key="title.teachingInquiries.semesterAverageStudentNumber" bundle="INQUIRIES_RESOURCES"/></strong></p>

<table class="tstyle1 thlight thleft td50px tdcenter">

	<tr>
		<th colspan="2"><bean:message key="title.teachingInquiries.semesterAverageStudentNumberInTheorical" bundle="INQUIRIES_RESOURCES"/></th>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.semesterStartAverageStudentNumberInTheorical" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.semesterStartAverageStudentNumberInTheorical}" /></td>
	</tr>
	<tr>

		<th><bean:message key="label.teachingInquiries.semesterMiddleAverageStudentNumberInTheorical" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.semesterMiddleAverageStudentNumberInTheorical}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.semesterEndAverageStudentNumberInTheorical" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.semesterEndAverageStudentNumberInTheorical}" /></td>
	</tr>
	<tr>
		<th colspan="2"><bean:message key="title.teachingInquiries.semesterAverageStudentNumberInProblems" bundle="INQUIRIES_RESOURCES"/></th>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.semesterStartAverageStudentNumberInProblems" bundle="INQUIRIES_RESOURCES"/> </th>
		<td><c:out value="${teachingInquiry.semesterStartAverageStudentNumberInProblems}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.semesterMiddleAverageStudentNumberInProblems" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.semesterMiddleAverageStudentNumberInProblems}" /></td>
	</tr>

	<tr>
		<th><bean:message key="label.teachingInquiries.semesterEndAverageStudentNumberInProblems" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.semesterEndAverageStudentNumberInProblems}" /></td>
	</tr>
	<tr>
		<th colspan="2"><bean:message key="title.teachingInquiries.semesterAverageStudentNumberInLabs" bundle="INQUIRIES_RESOURCES"/></th>
	</tr>
	<tr>

		<th><bean:message key="label.teachingInquiries.semesterStartAverageStudentNumberInLabs" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.semesterStartAverageStudentNumberInLabs}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.semesterMiddleAverageStudentNumberInLabs" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.semesterMiddleAverageStudentNumberInLabs}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.semesterEndAverageStudentNumberInLabs" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.semesterEndAverageStudentNumberInLabs}" /></td>

	</tr>
	<tr>
		<th colspan="2"><bean:message key="title.teachingInquiries.semesterAverageStudentNumberInSeminary" bundle="INQUIRIES_RESOURCES"/></th>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.semesterStartAverageStudentNumberInSeminary" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.semesterStartAverageStudentNumberInSeminary}" /></td>
	</tr>

	<tr>
		<th><bean:message key="label.teachingInquiries.semesterMiddleAverageStudentNumberInSeminary" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.semesterMiddleAverageStudentNumberInSeminary}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.semesterEndAverageStudentNumberInSeminary" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.semesterEndAverageStudentNumberInSeminary}" /></td>
	</tr>
	<tr>

		<th colspan="2"><bean:message key="title.teachingInquiries.semesterAverageStudentNumberInProject" bundle="INQUIRIES_RESOURCES"/></th>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.semesterStartAverageStudentNumberInProject" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.semesterStartAverageStudentNumberInProject}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.semesterMiddleAverageStudentNumberInProject" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.semesterMiddleAverageStudentNumberInProject}" /></td>

	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.semesterEndAverageStudentNumberInProject" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.semesterEndAverageStudentNumberInProject}" /></td>
	</tr>
</table>


<table class="tstyle1 thlight thleft td50px tdcenter">

	<tr>
		<th><bean:message key="label.teachingInquiries.activeAndInteressedStudentsRatio" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.activeAndInteressedStudentsRatio}" />%</td>
	</tr>
</table>

	

<table class="tstyle1 thlight thleft td50px tdcenter">
	<tr>
		<th><bean:message key="label.teachingInquiries.studentsPerformance" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.studentsPerformance}" />/4</td>
	</tr>
</table>	
 
<p class="mtop2"><strong><bean:message key="title.teachingInquiries.autoEvaluation" bundle="INQUIRIES_RESOURCES"/></strong></p>



<table class="tstyle1 thlight thleft tdleft">
	<tr>
		<th><bean:message key="label.teachingInquiries.classesAndOtherActivitiesFrequency" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.classesAndOtherActivitiesFrequency}" />/4</td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.classesAndOtherActivitiesPonctuality" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.classesAndOtherActivitiesPonctuality}" />/4</td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.globalQualityOfTeachingInCU" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.globalQualityOfTeachingInCU}" />/4</td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.pedagogicalActivitiesDeveloped" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.pedagogicalActivitiesDeveloped}" />/4</td>
	</tr>	
	<tr>
		<th><bean:message key="label.teachingInquiries.relativePedagogicalInitiatives" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.relativePedagogicalInitiatives}" /></td>
	</tr>
</table>

<c:if test="${teachingInquiry.professorship.responsibleFor}">

<p class="mtop2"><strong><bean:message key="title.teachingInquiries.cuEvaluationMethod" bundle="INQUIRIES_RESOURCES"/></strong></p>
<table class="tstyle1 thlight thleft td50px tdcenter">

	<tr>
		<th colspan="2"><bean:message key="title.teachingInquiries.cuEvaluationMethod.writtenProofs" bundle="INQUIRIES_RESOURCES"/></th>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.numberOfExams" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.numberOfExams}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.numberOfTests" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.numberOfTests}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.numberOfQuizzesAndMiniTests" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.numberOfQuizzesAndMiniTests}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.numberOfElectronicQuizzes" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.numberOfElectronicQuizzes}" /></td>
	</tr>
	<tr>
		<th colspan="2"><bean:message key="title.teachingInquiries.cuEvaluationMethod.worksOrProjects" bundle="INQUIRIES_RESOURCES"/></th>
	</tr>	
	<tr>
		<th><bean:message key="label.teachingInquiries.numberOfStudyVisitsOrOtherActivitiesReports" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.numberOfStudyVisitsOrOtherActivitiesReports}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.numberOfWorksOrProjects" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.numberOfWorksOrProjects}" /></td>
	</tr>			
	<tr>
		<th><bean:message key="label.teachingInquiries.teachingLanguage" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.teachingLanguage}" /></td>
	</tr>	
	<tr>
		<th><bean:message key="label.teachingInquiries.otherTeachingLanguage" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.otherTeachingLanguage}" /></td>
	</tr>		

</table>	


<p class="mtop2"><strong><bean:message key="title.teachingInquiries.cuGlobalEvaluation" bundle="INQUIRIES_RESOURCES"/></strong></p>
<table class="tstyle1 thlight thleft tdcenter">

	<tr>
		<th><bean:message key="label.teachingInquiries.workLoadClassification" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.workLoadClassification}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.workLoadClassificationReasons" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.workLoadClassificationReasons}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.positionOfCUInStudentCurricularPlan" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.positionOfCUInStudentCurricularPlan}" /></td>
	</tr>
	
</table>	


<p class="mtop2"><strong><bean:message key="title.teachingInquiries.cuStudentsCompetenceAcquisitionAndDevelopmentLevel" bundle="INQUIRIES_RESOURCES"/></strong></p>
<table class="tstyle1 thlight thleft td50px tdcenter">

	<tr>
		<th><bean:message key="label.teachingInquiries.comprehensionAndKnowledgeOfCU" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.comprehensionAndKnowledgeOfCU}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.comprehensionApplicationOfCU" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.comprehensionApplicationOfCU}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.criticalSenseAndReflexiveSpirit" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.criticalSenseAndReflexiveSpirit}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.cooperationAndCommunicationCapacity" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.cooperationAndCommunicationCapacity}" /></td>
	</tr>
		
</table>

<p class="mtop2"><strong><bean:message key="title.teachingInquiries.globalClassificationOfThisCU" bundle="INQUIRIES_RESOURCES"/></strong></p>
<table class="tstyle1 thlight thleft td50px tdcenter">

	<tr>
		<th></th>
		<td><c:out value="${teachingInquiry.globalClassificationOfThisCU}" /></td>
	</tr>
		
</table>


<p class="mtop2"><strong><bean:message key="title.teachingInquiries.cuTeachingProcess" bundle="INQUIRIES_RESOURCES"/></strong></p>
<table class="tstyle1 thlight thleft tdleft">

	<tr>
		<th><bean:message key="label.teachingInquiries.strongPointsOfCUTeachingProcess" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.strongPointsOfCUTeachingProcess}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.weakPointsOfCUTeachingProcess" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.weakPointsOfCUTeachingProcess}" /></td>
	</tr>
	<tr>
		<th><bean:message key="label.teachingInquiries.finalCommentsAndImproovements" bundle="INQUIRIES_RESOURCES"/></th>
		<td><c:out value="${teachingInquiry.finalCommentsAndImproovements}" /></td>
	</tr>
		
</table>

<p class="mtop2"><strong><bean:message key="title.teachingInquiries.resultsToImprove" bundle="INQUIRIES_RESOURCES"/></strong></p>
<table class="tstyle1 thlight thleft tdleft">
	<tr>
		<th><bean:message key="label.teachingInquiries.negativeResultsResolutionAndImproovementPlanOfAction" bundle="INQUIRIES_RESOURCES"/></th>
		<td style="vertical-align: top;"><c:out value="${teachingInquiry.negativeResultsResolutionAndImproovementPlanOfAction}" /></td>
	</tr>
</table>

</c:if>

</body>

</html>