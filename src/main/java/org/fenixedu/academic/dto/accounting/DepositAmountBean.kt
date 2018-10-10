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
 * along with FenixEdu Academic.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package org.fenixedu.academic.dto.accounting

import java.io.Serializable

import org.fenixedu.academic.domain.accounting.EntryType
import org.fenixedu.academic.domain.accounting.PaymentMethod
import org.fenixedu.academic.util.Money
import org.joda.time.DateTime
import org.springframework.format.annotation.DateTimeFormat

class DepositAmountBean : Serializable {

    var entryType: EntryType? = null

    var amount: Money? = null

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var whenRegistered: DateTime? = null

    var paymentMethod: PaymentMethod? = null

    var paymentReference: String? = null

    var reason: String? = null

    init {
        whenRegistered = DateTime()
    }

    companion object {

        private const val serialVersionUID = 1L
    }
}
