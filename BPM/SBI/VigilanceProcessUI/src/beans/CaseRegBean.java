package beans;


import java.util.Calendar;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.ValueChangeEvent;

import oracle.adf.model.BindingContext;


import oracle.adf.model.DataControl;
import oracle.adf.view.rich.component.rich.input.RichInputText;

import oracle.adf.view.rich.component.rich.layout.RichShowDetailHeader;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.binding.BindingContainer;

import oracle.binding.OperationBinding;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import oracle.jbo.domain.Number;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.SequenceImpl;

import util.ADFUtil;

public class CaseRegBean {
    private RichInputText sysSerNoBinding;
    private RichShowDetailHeader complaintHdrBinding;
    private RichShowDetailHeader fraudHdrBinding;
    private RichShowDetailHeader saeHdrBinding;
    private RichShowDetailHeader rfiaHdrBinding;

    public CaseRegBean() {
    }

    /**
     * Method gets called on the load of the page.
     * Shows data if existing task or blank if its new task.
     */
    public void checkIfExistingTask() {
        
        /**If complaint number exists, then its an existing task.*/
        
        String sysSerNo =
            (String)ADFUtil.evaluateEL("#{bindings.ComplaintNumber.inputValue}");

        if (sysSerNo != null && !"".equals(sysSerNo)) {
        
            /**It is existing task, fetch the respective details from DB.*/

            BindingContainer bindings =
                BindingContext.getCurrent().getCurrentBindingsEntry();
            OperationBinding method =
                bindings.getOperationBinding("setCurrentRowWithKeyValue");
            method.getParamsMap().put("rowKey", sysSerNo);
            method.execute();

            ViewObject caseRegVO =
                ADFUtil.findIterator("CaseRegistrationVOIterator").getViewObject();
            String source =
                (String)caseRegVO.getCurrentRow().getAttribute("Sourceofcase");
            if (source != null && "Complaint".equalsIgnoreCase(source)) {
                method = bindings.getOperationBinding("setComplaintRow");
                method.getParamsMap().put("rowKey", sysSerNo);
                method.execute();

            } else if (source != null && "Fraud".equalsIgnoreCase(source)) {
                method = bindings.getOperationBinding("setFraudRow");
                method.getParamsMap().put("rowKey", sysSerNo);
                method.execute();
            } else if (source != null && "SAE".equalsIgnoreCase(source)) {
                method = bindings.getOperationBinding("setSAERow");
                method.getParamsMap().put("rowKey", sysSerNo);
                method.execute();
            } else if (source != null && "RFIA".equalsIgnoreCase(source)) {
                method = bindings.getOperationBinding("setRFIARow");
                method.getParamsMap().put("rowKey", sysSerNo);
                method.execute();
            }
            ADFUtil.setEL("#{pageFlowScope.readOnly}", Boolean.TRUE);
        } else {
            
            /**It is new task, create a blank form*/
            
            ViewObject caseRegVO =
                ADFUtil.findIterator("CaseRegistrationVOIterator").getViewObject();
            Row caseRegRow = caseRegVO.createRow();
            caseRegVO.insertRow(caseRegRow);
            caseRegVO.setCurrentRow(caseRegRow);
            ADFUtil.setEL("#{pageFlowScope.readOnly}", Boolean.FALSE);
        }
    }

    /**
     * Generate Complaint number/System Serial number when branch is selected (based on the circle of the branch)
     * @param valueChangeEvent
     */
    public void branchCodeVC(ValueChangeEvent valueChangeEvent) {
        valueChangeEvent.getComponent().processUpdates(FacesContext.getCurrentInstance());
        /**Generate Ser num*/
        generateSystemSerNo();
        AdfFacesContext.getCurrentInstance().addPartialTarget(sysSerNoBinding);
    }

    public void setSysSerNoBinding(RichInputText sysSerNoBinding) {
        this.sysSerNoBinding = sysSerNoBinding;
    }

    public RichInputText getSysSerNoBinding() {
        return sysSerNoBinding;
    }

