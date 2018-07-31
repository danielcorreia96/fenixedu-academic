package org.fenixedu.academic.ui.spring.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.accounting.calculator.CreditEntry;
import org.fenixedu.academic.domain.accounting.calculator.Debt;
import org.fenixedu.academic.domain.accounting.calculator.DebtEntry;
import org.fenixedu.academic.domain.accounting.calculator.Interest;
import org.fenixedu.academic.domain.accounting.calculator.PartialPayment;
import org.fenixedu.commons.i18n.LocalizedString;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Created by Sérgio Silva (hello@fenixedu.org).
 */
@Service
public class AccountingManagementService {

    public static class PaymentSummary {
        final String id;
        final LocalizedString typeDescription;
        final String description;
        final DateTime created;
        final LocalDate date;
        final BigDecimal amount;
        final BigDecimal amountUsedInDebt;
        final BigDecimal amountUsedInInterest;

        public PaymentSummary(String id, LocalizedString typeDescription, String description, DateTime created, LocalDate date,
                BigDecimal amount, BigDecimal amountUsedInDebt, BigDecimal amountUsedInInterest) {
            this.id = id;
            this.typeDescription = typeDescription;
            this.description = description;
            this.created = created;
            this.date = date;
            this.amount = amount;
            this.amountUsedInDebt = amountUsedInDebt;
            this.amountUsedInInterest = amountUsedInInterest;
        }

        public String getId() {
            return id;
        }

        public DateTime getCreated() {
            return created;
        }

        public LocalDate getDate() {
            return date;
        }

        public LocalizedString getTypeDescription() {
            return typeDescription;
        }

        public String getDescription() {
            return description;
        }

        public BigDecimal getAmountUsedInDebt() {
            return amountUsedInDebt;
        }

        public BigDecimal getAmountUsedInInterest() {
            return amountUsedInInterest;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            return String
                    .format("PaymentSummary{id='%s', typeDescription='%s' description='%s', created=%s, date=%s, amount=%s, amountUsedInDebt=%s, amountUsedInInterest=%s}",
                            id, typeDescription , description, created, date, amount, amountUsedInDebt, amountUsedInInterest);
        }
    }

    public static class PaymentSummaryWithDebt extends PaymentSummary {
        private final DebtEntry debtEntry;

        public PaymentSummaryWithDebt(String id, LocalizedString typeDescription, String description, DateTime created,
                LocalDate date, BigDecimal amount, BigDecimal amountUsedInDebt, BigDecimal amountUsedInInterest, DebtEntry debtEntry) {
            super(id, typeDescription , description, created, date, amount, amountUsedInDebt, amountUsedInInterest);
            this.debtEntry = debtEntry;
        }

        public DebtEntry getDebtEntry() {
            return debtEntry;
        }
    }

    public List<PaymentSummary> createPaymentSummaries(Debt debt) {
        final List<PaymentSummary> paymentSummaries = new ArrayList<>();
        final Multimap<CreditEntry, PartialPayment> creditEntryPartialPaymentMultimap = HashMultimap.create();

        Stream.concat(debt.getPartialPayments().stream(), debt.getInterests().stream().flatMap(i -> i.getPartialPayments()
                .stream())).distinct().forEach(pp -> {
            creditEntryPartialPaymentMultimap.put(pp.getCreditEntry(), pp);
        });

        for (CreditEntry creditEntry : creditEntryPartialPaymentMultimap.keySet()) {
            final BigDecimal amountUsedInDebts = creditEntryPartialPaymentMultimap.get(creditEntry).stream().filter(c -> c
                    .getDebtEntry() == debt).map(PartialPayment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            final BigDecimal amountUsedInInterest = creditEntryPartialPaymentMultimap.get(creditEntry).stream().filter(c -> c
                    .getDebtEntry() instanceof Interest).map(PartialPayment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            PaymentSummary paymentSummary = new PaymentSummary(creditEntry.getId(), creditEntry.getTypeDescription() , creditEntry.getDescription(),
                    creditEntry.getCreated(), creditEntry.getDate(), creditEntry.getAmount(), amountUsedInDebts, amountUsedInInterest);

            paymentSummaries.add(paymentSummary);
        }

        paymentSummaries.sort(Comparator.comparing(PaymentSummary::getCreated).reversed());

        return paymentSummaries;
    }

    public Set<PaymentSummaryWithDebt> createPaymentSummaries(CreditEntry creditEntry) {
        final Set<PaymentSummaryWithDebt> paymentSummaryWithDebtSet = new TreeSet<>(
                Comparator.comparing(a -> a.getDebtEntry().getDate()));

        final Multimap<DebtEntry, PartialPayment> debtEntryPartialPaymentMultimap = HashMultimap.create();

        for (PartialPayment partialPayment : creditEntry.getPartialPayments()) {
            DebtEntry debtEntry = partialPayment.getDebtEntry();
            if (debtEntry instanceof Interest) {
                debtEntryPartialPaymentMultimap.put(((Interest) debtEntry).getTarget(), partialPayment);
            }
            else {
                debtEntryPartialPaymentMultimap.put(debtEntry, partialPayment);
            }
        }

        for (DebtEntry debtEntry : debtEntryPartialPaymentMultimap.keySet()) {
            BigDecimal amountUsedInDebt = BigDecimal.ZERO;
            BigDecimal amountUsedInInterest = BigDecimal.ZERO;
            Collection<PartialPayment> partialPayments = debtEntryPartialPaymentMultimap.get(debtEntry);
            for (PartialPayment partialPayment : partialPayments) {
                if (partialPayment.getDebtEntry() instanceof Interest) {
                    amountUsedInInterest = amountUsedInInterest.add(partialPayment.getAmount());
                } else {
                    amountUsedInDebt = amountUsedInDebt.add(partialPayment.getAmount());
                }
            }
            paymentSummaryWithDebtSet.add(new PaymentSummaryWithDebt(debtEntry.getId(), creditEntry.getTypeDescription(), creditEntry.getDescription(),
                    creditEntry.getCreated(), creditEntry.getDate(), amountUsedInDebt.add(amountUsedInInterest), amountUsedInDebt, amountUsedInInterest,
                    debtEntry));
        }

        return paymentSummaryWithDebtSet;
    }
}
