package org.fenixedu.academic.ui.spring.controller;

import org.fenixedu.academic.domain.accounting.AccountingTransaction;
import org.fenixedu.academic.domain.accounting.Discount;
import org.fenixedu.academic.domain.accounting.Event;
import org.fenixedu.academic.domain.accounting.Exemption;
import org.fenixedu.academic.domain.accounting.calculator.Debt;
import org.fenixedu.academic.domain.accounting.calculator.DebtInterestCalculator;
import org.fenixedu.academic.domain.accounting.calculator.Fine;
import org.fenixedu.academic.domain.accounting.calculator.Interest;
import org.fenixedu.academic.domain.accounting.events.EventExemptionJustificationType;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.academic.dto.accounting.AnnulAccountingTransactionBean;
import org.fenixedu.academic.dto.accounting.CreateExemptionBean;
import org.fenixedu.academic.dto.accounting.CreateExemptionBean.ExemptionType;
import org.fenixedu.academic.dto.accounting.DepositAmountBean;
import org.fenixedu.academic.dto.accounting.EntryDTO;
import org.fenixedu.academic.dto.accounting.PaymentsManagementDTO;
import org.fenixedu.academic.predicate.AcademicPredicates;
import org.fenixedu.academic.predicate.AccessControl;
import org.fenixedu.academic.service.services.accounting.AnnulAccountingTransaction;
import org.fenixedu.academic.service.services.accounting.DeleteExemption;
import org.fenixedu.academic.ui.spring.service.AccountingManagementAccessControlService;
import org.fenixedu.academic.ui.spring.service.AccountingManagementService;
import org.fenixedu.academic.util.Money;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import pt.ist.fenixframework.DomainObject;

import javax.servlet.ServletContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Sérgio Silva (hello@fenixedu.org).
 */
//@SpringApplication(path = "accounting", hint = "Accounting", group = "#managers", title = "title.manage.countries")
@Controller
@SpringFunctionality(app = AcademicAdministrationSpringApplication.class, title = "title.accounting.management")
@RequestMapping(AccountingEventsPaymentManagerController.REQUEST_MAPPING)
public class AccountingEventsPaymentManagerController extends AccountingController {
    private static final Logger logger = LoggerFactory.getLogger(AccountingEventsPaymentManagerController.class);

    static final String REQUEST_MAPPING = "/accounting-management";

    @Autowired
    public AccountingEventsPaymentManagerController(AccountingManagementService accountingManagementService, AccountingManagementAccessControlService accountingManagementAccessControlService, ServletContext servletContext) {
        super(accountingManagementService, accountingManagementAccessControlService, servletContext);
    }

    @Override
    public String entrypointUrl() {
        return REQUEST_MAPPING + "/{user}";
    }

    @RequestMapping
    public String entrypoint(User loggedUser) {
        return "redirect:" + REQUEST_MAPPING + "/" + loggedUser.getUsername();
    }

    @RequestMapping("{event}/summary")
    public String summary(@PathVariable Event event, User user, Model model) {
        accessControlService.checkPaymentManager(event, user);
        final DebtInterestCalculator debtInterestCalculator = event.getDebtInterestCalculator(new DateTime());
        model.addAttribute("entrypointUrl", entrypointUrl());
        model.addAttribute("eventUsername", event.getPerson().getUsername());
        model.addAttribute("creditEntries", debtInterestCalculator.getCreditEntries());
        model.addAttribute("debtCalculator", debtInterestCalculator);
        return view("event-summary");
    }

    @RequestMapping("{event}/delete/{transaction}")
    public String delete(@PathVariable DomainObject transaction, @PathVariable Event event, User user, Model model) {
        accessControlService.checkAdvancedPaymentManager(event, user);
        if (transaction instanceof AccountingTransaction) {
            model.addAttribute("annulAccountingTransactionBean", new AnnulAccountingTransactionBean((AccountingTransaction) transaction));
            model.addAttribute("event", event);
            return view("event-annul-transaction");
        }
        else if (transaction instanceof Exemption){
            try {
                DeleteExemption.run((Exemption) transaction);
            }
            catch (DomainException e) {
                model.addAttribute("error", e.getLocalizedMessage());
            }
        }
        else if (transaction instanceof Discount) {
            try {
                AccessControl.check(AcademicPredicates.MANAGE_STUDENT_PAYMENTS);
                ((Discount) transaction).delete();
            } catch (DomainException e) {
                model.addAttribute("error", e.getLocalizedMessage());
            }
        }
        else {
            throw new UnsupportedOperationException(String.format("Can't delete unknown transaction %s%n", transaction.getClass
                    ().getSimpleName()));
        }
        return redirectToEventDetails(event);
    }

    @RequestMapping(value = "{event}/deleteTransaction", method = RequestMethod.POST)
    public String deleteTransaction(@PathVariable Event event, User user, Model model,
            @ModelAttribute AnnulAccountingTransactionBean annulAccountingTransactionBean){
        accessControlService.checkAdvancedPaymentManager(event, user);
        try {
            AnnulAccountingTransaction.run(annulAccountingTransactionBean);
        }
        catch (DomainException e){
            model.addAttribute("error", e.getLocalizedMessage());
        }
        return redirectToEventDetails(event);
    }


    @RequestMapping(value = "{event}/deposit", method = RequestMethod.GET)
    public String deposit(@PathVariable Event event, User user, Model model){
        accessControlService.checkPaymentManager(event, user);

        model.addAttribute("person", event.getPerson());
        model.addAttribute("event", event);
        model.addAttribute("depositAmountBean", new DepositAmountBean());

        return view("event-deposit");
    }

