/*
 * Created on 14/Mar/2003
 *
 */
package ServidorApresentacao.Action.masterDegree.administrativeOffice.contributor;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.actions.DispatchAction;

import DataBeans.InfoContributor;
import ServidorAplicacao.GestorServicos;
import ServidorAplicacao.IUserView;
import ServidorAplicacao.Servico.exceptions.ExistingServiceException;
import ServidorApresentacao.Action.exceptions.ExistingActionException;
import ServidorApresentacao.Action.sop.utils.SessionConstants;
import ServidorApresentacao.Action.sop.utils.SessionUtils;

/**
 * 
 * @author Nuno Nunes (nmsn@rnl.ist.utl.pt)
 *         Joana Mota (jccm@rnl.ist.utl.pt)
 * 
 * 
 */
public class ListContributorsDispatchAction extends DispatchAction {

	public ActionForward prepare(ActionMapping mapping, ActionForm form,
									HttpServletRequest request,
									HttpServletResponse response)
		throws Exception {


		SessionUtils.validSessionVerification(request, mapping);
		HttpSession session = request.getSession(false);

		if (session != null) {
			DynaActionForm createContributorForm = (DynaActionForm) form;

			// Clean the form
			createContributorForm.set("contributorNumber", null);

			
			String action = request.getParameter("action");
			
			if (action.equals("visualize")) {
				session.removeAttribute(SessionConstants.CONTRIBUTOR_ACTION);
				session.setAttribute(SessionConstants.CONTRIBUTOR_ACTION, "label.action.contributor.visualize");
			}
			else if (action.equals("edit")) {
				session.removeAttribute(SessionConstants.CONTRIBUTOR_ACTION);
				session.setAttribute(SessionConstants.CONTRIBUTOR_ACTION, "label.action.contributor.edit");
			}
			
			return mapping.findForward("PrepareReady");
		  } else
			throw new Exception();   

	}
		

	public ActionForward getContributors(ActionMapping mapping, ActionForm form,
										HttpServletRequest request,
										HttpServletResponse response)
		throws Exception {


		SessionUtils.validSessionVerification(request, mapping);
		HttpSession session = request.getSession(false);

		if (session != null) {
			
			DynaActionForm createCandidateForm = (DynaActionForm) form;

			GestorServicos serviceManager = GestorServicos.manager();
			
			IUserView userView = (IUserView) session.getAttribute(SessionConstants.U_VIEW);
			
			// Get the Information

			String contributorNumberString = (String) createCandidateForm.get("contributorNumber");
			Integer contributorNumber = null;		
			if ((contributorNumberString != null) && (contributorNumberString.length() != 0))
				contributorNumber = new Integer(contributorNumberString);


	  		List contributors = null;
	  		
	  		Object args[] = { contributorNumber };
	  		try {
				contributors = (List) serviceManager.executar(userView, "ReadContributorList", args);
			} catch (Exception e) {
				throw new Exception(e);
			}

			if (contributors.size() == 1) {
				InfoContributor infoContributor = (InfoContributor) contributors.get(0);
				session.removeAttribute(SessionConstants.CONTRIBUTOR);
				session.setAttribute(SessionConstants.CONTRIBUTOR, infoContributor);
				return mapping.findForward("ActionReady");
			}
			
		  session.removeAttribute(SessionConstants.CONTRIBUTOR_LIST);
		  session.setAttribute(SessionConstants.CONTRIBUTOR_LIST, contributors);
		  return mapping.findForward("ChooseContributor");
		} else
		  throw new Exception();   
	  }
	  
	public ActionForward chooseContributor(ActionMapping mapping, ActionForm form,
										 HttpServletRequest request,
										 HttpServletResponse response)
		throws Exception {

		SessionUtils.validSessionVerification(request, mapping);
		HttpSession session = request.getSession(false);

		if (session != null) {
			

			GestorServicos serviceManager = GestorServicos.manager();
			
			IUserView userView = (IUserView) session.getAttribute(SessionConstants.U_VIEW);
			List contributorList = (List) session.getAttribute(SessionConstants.CONTRIBUTOR_LIST);
			

			Integer choosenContributorPosition = Integer.valueOf(request.getParameter("contributorPosition"));
			
			
			// Put the selected Contributor in Session
			InfoContributor infoContributor = (InfoContributor) contributorList.get(choosenContributorPosition.intValue());
		
			session.setAttribute(SessionConstants.CONTRIBUTOR, infoContributor);
			return mapping.findForward("ActionReady");
			
		} else
	  		throw new Exception();  
	}
	  
	  
	public ActionForward prepareEdit(ActionMapping mapping, ActionForm form,
										 HttpServletRequest request,
										 HttpServletResponse response)
		throws Exception {

System.out.println("Prepare Edit");

		SessionUtils.validSessionVerification(request, mapping);
		HttpSession session = request.getSession(false);

		if (session != null) {
			DynaActionForm editContributorForm = (DynaActionForm) form;

			GestorServicos serviceManager = GestorServicos.manager();
			
			IUserView userView = (IUserView) session.getAttribute(SessionConstants.U_VIEW);
			InfoContributor infoContributor = (InfoContributor) session.getAttribute(SessionConstants.CONTRIBUTOR); 


System.out.println("Contribuinte em sessao : " + infoContributor);

			// Fill in The Form
			
			editContributorForm.set("contributorNumber", String.valueOf(infoContributor.getContributorNumber()));
			editContributorForm.set("contributorName", infoContributor.getContributorName());
			editContributorForm.set("contributorAddress", infoContributor.getContributorAddress());
			
			return mapping.findForward("EditReady");
			
		} else
			throw new Exception();  
	}
	
	
	public ActionForward edit(ActionMapping mapping, ActionForm form,
										 HttpServletRequest request,
										 HttpServletResponse response)
		throws Exception {

		SessionUtils.validSessionVerification(request, mapping);
		HttpSession session = request.getSession(false);

		if (session != null) {
			DynaActionForm editContributorForm = (DynaActionForm) form;

			GestorServicos serviceManager = GestorServicos.manager();
			
			IUserView userView = (IUserView) session.getAttribute(SessionConstants.U_VIEW);
			InfoContributor infoContributor = (InfoContributor) session.getAttribute(SessionConstants.CONTRIBUTOR);
 

			// Get the Information
			String contributorNumberString = (String) editContributorForm.get("contributorNumber");
			Integer contributorNumber = new Integer(contributorNumberString);
			String contributorName = (String) editContributorForm.get("contributorName");
			String contributorAddress = (String) editContributorForm.get("contributorAddress");
			
			Object args[] = { infoContributor, contributorNumber , contributorName, contributorAddress};
			try {
				serviceManager.executar(userView, "EditContributor", args);
			} catch (ExistingServiceException e) {
				throw new ExistingActionException("O Contribuinte", e);
			}
			
			session.removeAttribute(SessionConstants.CONTRIBUTOR);
			return mapping.findForward("EditSuccess");
			
		} else
			throw new Exception();  
	}
	  
}
