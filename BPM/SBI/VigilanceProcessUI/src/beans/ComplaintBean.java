package beans;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import util.ADFUtil;

public class ComplaintBean {
    public ComplaintBean() {
    }

    public String customSubmit() {
        // Add event code here...
        
        ViewObject caseRegVO = ADFUtil.findIterator("CaseRegistrationVOIterator").getViewObject();
        Row caseRow  = caseRegVO.getCurrentRow();
        
        ADFUtil.setEL("#{bindings.SourceOfCase.inputValue}", caseRow.getAttribute("Sourceofcase"));
        ADFUtil.setEL("#{bindings.Signed.inputValue}", caseRow.getAttribute("Signed"));
        ADFUtil.setEL("#{bindings.ComplaintNumber.inputValue}", caseRow.getAttribute("Systemsernum"));
        ADFUtil.setEL("#{bindings.attribute1.inputValue}", caseRow.getAttribute("Circlenm"));
        
        ADFUtil.invokeEL("#{bindings.commit.execute}");
        
        return (String)ADFUtil.invokeEL("#{invokeActionBean.invokeOperation}");
    }
}
