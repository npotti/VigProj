package beans;


import java.util.Calendar;

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

import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.SequenceImpl;

import util.ADFUtil;

public class CaseRegBean {
    private RichInputText sysSerNoBinding;
    private RichShowDetailHeader complaintHdrBinding;
    private RichShowDetailHeader fraudHdrBinding;

    public CaseRegBean() {
    }
    
    public void checkIfExistingTask() {
        String sysSerNo =
            (String)ADFUtil.evaluateEL("#{bindings.ComplaintNumber.inputValue}");

        if (sysSerNo != null && !"".equals(sysSerNo)) {

            BindingContainer bindings =
                BindingContext.getCurrent().getCurrentBindingsEntry();
            OperationBinding method =
                bindings.getOperationBinding("setCurrentRowWithKeyValue");
            method.getParamsMap().put("rowKey", sysSerNo);
            method.execute();
            
            ViewObject caseRegVO = ADFUtil.findIterator("CaseRegistrationVOIterator").getViewObject();
            String source = (String)caseRegVO.getCurrentRow().getAttribute("Sourceofcase");
            if(source != null && "Complaint".equalsIgnoreCase(source)){
                method =
                    bindings.getOperationBinding("setComplaintRow");
                method.getParamsMap().put("rowKey", sysSerNo);
                method.execute();
                
            }
            else if(source != null && "Fraud".equalsIgnoreCase(source)){
                method =
                    bindings.getOperationBinding("setFraudRow");
                method.getParamsMap().put("rowKey", sysSerNo);
                method.execute();
            }
            
            ADFUtil.setEL("#{pageFlowScope.readOnly}", Boolean.TRUE);
        }
        else{
            ViewObject caseRegVO = ADFUtil.findIterator("CaseRegistrationVOIterator").getViewObject();
            Row caseRegRow = caseRegVO.createRow();
            caseRegVO.insertRow(caseRegRow);
            caseRegVO.setCurrentRow(caseRegRow);
            ADFUtil.setEL("#{pageFlowScope.readOnly}", Boolean.FALSE);
        }
    }

    public void branchCodeVC(ValueChangeEvent valueChangeEvent) {
        valueChangeEvent.getComponent().processUpdates(FacesContext.getCurrentInstance());
        generateSystemSerNo();
        AdfFacesContext.getCurrentInstance().addPartialTarget(sysSerNoBinding);
    }

    public void setSysSerNoBinding(RichInputText sysSerNoBinding) {
        this.sysSerNoBinding = sysSerNoBinding;
    }

    public RichInputText getSysSerNoBinding() {
        return sysSerNoBinding;
    }
    
    private void generateSystemSerNo(){
        
        FacesContext ctx = FacesContext.getCurrentInstance();

        ValueBinding vb = ctx.getCurrentInstance().getApplication().createValueBinding("#{data}");

        BindingContext bc = (BindingContext)vb.getValue(ctx.getCurrentInstance());

        DataControl dc = bc.findDataControl("VigilanceAMDataControl");

        ApplicationModuleImpl am = ((ApplicationModuleImpl)(ApplicationModule)dc.getDataProvider());
        
        SequenceImpl seqImpl = new SequenceImpl("SYS_SER_NO_SEQ", am.getDBTransaction());
        oracle.jbo.domain.Number seqNum = seqImpl.getSequenceNumber();
        
        oracle.jbo.domain.Number circle = (oracle.jbo.domain.Number)ADFUtil.evaluateEL("#{bindings.Circlecd.inputValue}");
        Integer startYear = new Integer(0);
        Integer endYear = new Integer(0);
        String uniqueNumber = seqNum+"";
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
        ADFUtil.setEL("#{bindings.Systemsernum.inputValue}", sysSerNum);
    }

    public void setComplaintHdrBinding(RichShowDetailHeader complaintHdrBinding) {
        this.complaintHdrBinding = complaintHdrBinding;
    }

    public RichShowDetailHeader getComplaintHdrBinding() {
        return complaintHdrBinding;
    }

    public void sourceVC(ValueChangeEvent valueChangeEvent) {
        if(valueChangeEvent.getNewValue() != null && valueChangeEvent.getNewValue() != valueChangeEvent.getOldValue()) {
            if((Integer)valueChangeEvent.getNewValue() == 1){
                ViewObject complaintVO = ADFUtil.findIterator("ComplaintDetailsVOIterator").getViewObject();
                Row complaintRow = complaintVO.createRow();
                complaintRow.setAttribute("Systemsernum", ADFUtil.evaluateEL("#{bindings.Systemsernum.inputValue}"));
                complaintVO.insertRow(complaintRow);
                complaintVO.setCurrentRow(complaintRow);
                complaintHdrBinding.setVisible(Boolean.TRUE);
                AdfFacesContext.getCurrentInstance().addPartialTarget(complaintHdrBinding);
            }
            else if((Integer)valueChangeEvent.getNewValue() == 2){
                ViewObject fraudVO = ADFUtil.findIterator("FraudDetailsVOIterator").getViewObject();
                Row fraudRow = fraudVO.createRow();
                fraudRow.setAttribute("Systemsernum", ADFUtil.evaluateEL("#{bindings.Systemsernum.inputValue}"));
                fraudVO.insertRow(fraudRow);
                fraudVO.setCurrentRow(fraudRow);
                fraudHdrBinding.setVisible(Boolean.TRUE);
                AdfFacesContext.getCurrentInstance().addPartialTarget(fraudHdrBinding);
            }
        }
    }

    public void setFraudHdrBinding(RichShowDetailHeader fraudHdrBinding) {
        this.fraudHdrBinding = fraudHdrBinding;
    }

    public RichShowDetailHeader getFraudHdrBinding() {
        return fraudHdrBinding;
    }
}
