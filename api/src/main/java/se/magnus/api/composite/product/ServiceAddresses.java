package se.magnus.api.composite.product;

public class ServiceAddresses {
    private final String cmp;
    private final String pro;
    private final String rev;
    private final String rec;

    public ServiceAddresses(){
        cmp = null;
        pro = null;
        rev = null;
        rec = null;
    }
    
    public ServiceAddresses(String compositeAddress,String productAddress, String serviceAddress, String recommendationAddress){
        this.cmp = compositeAddress;
        this.pro = productAddress;
        this.rev = serviceAddress;
        this.rec = recommendationAddress;
    }   
    public String getCmp(){
        return this.cmp;
    }
    public String getPro(){
        return this.pro;
    }
    public String getRev(){
        return this.rev;
    }
    public String getRec(){
        return this.rec;
    }
}
