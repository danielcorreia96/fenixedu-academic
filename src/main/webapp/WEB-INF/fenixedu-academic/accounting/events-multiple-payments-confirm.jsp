<%--

    Copyright © 2017 Instituto Superior Técnico

    This file is part of FenixEdu Academic.

    FenixEdu Academic is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FenixEdu Academic is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with FenixEdu Academic.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ page trimDirectiveWhitespaces="true" %>

<link rel="stylesheet" type="text/css" media="screen" href="<%= request.getContextPath() %>/CSS/accounting.css"/>

<script type="text/javascript">

    function setTotalAmount(amount) {
        $('#totalAmount').text(Math.round(amount*100)/100);
        $('#totalAmount').val(Math.round(amount*100)/100);
    }

    function recalculateAmount() {
        setTotalAmount(getAmount('input.penalty') + getAmount("input.debt:checked"));
        $("#submitForm").prop('disabled', ($('#totalAmount').val() <= 0));
    }

    function getAmount(clazz) {
        var amounts = $(clazz).map(function() {
            return parseFloat($(this).data('amount'));
        }).toArray();

        return amounts.reduce(function(a, b) {
            return a + b;
        }, 0);
    }

    $(document).ready(function() {
        $('input.debt').click(function(e) {
            recalculateAmount();
        });

        $('#selectAllDebts').click(function () {
            $('input.debt').prop('checked', this.checked);
            recalculateAmount();
        });

        recalculateAmount();
    });

</script>

<div class="container-fluid">
    <main>
        <header>
            <div class="row">
                <div class="col-md-12">
                    <h1>Pagamento de Dívidas</h1>
                </div>
            </div>
            <div class="row">
                <div class="col-md-5 col-sm-12">
                    <div class="overall-description">
                        <dl>
                            <dt><spring:message code="label.name" text="Name"/></dt>
                            <dd><c:out value="${person.presentationName}"/></dd>
                        </dl>
                        <dl>
                            <dt><spring:message code="label.document.id.type" text="ID Document Type"/></dt>
                            <dd><c:out value="${person.idDocumentType.localizedName}"/></dd>
                        </dl>
                        <dl>
                            <dt><spring:message code="label.document.id" text="ID Document"/></dt>
                            <dd><c:out value="${person.documentIdNumber}"/></dd>
                        </dl>
                    </div>
                </div>
            </div>
        </header>

        <div class="row">
            <div class="col-md-12">
                <h2><spring:message code="accounting.event.payment.options.debts.and.interests" text="Debts and Interests"/></h2>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="overall-description">
                    <dl>
                        <dt>Data de pagamento</dt>
                        <dd><c:out value="${paymentsManagementDTO.paymentDate}"/></dd>
                    </dl>
                    <dl>
                        <dt>Valor Total</dt>
                        <dd><c:out value="${paymentsManagementDTO.totalAmountToPay}"/></dd>
                    </dl>
                </div>
            </div>
        </div>

        <form:form modelAttribute="paymentsManagementDTO" role="form" class="form-horizontal" action="cenas" method="post">
            ${csrf.field()}
            <div class="row">
                <div class="col-md-6">
                    <section class="list-debts">
                        <table class="table">
                            <thead>
                            <tr>
                                <th>Descrição</th>
                                <th>Valor a pagar</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${paymentsManagementDTO.selectedEntries}" var="entryDTO" varStatus="status">
                                <tr>
                                    <td><c:out value="${entryDTO.description}"/></td>
                                    <td><c:out value="${entryDTO.amountToPay}"/><span>€</span></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </section>
                </div>
            </div>
            <button class="btn btn-primary" type="submit">Confirmar pagamento</button>
        </form:form>
        <spring:url var="cancelUrl" value="../../{user}">
            <spring:param name="user" value="${person.username}"/>
        </spring:url>
        <a href="${cancelUrl}" class="btn btn-default">Cancelar</a>
    </main>
</div>