    @RequestMapping(value = "{event}/depositAmount", method = RequestMethod.POST)
    public String depositAmount(@PathVariable Event event, User user, Model model, @ModelAttribute DepositAmountBean depositAmountBean) {
        accessControlService.checkPaymentManager(event, user);

        try {
            accountingManagementService.depositAmount(event, user, depositAmountBean);
        }
        catch (DomainException e) {
            model.addAttribute("error", e.getLocalizedMessage());
            return deposit(event, user, model);
        }

        return redirectToEventDetails(event);
    }

    @RequestMapping(value = "{event}/cancel", method = RequestMethod.GET)
    public String cancel(@PathVariable Event event, User user, Model model) {
        accessControlService.checkAdvancedPaymentManager(event, user);

        model.addAttribute("person", event.getPerson());
        model.addAttribute("event", event);

        return view("event-cancel");
    }

    @RequestMapping(value = "{event}/cancelEvent", method = RequestMethod.POST)
    public String cancelEvent(@PathVariable Event event, User user, Model model, @RequestParam String justification) {
        accessControlService.checkAdvancedPaymentManager(event, user);

        try {
            accountingManagementService.cancelEvent(event, user.getPerson(), justification);
        }
        catch (DomainException e) {
            model.addAttribute("error", e.getLocalizedMessage());
            return cancel(event, user, model);
        }

        return redirectToEventDetails(event);
    }

    @RequestMapping(value = "{event}/exempt", method = RequestMethod.GET)
    public String exempt(@PathVariable Event event, User user, Model model) {
        accessControlService.checkAdvancedPaymentManager(event, user);

        final DebtInterestCalculator calculator = event.getDebtInterestCalculator(new DateTime());

        if (calculator.getTotalDueAmount().compareTo(BigDecimal.ZERO) == 0) {
            return redirectToEventDetails(event);
        }

        model.addAttribute("event", event);
        model.addAttribute("person", event.getPerson());

        final Map<ExemptionType, BigDecimal> exemptionTypeAmountMap = new HashMap<>();
        exemptionTypeAmountMap.put(ExemptionType.DEBT, calculator.getDueAmount());
        exemptionTypeAmountMap.put(ExemptionType.INTEREST, calculator.getDueInterestAmount());
        exemptionTypeAmountMap.put(ExemptionType.FINE, calculator.getDueFineAmount());

        model.addAttribute("exemptionTypeAmountMap", exemptionTypeAmountMap);
        model.addAttribute("createExemptionBean", new CreateExemptionBean());
        model.addAttribute("eventExemptionJustificationTypes", EventExemptionJustificationType.values());

        return view("event-create-exemption");
    }

    @RequestMapping(value = "{event}/createExemption", method = RequestMethod.POST)
    public String createExemption(@PathVariable Event event, User user, Model model, @ModelAttribute CreateExemptionBean createExemptionBean){
        accessControlService.checkAdvancedPaymentManager(event, user);

        try {
            accountingManagementService.exemptEvent(event, user.getPerson(), createExemptionBean);
        }
        catch (DomainException e) {
            model.addAttribute("error", e.getLocalizedMessage());
            return exempt(event, user, model);
        }

        return redirectToEventDetails(event);
    }

    @RequestMapping(value = "{user}/multiplePayments/select", method = RequestMethod.GET)
    public String prepareMultiplePayments(@PathVariable User user, Model model, User loggedUser){
        accessControlService.isPaymentManager(loggedUser);

        PaymentsManagementDTO paymentsManagementDTO = new PaymentsManagementDTO(user.getPerson());
        user.getPerson().getEventsSet().stream().map(Event::calculateEntries).forEach(paymentsManagementDTO::addEntryDTOs);

        // Show penalties first then order by due date
        paymentsManagementDTO.getEntryDTOs().sort(Comparator.comparing(EntryDTO::isForPenalty).reversed().thenComparing(EntryDTO::getDueDate));

        if (paymentsManagementDTO.getTotalAmountToPay().lessOrEqualThan(Money.ZERO)) {
            logger.warn("Hacky user {} tried to access multiple payments interface for user {}",
                    Optional.ofNullable(loggedUser).map(User::getUsername).orElse("---"), user.getUsername());
            return "redirect:" + REQUEST_MAPPING + "/" + user.getUsername();
        }

        model.addAttribute("paymentsManagementDTO", paymentsManagementDTO);
        model.addAttribute("entryDTOsList", new ArrayList<EntryDTO>());
        model.addAttribute("person", user.getPerson());

        return view("events-multiple-payments-prepare");
    }

    @RequestMapping(value = "{user}/multiplePayments/confirm", method = RequestMethod.POST)
    public String confirmMultiplePayments(@PathVariable User user, Model model, User loggedUser, @ModelAttribute List<EntryDTO> entryDTOsList) {
        accessControlService.isPaymentManager(loggedUser);

//        entryDTOS.setPaymentDate(DateTime.now());

        model.addAttribute("person", user.getPerson());

        return view("events-multiple-payments-confirm");
    }


    private String redirectToEventDetails(@PathVariable Event event) {
        return String.format("redirect:%s/%s/details", REQUEST_MAPPING, event.getExternalId());
    }

    private List<Interest> getInterests(DebtInterestCalculator debtInterestCalculator) {
        return debtInterestCalculator.getDebtsOrderedByDueDate().stream()
                .flatMap(d -> d.getInterests().stream())
                .collect(Collectors.toList());
    }

    private List<Fine> getFines(DebtInterestCalculator debtInterestCalculator) {
        return debtInterestCalculator.getDebtsOrderedByDueDate().stream()
                .flatMap(d -> d.getFines().stream())
                .collect(Collectors.toList());
    }
}