    /**
     * Generate System Serial number in the format aa:bbbb:cccc:dddd
     * aa - circle code
     * bbbb - Begin financial year
     * cccc - End financial year
     * dddd - unique number
     */
    private void generateSystemSerNo() {

        FacesContext ctx = FacesContext.getCurrentInstance();

        ValueBinding vb =
            ctx.getCurrentInstance().getApplication().createValueBinding("#{data}");

        BindingContext bc =
            (BindingContext)vb.getValue(ctx.getCurrentInstance());

        DataControl dc = bc.findDataControl("VigilanceAMDataControl");

        ApplicationModuleImpl am =
            ((ApplicationModuleImpl)(ApplicationModule)dc.getDataProvider());

        SequenceImpl seqImpl =
            new SequenceImpl("SYS_SER_NO_SEQ", am.getDBTransaction());
        oracle.jbo.domain.Number seqNum = seqImpl.getSequenceNumber();

        oracle.jbo.domain.Number circle =
            (oracle.jbo.domain.Number)ADFUtil.evaluateEL("#{bindings.Circlecd.inputValue}");
        Integer startYear = new Integer(0);
        Integer endYear = new Integer(0);
        String uniqueNumber = seqNum + "";
        Calendar cal = Calendar.getInstance();
        Integer month = cal.get(Calendar.MONTH);
        if (month > 3) {
            startYear = cal.get(Calendar.YEAR);
            endYear = cal.get(Calendar.YEAR) + 1;
        } else {
            startYear = cal.get(Calendar.YEAR) - 1;
            endYear = cal.get(Calendar.YEAR);
        }
        String sysSerNum =
            circle + ":" + startYear + ":" + endYear + ":" + uniqueNumber;
        ADFUtil.setEL("#{bindings.Systemsernum.inputValue}", sysSerNum);
    }

    public void setComplaintHdrBinding(RichShowDetailHeader complaintHdrBinding) {
        this.complaintHdrBinding = complaintHdrBinding;
    }

    public RichShowDetailHeader getComplaintHdrBinding() {
        return complaintHdrBinding;
    }


    /**
     * Render related form specific to the source selected.
     * 
     * @param valueChangeEvent
     */
    public void sourceVC(ValueChangeEvent valueChangeEvent) {
        oracle.jbo.domain.Number branchCode =
            (Number)ADFUtil.evaluateEL("#{bindings.Branchcode.inputValue}");
        if (branchCode == null) {
            ADFUtil.showFacesMessage("Please select Branch.",
                                     FacesMessage.SEVERITY_ERROR);
            return;
        }
        if (valueChangeEvent.getNewValue() != null &&
            valueChangeEvent.getNewValue() != valueChangeEvent.getOldValue()) {
            
            /**If complaint is selected*/
            
            if ((Integer)valueChangeEvent.getNewValue() == 1) {
                closeAllHeaders();
                ADFUtil.setEL("#{viewScope.complaintRefresh}", Boolean.TRUE);
                ViewObject complaintVO =
                    ADFUtil.findIterator("ComplaintDetailsVOIterator").getViewObject();
                Row complaintRow = complaintVO.createRow();
                complaintRow.setAttribute("Systemsernum",
                                          ADFUtil.evaluateEL("#{bindings.Systemsernum.inputValue}"));
                complaintVO.insertRow(complaintRow);
                complaintVO.setCurrentRow(complaintRow);
                complaintHdrBinding.setVisible(Boolean.TRUE);
                AdfFacesContext.getCurrentInstance().addPartialTarget(complaintHdrBinding);
            } 
            
            /**If fraud is selected*/
            
            else if ((Integer)valueChangeEvent.getNewValue() == 2) {
                closeAllHeaders();
                ADFUtil.setEL("#{viewScope.fraudRefresh}", Boolean.TRUE);
                ViewObject fraudVO =
                    ADFUtil.findIterator("FraudDetailsVOIterator").getViewObject();
                Row fraudRow = fraudVO.createRow();
                fraudRow.setAttribute("Systemsernum",
                                      ADFUtil.evaluateEL("#{bindings.Systemsernum.inputValue}"));
                fraudVO.insertRow(fraudRow);
                fraudVO.setCurrentRow(fraudRow);
                fraudHdrBinding.setVisible(Boolean.TRUE);
                AdfFacesContext.getCurrentInstance().addPartialTarget(fraudHdrBinding);
            } 
            
            /**If SAE is selected*/
            
            else if ((Integer)valueChangeEvent.getNewValue() == 3) {
                closeAllHeaders();
                ADFUtil.setEL("#{viewScope.saeRefresh}", Boolean.TRUE);
                ViewObject saeVO =
                    ADFUtil.findIterator("StaffAccountabilityDetailsVOIterator").getViewObject();
                Row saeRow = saeVO.createRow();
                saeRow.setAttribute("Systemsernum",
                                    ADFUtil.evaluateEL("#{bindings.Systemsernum.inputValue}"));
                saeVO.insertRow(saeRow);
                saeVO.setCurrentRow(saeRow);
                saeHdrBinding.setVisible(Boolean.TRUE);
                AdfFacesContext.getCurrentInstance().addPartialTarget(saeHdrBinding);
            } 
            
            /**If RFIA is selected*/
            
            else if ((Integer)valueChangeEvent.getNewValue() == 4) {
                closeAllHeaders();
                ADFUtil.setEL("#{viewScope.rfiaRefresh}", Boolean.TRUE);
                ViewObject rfiaVO =
                    ADFUtil.findIterator("RiskFocussedDetailsVOIterator").getViewObject();
                Row rfiaRow = rfiaVO.createRow();
                rfiaRow.setAttribute("Systemsernum",
                                     ADFUtil.evaluateEL("#{bindings.Systemsernum.inputValue}"));
                rfiaVO.insertRow(rfiaRow);
                rfiaVO.setCurrentRow(rfiaRow);
                rfiaHdrBinding.setVisible(Boolean.TRUE);
                AdfFacesContext.getCurrentInstance().addPartialTarget(rfiaHdrBinding);
            }
        }
    }

