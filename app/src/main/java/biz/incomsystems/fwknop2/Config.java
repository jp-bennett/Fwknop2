package biz.incomsystems.fwknop2;

import android.util.Log;

import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.util.UUID;

public class Config {
    public String NICK_NAME;
    public String ACCESS_IP;
    public String PORTS;
    public String SERVER_IP;
    public String SERVER_PORT;
    public String SERVER_TIMEOUT;
    public String KEY;
    public Boolean KEY_BASE64;
    public String HMAC;
    public String NAT_IP;
    public String NAT_PORT;
    public String SERVER_CMD;
    public Boolean HMAC_BASE64;
    public String SSH_CMD;
    public UUID juice_uuid;
    public Boolean LEGACY;
    public String PROTOCOL;
    public String MESSAGE_TYPE;
    public String DIGEST_TYPE;
    public String HMAC_TYPE;

    public int Is_Valid(){
        InetAddressValidator ipValidate = new InetAddressValidator();

        try {
            if ((Integer.parseInt(this.SERVER_PORT) > 0) && (Integer.parseInt(this.SERVER_PORT) < 65535)) { // check for valid port
                this.SERVER_PORT = (String.valueOf(Integer.parseInt(this.SERVER_PORT))); // this double dance is to ensure we get just the number
            } else {
                this.SERVER_PORT = String.valueOf(62201);
            }
        } catch (NumberFormatException ex) {
            if (!this.SERVER_PORT.equalsIgnoreCase("random")) {
                this.SERVER_PORT = String.valueOf(62201);
            }
        }
        if (this.MESSAGE_TYPE.contains("Nat")) {
            try {
                if (Integer.parseInt(this.NAT_PORT) < 1 || Integer.parseInt(this.NAT_PORT) > 65535) {
                    return (R.string.NotValidNatPort);
                }
            } catch (NumberFormatException ex) {
                return (R.string.NotValidNatPort);
            }
        }
        if (this.NICK_NAME.equalsIgnoreCase("")) { // Need to create a new Nick
            return(R.string.unique_nick); // choosing a used nick will just overwrite it. So really
        } else if ((this.LEGACY && this.HMAC_BASE64) || (this.LEGACY && !this.HMAC.equalsIgnoreCase(""))) {
            return(R.string.HMAC_Legacy);
        } else if(this.HMAC_BASE64 && this.HMAC.length() % 4 != 0) { // base64 must have a multiple of 4 length
            return(R.string.hmac64_x4);
        } else if(this.HMAC_BASE64 && !(this.HMAC.matches("^[A-Za-z0-9+/]+={0,2}$"))) { // looks for disallowed b64 characters
            return(R.string.hmac64_xchars);
        } else if(this.KEY_BASE64 && this.KEY.length() % 4 != 0) {
            return(R.string.key64_x4);
        } else if(this.KEY_BASE64 && !(this.KEY.matches("^[A-Za-z0-9+/]+={0,2}$"))) { // looks for disallowed b64 characters
            return(R.string.key64_xchars);
        } else if (!(this.PORTS.matches("tcp/\\d.*") || this.PORTS.matches("udp/\\d.*") || this.MESSAGE_TYPE.equalsIgnoreCase("Server Command"))) {
            return(R.string.port_format);
        } else if (!(this.ACCESS_IP.equalsIgnoreCase("Allow IP") || this.ACCESS_IP.equalsIgnoreCase("Resolve IP") || this.ACCESS_IP.equalsIgnoreCase("Prompt IP") || (ipValidate.isValid(this.ACCESS_IP)))){ //Have to have a valid ip to allow, if using allow ip
            return(R.string.valid_ip);
        }  else if (!ipValidate.isValid(this.SERVER_IP) && !DomainValidator.getInstance().isValid(this.SERVER_IP)) { // check server entry. Must be a valid url or ip.
            return(R.string.valid_server);
        } else if (this.juice_uuid == null) { //This one might have to go in the main function
            return (R.string.juice_first);
        } else if (this.MESSAGE_TYPE.equalsIgnoreCase("NAT Access") && !ipValidate.isValid(this.NAT_IP)){
            return (R.string.NotValidNatIP);
        } else {
            return 0;
        }
    }
}
