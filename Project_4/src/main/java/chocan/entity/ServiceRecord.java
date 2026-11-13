package chocan.entity;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
/** @author Dylan Filmer */
public class ServiceRecord implements java.io.Serializable {
    private static final long serialVersionUID=1L;
    private final Date recordedDateTime,serviceDate;
    private final String providerNumber,memberNumber,serviceCode,comments;
    private final BigDecimal feeToPay;
    // Receipt time and the supplied service date remain separate fields.
    public ServiceRecord(Date date,String provider,String member,String code,double fee,String comments) {
        this(new Date(),date,provider,member,code,BigDecimal.valueOf(fee),comments);
    }
    public ServiceRecord(Date received,Date date,String provider,String member,String code,BigDecimal fee,String comments) {
        if(received==null||date==null)throw new IllegalArgumentException("Dates required");
        if(provider==null||!provider.matches("[0-9]{9}")||member==null||!member.matches("[0-9]{9}")||code==null||!code.matches("[0-9]{6}"))throw new IllegalArgumentException("Invalid record identifiers");
        if(fee==null||fee.signum()<0||fee.compareTo(new BigDecimal("999.99"))>0||fee.stripTrailingZeros().scale()>2)throw new IllegalArgumentException("Invalid fee");
        String text=comments==null?"":comments;
        if(text.length()>100||text.chars().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Comments must contain at most 100 printable characters");
        recordedDateTime=new Date(received.getTime());
        serviceDate=new Date(date.getTime());
        providerNumber=provider;
        memberNumber=member;
        serviceCode=code;
        feeToPay=fee.setScale(2);
        this.comments=text;
    }
    public Date getRecordedDateTime() {
        return new Date(recordedDateTime.getTime());
    }
    // A defensive copy prevents callers from changing the stored date.
    public Date getServiceDate() {
        return new Date(serviceDate.getTime());
    }
    public String getProviderNumber() {
        return providerNumber;
    }
    public String getMemberNumber() {
        return memberNumber;
    }
    public String getServiceCode() {
        return serviceCode;
    }
    public double getFeeToPay() {
        return feeToPay.doubleValue();
    }
    public BigDecimal getFeeAmount() {
        return feeToPay;
    }
    public String getComments() {
        return comments;
    }
    public String getFormattedRecordedTime() {
        return new java.text.SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(recordedDateTime);
    }
    public String getFormattedServiceDate() {
        return new java.text.SimpleDateFormat("MM-dd-yyyy").format(serviceDate);
    }
    // The accepted service is appended to the local service-record file.
    public boolean save(java.nio.file.Path file) {
        try {
            java.nio.file.Files.writeString(file,getFormattedRecordedTime()+" | "+getFormattedServiceDate()+" | "+providerNumber+" | "+memberNumber+" | "+serviceCode+" | "+feeToPay+" | "+comments+System.lineSeparator(),java.nio.file.StandardOpenOption.CREATE,java.nio.file.StandardOpenOption.APPEND);
            return true;
        }
        catch(java.io.IOException|SecurityException e) {
            return false;
        }
    }
}
