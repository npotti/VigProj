package com.sbi.vigilance.app.view.beans;

import com.sbi.vigilance.app.view.util.ADFUtil;

import javax.faces.application.FacesMessage;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;

public class RegisterBean {
    public RegisterBean() {
    }

    public String customSubmit() {
        
        
        /**Check If Source of case is selected*/

        ViewObject branchVO = ADFUtil.findIterator("BrmastTransVOIterator").getViewObject();
        Row branchRow = branchVO.getCurrentRow();

        String source = (String)branchRow.getAttribute("Source");
        if(source == null || "".equals(source)){
            ADFUtil.showFacesMessage("Select source of case", FacesMessage.SEVERITY_ERROR);
            return null;
        }
        
        /**Set Case Registration attributes to BPM */
        
        ADFUtil.setEL("#{bindings.SourceOfCase.inputValue}", branchRow.getAttribute("Source"));
        ADFUtil.setEL("#{bindings.SystemSerialNo.inputValue}", branchRow.getAttribute("SysSerNumber"));
        ADFUtil.setEL("#{bindings.BranchCode.inputValue}", branchRow.getAttribute("Brcd"));
        ADFUtil.setEL("#{bindings.BranchName.inputValue}", branchRow.getAttribute("Brname"));
        ADFUtil.setEL("#{bindings.Region.inputValue}", branchRow.getAttribute("Region"));
        ADFUtil.setEL("#{bindings.OfficeType.inputValue}", branchRow.getAttribute("AoMcro"));
        ADFUtil.setEL("#{bindings.NetWork.inputValue}", branchRow.getAttribute("NetWork"));
        ADFUtil.setEL("#{bindings.Circle.inputValue}", branchRow.getAttribute("Circle"));
        ADFUtil.setEL("#{bindings.Signed.inputValue}", branchRow.getAttribute("Signed"));
        ADFUtil.setEL("#{bindings.CCFileNumber.inputValue}", branchRow.getAttribute("CcFileNumber"));
        
        
        /**If Source is Complaint, then Set Complaint Attributes to BPM*/
        
        if(source != null && "Complaint".equalsIgnoreCase(source)){
            ViewObject complaintVO = ADFUtil.findIterator("ComplaintTransVOIterator").getViewObject();
            Row complaintRow = complaintVO.getCurrentRow();
            ADFUtil.setEL("#{ReceivedFromBPM}", complaintRow.getAttribute("ReceivedFrom"));
        }
        
        /**Invoke BPM Submit method*/
        
        ADFUtil.invokeEL("#{bindings.SUBMIT.execute}");
        
        return null;
    }
}
