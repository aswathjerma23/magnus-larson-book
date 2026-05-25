package util.http;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class ServiceUtil {
    private final String port;
    private String serviceAddress = null;

    @Autowired
    public ServiceUtil(@Value("${server.port}") String port){
        this.port = port;
    }
    public String getServiceAddress(){
        if(serviceAddress == null){
            serviceAddress = findMyHostName() + "/" + findMyIpAddress() + ":" + port;
        }
        return serviceAddress;
    }
    public String findMyHostName(){
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown Host";
        }
    }

    public String findMyIpAddress(){
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "Unknown Ip address";
        }
    }
}
