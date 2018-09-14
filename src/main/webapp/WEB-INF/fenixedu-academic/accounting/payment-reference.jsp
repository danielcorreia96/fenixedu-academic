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
<spring:url var="backUrl" value="../details">
</spring:url>

<div class="container-fluid">
    <main>
        <header>
            <div class="row">
                <div class="col-md-12">
                    <div class="row">
                        <div class="col-md-12">
                            <p><a href="${backUrl}" class="btn btn-default"><spring:message code="label.back" text="Back"/></a></p>
                            <h1>${description}</h1>
                        </div>
                    </div>
                    <h1>Referência de pagamento</h1>
                </div>
            </div>
        </header>

        <div class="row">
            <div class="col-sm-6 alert alert-warning">
                <h4><b>Informações da referência</b></h4>
                <c:choose>
                    <c:when test="${empty paymentCodeEntry.amount}">
                    <p style="white-space: pre-line;">
                        Esta referência pode ser reutilizada para todos os pagamentos realizados no contexto desta divida.
                        O utilizador deverá conferir o valor a pagar e respetivos prazos conforme os detalhes da divida.
                    </c:when>
                    <c:otherwise>
                    <p style="white-space: pre-line;">
                        Esta referência deverá ser utilizada apenas uma vez para efeitos de liquidação de juros.
                        O pagamento desta referência deverá ser realizado nas próximas 48 horas.
                        Se o pagamento for realizado após este prazo, poderão continuar a existir juros associados a esta divida.
                    </p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="row">
            <div class="col-sm-3">
                <section class="reference-card">
                    <dl>
                        <dt>Referencia:</dt>
                        <dd><c:out value="${paymentCodeEntry.paymentCode.formattedCode}"/></dd>
                    </dl>
                    <dl>
                        <dt>Entidade:</dt>
                        <dd><c:out value="${paymentCodeEntry.paymentCode.entityCode}"/></dd>
                    </dl>
                    <c:if test="${not empty paymentCodeEntry.amount}">
                        <dl>
                            <dt>Valor:</dt>
                            <dd><c:out value="${paymentCodeEntry.amount}"/><span>€</span></dd>
                        </dl>
                    </c:if>
                </section>
            </div>
        </div>

        <div class="row">
            <div class="col-sm-6 alert alert-info">
                <h4>
                    <span class="glyphicon glyphicon-info-sign" aria-hidden="true"></span> Dados de Faturação
                </h4>
                <p>
                    Antes de realizar o pagamento, deverá verificar se os dados de faturação apresentados estão corretos.
                </p>
            </div>
        </div>

        <div class="row">
            <div class="col-sm-3">
                <section class="reference-card">
                    <h4>Dados de faturação</h4>
                    <dl>
                        <dt>Referencia:</dt>
                        <dd><c:out value="${paymentCodeEntry.paymentCode.formattedCode}"/></dd>
                    </dl>
                    <dl>
                        <dt>Entidade:</dt>
                        <dd><c:out value="${paymentCodeEntry.paymentCode.entityCode}"/></dd>
                    </dl>
                </section>
            </div>
        </div>
    </main>
</div>