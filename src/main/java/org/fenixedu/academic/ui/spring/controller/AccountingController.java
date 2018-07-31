package org.fenixedu.academic.ui.spring.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.fenixedu.academic.domain.accounting.Event;
import org.fenixedu.academic.domain.accounting.calculator.CreditEntry;
import org.fenixedu.academic.domain.accounting.calculator.Debt;
import org.fenixedu.academic.domain.accounting.calculator.DebtExemption;
import org.fenixedu.academic.domain.accounting.calculator.DebtInterestCalculator;
import org.fenixedu.academic.domain.accounting.calculator.Payment;
import org.fenixedu.academic.ui.spring.service.AccountingManagementAccessControlService;
import org.fenixedu.academic.ui.spring.service.AccountingManagementService;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import pt.ist.fenixframework.FenixFramework;

/**
 * Created by Sérgio Silva (hello@fenixedu.org).
 */
public abstract class AccountingController {


    protected final AccountingManagementService accountingManagementService;
    protected final AccountingManagementAccessControlService accessControlService;
    protected final ServletContext servletContext;

    public AccountingController(AccountingManagementService accountingManagementService,
            AccountingManagementAccessControlService accountingManagementAccessControlService, ServletContext servletContext) {
        this.accountingManagementService = accountingManagementService;
        this.accessControlService = accountingManagementAccessControlService;
        this.servletContext = servletContext;
    }

    @ModelAttribute("entrypointUrl")
    public abstract String entrypointUrl();

    @RequestMapping("{user}")
    public String events(@PathVariable User user, Model model) {
        if (Authenticate.getUser() == user || accessControlService.isPaymentManager(Authenticate.getUser(), Collections.emptySet())) {
            model.addAttribute("name", user.getPerson().getPresentationName());
            model.addAttribute("idDocumentType", user.getPerson().getIdDocumentType().getLocalizedName());
            model.addAttribute("idDocument", user.getPerson().getDocumentIdNumber());

            model.addAttribute("openEvents", user.getPerson().getEventsSet().stream().filter(Event::isOpen).sorted(Comparator
                    .comparing(Event::getWhenOccured).reversed()).collect(Collectors.toList()));

            model.addAttribute("otherEvents", user.getPerson().getEventsSet().stream().filter(e -> !e.isOpen()).sorted(Comparator
                    .comparing(Event::getWhenOccured).reversed()).collect(Collectors.toList()));

            return view("events");
        }
        throw new UnsupportedOperationException("Unauthorized");
    }

    @RequestMapping("{event}/details")
    public String details(@PathVariable Event event, @RequestParam(value = "date", defaultValue = "#{new org.joda.time.DateTime()}") DateTime date,  Model model) {
        accessControlService.checkEventOwnerOrPaymentManager(event, Authenticate.getUser());

        final DebtInterestCalculator debtInterestCalculator = event.getDebtInterestCalculator(date);
        final List<CreditEntry> creditEntries = debtInterestCalculator.getCreditEntries();
        Collections.reverse(creditEntries);

        model.addAttribute("eventUsername", event.getPerson().getUsername());
        model.addAttribute("debts", debtInterestCalculator.getDebtsOrderedByDueDate());
        model.addAttribute("payments", creditEntries);
        model.addAttribute("eventId", event.getExternalId());
        model.addAttribute("eventCreationDate", event.getWhenOccured());
        model.addAttribute("eventDescription", event.getDescription());
        model.addAttribute("currentDate", date.toLocalDate());

        model.addAttribute("eventTotalAmountToPay", debtInterestCalculator.getTotalDueAmount());
        model.addAttribute("eventDebtAmountToPay", debtInterestCalculator.getDueAmount());
        model.addAttribute("eventInterestAmountToPay", debtInterestCalculator.getDueInterestAmount());
        model.addAttribute("eventFineAmountToPay", debtInterestCalculator.getDueFineAmount());
        model.addAttribute("eventOriginalAmountToPay", event.getOriginalAmountToPay());

        return view("event-details");
    }

    @RequestMapping("{event}/debt/{debtDueDate}/details")
    public String debtDetails(@PathVariable Event event, @PathVariable LocalDate debtDueDate, Model model) {
        accessControlService.checkEventOwnerOrPaymentManager(event, Authenticate.getUser());
        final DebtInterestCalculator debtInterestCalculator = event.getDebtInterestCalculator(new DateTime());
        List<Debt> debtsOrderedByDueDate = debtInterestCalculator.getDebtsOrderedByDueDate();
        Debt debt = debtsOrderedByDueDate.stream().filter(d -> d.getDueDate().equals(debtDueDate)).findAny()
                .orElseThrow(UnsupportedOperationException::new);

        model.addAttribute("eventId", event.getExternalId());
        model.addAttribute("eventDescription", event.getDescription());
        model.addAttribute("debtIndex", debtsOrderedByDueDate.indexOf(debt) + 1);
        model.addAttribute("debt", debt);
        model.addAttribute("payments", accountingManagementService.createPaymentSummaries(debt));
        return view("event-debt-details");
    }

    @RequestMapping("{event}/{creditEntryId}/details")
    public String creditEntryDetails(@PathVariable Event event, @PathVariable String creditEntryId, Model model) {
        accessControlService.checkEventOwnerOrPaymentManager(event, Authenticate.getUser());
        final DebtInterestCalculator debtInterestCalculator = event.getDebtInterestCalculator(new DateTime());
        final CreditEntry creditEntry = getCreditEntryById(debtInterestCalculator, creditEntryId);
        model.addAttribute("eventId", event.getExternalId());
        model.addAttribute("payment", creditEntry);
        model.addAttribute("transactionDetail", FenixFramework.getDomainObject(creditEntryId));
        model.addAttribute("processedDate", creditEntry.getCreated());
        model.addAttribute("registeredDate", creditEntry.getDate());
        model.addAttribute("amount", creditEntry.getAmount());
        model.addAttribute("payments", accountingManagementService.createPaymentSummaries(creditEntry));
        if (creditEntry instanceof Payment)
            return view("event-payment-details");
        else if (creditEntry instanceof DebtExemption){
            return view("event-debtExemption-details");
        }
        else {
            throw new UnsupportedOperationException();
        }
    }

    private CreditEntry getCreditEntryById(DebtInterestCalculator debtInterestCalculator, String creditEntryId) {
        final Optional<Payment> paymentById = debtInterestCalculator.getPaymentById(creditEntryId);
        if (paymentById.isPresent()) {
            return paymentById.get();
        }

        final Optional<DebtExemption> debtExemptionById = debtInterestCalculator.getDebtExemptionById(creditEntryId);
        if (debtExemptionById.isPresent()) {
            return debtExemptionById.get();
        }

        throw new UnsupportedOperationException();
    }

    protected String view(String view) {
        return "fenixedu-academic/accounting/" + view;
    }
}