    public void setFraudHdrBinding(RichShowDetailHeader fraudHdrBinding) {
        this.fraudHdrBinding = fraudHdrBinding;
    }

    public RichShowDetailHeader getFraudHdrBinding() {
        return fraudHdrBinding;
    }

    public void setSaeHdrBinding(RichShowDetailHeader saeHdrBinding) {
        this.saeHdrBinding = saeHdrBinding;
    }

    public RichShowDetailHeader getSaeHdrBinding() {
        return saeHdrBinding;
    }

    /**
     * Change source from complaint to fraud, fraud to RFIA ... etc (from one to another)
     * Clear all fields and rollback changes from DB
     */
    private void closeAllHeaders() {
        ViewObject complaintVO =
            ADFUtil.findIterator("ComplaintDetailsVOIterator").getViewObject();
        Row row = complaintVO.getCurrentRow();
        if (row != null){
            row.refresh(Row.REFRESH_UNDO_CHANGES |
                        Row.REFRESH_WITH_DB_FORGET_CHANGES);
            row.refresh(Row.REFRESH_REMOVE_NEW_ROWS);
        }
        ViewObject fraudVO =
            ADFUtil.findIterator("FraudDetailsVOIterator").getViewObject();
        row = fraudVO.getCurrentRow();
        if (row != null){
            row.refresh(Row.REFRESH_UNDO_CHANGES |
                        Row.REFRESH_WITH_DB_FORGET_CHANGES);
            row.refresh(Row.REFRESH_REMOVE_NEW_ROWS);
        }
        ViewObject saeVO =
            ADFUtil.findIterator("StaffAccountabilityDetailsVOIterator").getViewObject();
        row = saeVO.getCurrentRow();
        if (row != null){
            row.refresh(Row.REFRESH_UNDO_CHANGES |
                        Row.REFRESH_WITH_DB_FORGET_CHANGES);
            row.refresh(Row.REFRESH_REMOVE_NEW_ROWS);
        }
        ViewObject rfiaVO =
            ADFUtil.findIterator("RiskFocussedDetailsVOIterator").getViewObject();
        row = rfiaVO.getCurrentRow();
        if (row != null){
            row.refresh(Row.REFRESH_UNDO_CHANGES |
                        Row.REFRESH_WITH_DB_FORGET_CHANGES);
            row.refresh(Row.REFRESH_REMOVE_NEW_ROWS);
        }
        complaintHdrBinding.setVisible(Boolean.FALSE);
        fraudHdrBinding.setVisible(Boolean.FALSE);
        saeHdrBinding.setVisible(Boolean.FALSE);
        rfiaHdrBinding.setVisible(Boolean.FALSE);
        AdfFacesContext.getCurrentInstance().addPartialTarget(complaintHdrBinding);
        AdfFacesContext.getCurrentInstance().addPartialTarget(fraudHdrBinding);
        AdfFacesContext.getCurrentInstance().addPartialTarget(saeHdrBinding);
        AdfFacesContext.getCurrentInstance().addPartialTarget(rfiaHdrBinding);
    }

    public void setRfiaHdrBinding(RichShowDetailHeader rfiaHdrBinding) {
        this.rfiaHdrBinding = rfiaHdrBinding;
    }

    public RichShowDetailHeader getRfiaHdrBinding() {
        return rfiaHdrBinding;
    }
}
