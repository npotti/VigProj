package beans;

import java.util.Calendar;

import javax.faces.application.FacesMessage;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import utils.ADFUtil;

public class RegCompBean {
    private RichInputText sysSerNoBinding;

    public RegCompBean() {
    }

    public String customSubmit() {
        // Add event code here...
        
        ViewObject branchVO = ADFUtil.findIterator("BranchTransVOIterator").getViewObject();
        Row branchRow = branchVO.getCurrentRow();
        
        String source = (String)branchRow.getAttribute("Source");
        if(source == null || "".equals(source)){
            ADFUtil.showFacesMessage("Select source of case", FacesMessage.SEVERITY_ERROR);
            return null;
        }
        
        /**Set Case Registration attributes to BPM */
        
        ADFUtil.setEL("#{bindings.SourceOfCase.inputValue}", branchRow.getAttribute("Source"));
        ADFUtil.setEL("#{bindings.SystemSerialNo.inputValue}", branchRow.getAttribute("SysSerNumber"));
        ADFUtil.setEL("#{bindings.BranchCode.inputValue}", branchRow.getAttribute("Brcode"));
        ADFUtil.setEL("#{bindings.BranchName.inputValue}", branchRow.getAttribute("Brname"));
        ADFUtil.setEL("#{bindings.Region.inputValue}", branchRow.getAttribute("Region"));
        ADFUtil.setEL("#{bindings.OfficeType.inputValue}", branchRow.getAttribute("AoMcro"));
        ADFUtil.setEL("#{bindings.NetWork.inputValue}", branchRow.getAttribute("NetWork"));
        ADFUtil.setEL("#{bindings.Circle.inputValue}", branchRow.getAttribute("Circle"));
        ADFUtil.setEL("#{bindings.Signed.inputValue}", branchRow.getAttribute("Signed"));
        ADFUtil.setEL("#{bindings.CCFileNumber.inputValue}", branchRow.getAttribute("CcFileNumber"));
        
        return (String)ADFUtil.invokeEL("#{invokeActionBean.invokeOperation}");
    }

    public void branchCodeVC(ValueChangeEvent valueChangeEvent) {
        valueChangeEvent.getComponent().processUpdates(FacesContext.getCurrentInstance());
        generateSystemSerNo();
        AdfFacesContext.getCurrentInstance().addPartialTarget(sysSerNoBinding);
    }
    
    private void generateSystemSerNo(){
        String circle = (String)ADFUtil.evaluateEL("#{bindings.Circle1.inputValue}");
        Integer startYear = new Integer(0);
        Integer endYear = new Integer(0);
        String uniqueNumber = "1234";
        Calendar cal = Calendar.getInstance();
        Integer month = cal.get(Calendar.MONTH);
        if(month > 3){
            startYear = cal.get(Calendar.YEAR);
            endYear = cal.get(Calendar.YEAR) + 1;
        }
        else{
            startYear = cal.get(Calendar.YEAR) - 1;
            endYear = cal.get(Calendar.YEAR);
        }
        String sysSerNum = circle + ":" + startYear + ":" + endYear + ":" + uniqueNumber;
        ADFUtil.setEL("#{bindings.SysSerNumber.inputValue}", sysSerNum);
    }

    public void setSysSerNoBinding(RichInputText sysSerNoBinding) {
        this.sysSerNoBinding = sysSerNoBinding;
    }

    public RichInputText getSysSerNoBinding() {
        return sysSerNoBinding;
    }
}
