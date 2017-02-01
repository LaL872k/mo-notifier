/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lal872k.monotifier;

import io.github.lal872k.monotifier.PasswordSecure.Key;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author L. Arthur Lewis II
 */
public class Password {
    
    private byte[] iv, cipherpass;
    
    public Password(){}
    
    public Password(byte[] iv, byte[] cipherpass){
        this.iv = iv;
        this.cipherpass = cipherpass;
    }
    
    @JsonIgnore
    public void changePasswordToCurrentKey(){
        String pass = PasswordSecure.decryptText(this, Key.OLD_KEY);
        Password newpass = PasswordSecure.encryptText(pass, Key.CURRENT_KEY);
        setIV(newpass.getIV());
        setCipherPassword(newpass.getCipherPassword());
    }
    
    public void setIV(byte[] iv){
        this.iv = iv;
    }
    
    public void setCipherPassword(byte[] cipherpass){
        this.cipherpass = cipherpass;
    }
    
    public byte[] getIV(){
        return iv;
    }
    
    public byte[] getCipherPassword(){
        return cipherpass;
    }
    
    @JsonIgnore
    public String getPassword(){
        return PasswordSecure.decryptText(this, Key.CURRENT_KEY);
    }
    
}
