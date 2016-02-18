package com.sbi.vigilance.app.view.beans;

import com.sbi.vigilance.app.view.util.ADFUtil;

import com.sbi.vigilance.app.view.util.NumberToWords;

import java.math.BigDecimal;

import java.util.Calendar;

import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import oracle.adf.view.rich.component.rich.fragment.RichRegion;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.layout.RichShowDetailHeader;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.util.ResetUtils;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;

public class CaseRegBean {
    public CaseRegBean() {
    }
    
    private RichRegion regionBinding;
    private RichInputText sysSerNoBinding;
    private Date maxDate;
    private Boolean folderAmountVisible = Boolean.FALSE;
    private String folderAmountWords = "";
    
    private void generateSystemSerNo(){
        String circle = (String)ADFUtil.evaluateEL("#{bindings.Circle.inputValue}");
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
    
    public void branchVC(ValueChangeEvent valueChangeEvent) {
        valueChangeEvent.getComponent().processUpdates(FacesContext.getCurrentInstance());
        generateSystemSerNo();
        AdfFacesContext.getCurrentInstance().addPartialTarget(sysSerNoBinding);
    }

    public void setRegionBinding(RichRegion regionBinding) {
        this.regionBinding = regionBinding;
    }

    public RichRegion getRegionBinding() {
        return regionBinding;
    }

    public void setSysSerNoBinding(RichInputText sysSerNoBinding) {
        this.sysSerNoBinding = sysSerNoBinding;
    }

    public RichInputText getSysSerNoBinding() {
        return sysSerNoBinding;
    }

    public void setMaxDate(Date maxDate) {
        this.maxDate = maxDate;
    }
    
    public Date getMaxDate() {
        maxDate = new Date();
        return maxDate;
    }
    
    public void sourceVC(ValueChangeEvent valueChangeEvent) {
        if (valueChangeEvent.getNewValue() != null &&
            !"".equals(valueChangeEvent.getNewValue()) &&
            valueChangeEvent.getNewValue() != valueChangeEvent.getOldValue()) {
            valueChangeEvent.getComponent().processUpdates(FacesContext.getCurrentInstance());
            System.err.println("VALUE : " + valueChangeEvent.getNewValue());
            ADFUtil.setEL("#{sessionScope.source}", valueChangeEvent.getNewValue());
            setFolderAmountVisible(Boolean.FALSE);
            regionBinding.setVisible(Boolean.TRUE);
            regionBinding.refresh(FacesContext.getCurrentInstance());
        } else {
            regionBinding.setVisible(Boolean.FALSE);
            regionBinding.refresh(FacesContext.getCurrentInstance());
        }
    }
    
    public void setFolderAmountVisible(Boolean folderAmountVisible) {
        this.folderAmountVisible = folderAmountVisible;
    }

    public Boolean getFolderAmountVisible() {
        return folderAmountVisible;
    }

    public void setFolderAmountWords(String folderAmountWords) {
        this.folderAmountWords = folderAmountWords;
    }

    public String getFolderAmountWords() {
        return folderAmountWords;
    }
    
    public void folderAmountVC(ValueChangeEvent valueChangeEvent) {
        if(valueChangeEvent.getNewValue() != null && !"".equals(valueChangeEvent.getNewValue()) && valueChangeEvent.getOldValue() != valueChangeEvent.getNewValue()){
    //            String val = (String)valueChangeEvent.getNewValue();
    //            val.replaceAll(",", "");
            BigDecimal numberVariable = (BigDecimal)valueChangeEvent.getNewValue();
            String numberInWords = NumberToWords.convertNumberToWords(numberVariable); 
            setFolderAmountWords(numberInWords + " Rupees Only.");
            setFolderAmountVisible(Boolean.TRUE);
        }
    }
    
    public String checkTaskName(){
        String taskName = (String)ADFUtil.evaluateEL("#{bindings.TaskName.inputValue}");
        if(taskName != null && "Register Complaint".equalsIgnoreCase(taskName)){
            return "dataentry";
        }
        else{
            return "other";
        }
    }

    public String submitComplaint() {
        
        
        /**Check If Source of case is selected*/
        
        String source = (String)ADFUtil.evaluateEL("#{bindings.source.inputValue}");
        if(source == null || "".equals(source)){
            ADFUtil.showFacesMessage("Select source of case", FacesMessage.SEVERITY_ERROR);
            return null;
        }
        
        /**Set Case Registration attributes to BPM */
        
        ADFUtil.setEL("#{bindings.SourceOfCaseBPM.inputValue}", ADFUtil.evaluateEL("#{bindings.source.inputValue}"));
        ADFUtil.setEL("#{bindings.SystemSerialNoBPM.inputValue}", ADFUtil.evaluateEL("#{pageFlowScope.caseRegBean.sysSerNum}"));
        ADFUtil.setEL("#{bindings.BranchCodeBPM.inputValue}", ADFUtil.evaluateEL("#{bindings.Brcd.inputValue}"));
        ADFUtil.setEL("#{bindings.BranchNameBPM.inputValue}", ADFUtil.evaluateEL("#{bindings.Brname.inputValue}"));
        ADFUtil.setEL("#{bindings.RegionBPM.inputValue}", ADFUtil.evaluateEL("#{bindings.Region.inputValue}"));
        ADFUtil.setEL("#{bindings.OfficeTypeBPM.inputValue}", ADFUtil.evaluateEL("#{bindings.AoMcro.inputValue}"));
        ADFUtil.setEL("#{bindings.NetWorkBPM.inputValue}", ADFUtil.evaluateEL("#{bindings.NetWork.inputValue}"));
        ADFUtil.setEL("#{bindings.CircleBPM.inputValue}", ADFUtil.evaluateEL("#{bindings.Circle.inputValue}"));
        ADFUtil.setEL("#{bindings.SignedBPM.inputValue}", ADFUtil.evaluateEL("#{bindings.signed.inputValue}"));
        ADFUtil.setEL("#{bindings.CCFileNumberBPM.inputValue}", ADFUtil.evaluateEL("#{bindings.ccFileNumber.inputValue}"));
        
        
        /**If Source is Complaint, then Set Complaint Attributes to BPM*/
        
        if(source != null && "Complaint".equalsIgnoreCase(source)){
            ViewObject complaintVO = ADFUtil.findIterator("ComplaintTransVOIterator").getViewObject();
            Row complaintRow = complaintVO.getCurrentRow();
            ADFUtil.setEL("#{bindings.ReceivedFromBPM.inputValue}", complaintRow.getAttribute("ReceivedFrom"));
//            ADFUtil.setEL("", val);
        }
        
        /**Invoke BPM Submit method*/
        
        ADFUtil.invokeEL("#{bindings.SUBMIT.execute}");
        
        return null;
    }
}
