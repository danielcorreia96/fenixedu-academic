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
 * @(#)RequestUtils.java Created on Oct 24, 2004
 * 
 */
package org.fenixedu.academic.ui.struts.action.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.util.LabelValueBean;
import org.fenixedu.academic.util.Bundle;
import org.fenixedu.bennu.core.i18n.BundleUtil;

/**
 * 
 * @author Luis Cruz
 * @version 1.1, Oct 24, 2004
 * @since 1.1
 * 
 */
public class RequestUtils {

    public static String getAndSetStringToRequest(HttpServletRequest request, String name) {
        String parameter = request.getParameter(name);
        if (parameter == null) {
            parameter = (String) request.getAttribute(name);
        }
        request.setAttribute(name, parameter);
        return parameter;
    }

    public static final List<LabelValueBean> buildCurricularYearLabelValueBean() {
        final List<LabelValueBean> curricularYears = new ArrayList<LabelValueBean>();
        curricularYears.add(new LabelValueBean(BundleUtil.getString(Bundle.RENDERER, "renderers.menu.default.title"), ""));
        curricularYears.add(new LabelValueBean(BundleUtil.getString(Bundle.ENUMERATION, "1.ordinal.short"), "1"));
        curricularYears.add(new LabelValueBean(BundleUtil.getString(Bundle.ENUMERATION, "2.ordinal.short"), "2"));
        curricularYears.add(new LabelValueBean(BundleUtil.getString(Bundle.ENUMERATION, "3.ordinal.short"), "3"));
        curricularYears.add(new LabelValueBean(BundleUtil.getString(Bundle.ENUMERATION, "4.ordinal.short"), "4"));
        curricularYears.add(new LabelValueBean(BundleUtil.getString(Bundle.ENUMERATION, "5.ordinal.short"), "5"));
        return curricularYears;
    }

    public static void sendLoginRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/login");
    }

}
