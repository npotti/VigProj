package com.sbi.vigilance.app.view.beans;

import com.sbi.vigilance.app.view.util.ADFUtil;

import com.sbi.vigilance.app.view.util.NumberToWords;

import java.math.BigDecimal;

import java.util.Date;

import javax.faces.event.ValueChangeEvent;

import oracle.adf.view.rich.component.rich.output.RichOutputText;

public class SourceRedirectBean {
    private RichOutputText folderWordsBinding;

    public SourceRedirectBean() {
        super();
    }
    
    private Date maxDate;
    private Boolean folderAmountVisible = Boolean.FALSE;
    private String folderAmountWords = "";
    
    public String redirectToAppropriateForm(){
        String source = (String)ADFUtil.evaluateEL("#{sessionScope.source}");
        return source;
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
    
    public void setMaxDate(Date maxDate) {
        this.maxDate = maxDate;
    }
    
    public Date getMaxDate() {
        maxDate = new Date();
        return maxDate;
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

    public void setFolderWordsBinding(RichOutputText folderWordsBinding) {
        this.folderWordsBinding = folderWordsBinding;
    }

    public RichOutputText getFolderWordsBinding() {
        return folderWordsBinding;
    }
}
