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
/*
 * Created on Jun 26, 2006
 */
package org.fenixedu.academic.ui.struts.action.administrativeOffice.payments;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.accounting.Event;
import org.fenixedu.academic.domain.accounting.PaymentMode;
import org.fenixedu.academic.domain.accounting.Receipt;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.academic.domain.exceptions.DomainExceptionWithLabelFormatter;
import org.fenixedu.academic.dto.accounting.EntryDTO;
import org.fenixedu.academic.dto.accounting.PaymentsManagementDTO;
import org.fenixedu.academic.service.services.accounting.CreatePaymentsForEvents;
import org.fenixedu.academic.ui.struts.FenixActionForm;
import org.fenixedu.academic.ui.struts.action.administrativeOffice.student.SearchForStudentsDA;
import org.fenixedu.academic.ui.struts.action.base.FenixDispatchAction;
import org.fenixedu.bennu.struts.annotations.Forward;
import org.fenixedu.bennu.struts.annotations.Forwards;
import org.fenixedu.bennu.struts.annotations.Mapping;
import org.joda.time.DateTime;
import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapping(path = "/payments", module = "academicAdministration", formBeanClass = FenixActionForm.class,
        functionality = SearchForStudentsDA.class)
@Forwards({ @Forward(name = "showOperations", path = "/academicAdminOffice/payments/showOperations.jsp"),
        @Forward(name = "showEvents", path = "/academicAdminOffice/payments/showEvents.jsp"),
        @Forward(name = "preparePayment", path = "/academicAdminOffice/payments/preparePayment.jsp"),
})
public class PaymentsManagementDispatchAction extends FenixDispatchAction {

    protected PaymentsManagementDTO searchNotPayedEventsForPerson(Person person) {

        final PaymentsManagementDTO paymentsManagementDTO = new PaymentsManagementDTO(person);
        for (final Event event : person.getNotPayedEvents()) {
            paymentsManagementDTO.addEntryDTOs(event.calculateEntries());
        }

        return paymentsManagementDTO;
    }

    public ActionForward showEvents(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) {

        try {
            request.setAttribute("paymentsManagementDTO", searchNotPayedEventsForPerson(getPerson(request)));

        } catch (DomainException e) {
            addActionMessage(request, e.getKey(), e.getArgs());
            return showOperations(mapping, form, request, response);
        }

        return mapping.findForward("showEvents");
    }

    public ActionForward preparePayment(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) {

        final PaymentsManagementDTO paymentsManagementDTO =
                (PaymentsManagementDTO) RenderUtils.getViewState("paymentsManagementDTO").getMetaObject().getObject();

        paymentsManagementDTO.setPaymentDate(new DateTime());

        request.setAttribute("paymentsManagementDTO", paymentsManagementDTO);

        final List<EntryDTO> selectedEntries = paymentsManagementDTO.getSelectedEntries();

        if (selectedEntries.isEmpty()) {
            addActionMessage("context", request, "error.payments.payment.entries.selection.is.required");
            return mapping.findForward("showEvents");
        }

        final List<EntryDTO> eventPenaltyEntries = getEventPenaltyEntries(searchNotPayedEventsForPerson(getPerson(request))
                .getEntryDTOs());
        final List<EntryDTO> selectedEventPenaltyEntries = getEventPenaltyEntries(selectedEntries);

        final Set<Event> missingPenaltyEntriesEvents = selectedEntries.stream().map(EntryDTO::getEvent).distinct()
                .flatMap(e -> eventPenaltyEntries.stream().filter(pe -> pe.getEvent() == e))
                .filter(pe -> !selectedEventPenaltyEntries.contains(pe)).map(EntryDTO::getEvent).collect(Collectors.toSet());

        if (!missingPenaltyEntriesEvents.isEmpty()) {
            missingPenaltyEntriesEvents.forEach(e -> {
                addActionMessage("context", request, "error.payments.payment.penalty.entries.selection.is.required",e
                        .getDescription().toString());
            });
            return mapping.findForward("showEvents");
        }

        return mapping.findForward("preparePayment");
    }

    private List<EntryDTO> getEventPenaltyEntries(List<EntryDTO> entries) {
        return entries.stream().filter(EntryDTO::isForPenalty).collect(Collectors.toList());
    }

    public ActionForward doPayment(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) {

        final PaymentsManagementDTO paymentsManagementDTO =
                (PaymentsManagementDTO) RenderUtils.getViewState("paymentsManagementDTO-edit").getMetaObject().getObject();

        if (paymentsManagementDTO.getSelectedEntries().isEmpty()) {

            addActionMessage("context", request, "error.payments.payment.entries.selection.is.required");
            request.setAttribute("paymentsManagementDTO", paymentsManagementDTO);

            return mapping.findForward("preparePayment");
        }

        //This is here to force the load of the relation to debug a possible bug in FenixFramework
        paymentsManagementDTO.getPerson().getReceiptsSet().size();
        try {

            CreatePaymentsForEvents.run(getUserView(request).getPerson().getUser(),
                    paymentsManagementDTO.getSelectedEntries(), PaymentMode.CASH,
                    paymentsManagementDTO.getPaymentDate());

            request.setAttribute("personId", paymentsManagementDTO.getPerson().getExternalId());

            return mapping.findForward("showReceipt");

        } catch (DomainExceptionWithLabelFormatter ex) {
            addActionMessage(request, ex.getKey(), solveLabelFormatterArgs(request, ex.getLabelFormatterArgs()));
            request.setAttribute("paymentsManagementDTO", paymentsManagementDTO);
            return mapping.findForward("preparePayment");
        } catch (DomainException ex) {

            addActionMessage(request, ex.getKey(), ex.getArgs());
            request.setAttribute("paymentsManagementDTO", paymentsManagementDTO);
            return mapping.findForward("preparePayment");
        }

    }

    protected Person getPerson(HttpServletRequest request) {
        return getDomainObject(request, "personId");
    }

    public ActionForward prepareShowEventsInvalid(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) {

        request.setAttribute("paymentsManagementDTO", RenderUtils.getViewState("paymentsManagementDTO").getMetaObject()
                .getObject());

        return mapping.findForward("showEvents");
    }

    public ActionForward backToShowOperations(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) {
        setContextInformation(request);
        return findMainForward(mapping);
    }

    protected void setContextInformation(HttpServletRequest request) {
        request.setAttribute("person", getPerson(request));
    }

    protected ActionForward findMainForward(final ActionMapping mapping) {
        return mapping.findForward("showOperations");
    }

    public ActionForward showOperations(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) {

        request.setAttribute("person", getPerson(request));

        return mapping.findForward("showOperations");
    }

    protected Event getEvent(HttpServletRequest request) {
        return getDomainObject(request, "eventId");
    }

}
