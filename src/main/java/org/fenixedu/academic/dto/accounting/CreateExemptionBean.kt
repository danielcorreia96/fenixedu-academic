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

import org.fenixedu.academic.domain.accounting.events.EventExemptionJustificationType
import org.fenixedu.academic.util.Money
import org.joda.time.DateTime
import org.springframework.format.annotation.DateTimeFormat

import java.io.Serializable

class CreateExemptionBean : Serializable {

    var exemptionType: ExemptionType? = null

    var justificationType: EventExemptionJustificationType? = null

    var reason: String? = null

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var dispatchDate: DateTime? = null

    var value: Money? = null

    enum class ExemptionType {
        DEBT, INTEREST, FINE
    }

    init {
        dispatchDate = DateTime()
    }

    companion object {

        private const val serialVersionUID = 7480560410293395468L
    }
}
