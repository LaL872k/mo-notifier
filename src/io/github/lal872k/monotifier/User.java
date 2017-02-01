/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lal872k.monotifier;

import io.github.lal872k.monotifier.MO.DateComparator;
import io.github.lal872k.monotifier.PasswordSecure.Key;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author L. Arthur Lewis II
 */
public final class User {
    
    private long id;
    
    private String email;
    
    private String name;
    
    private String mybackpackUsername;
    
    private Password mybackpackPassword;
    
    @JsonIgnore
    private transient boolean emailVerified = false;
    
    private boolean mybackpackVerified = false;
    
    private List<MO> mos;
    
    public User(){}
    
    public User(Engine engine){
        id = engine.getUserbase().getLargestID()+1l;
        mos = new ArrayList();
    }
    
    public User(Engine engine, String email, String name, String mybackpackUsername, String mybackpackPassword){
        id = engine.getUserbase().getLargestID()+1l;
        this.email = email;
        this.name = name;
        this.mybackpackUsername = mybackpackUsername;
        setMyBackPackPassword(mybackpackPassword);
        mos = new ArrayList();
    }
    
    public void updateMOs(Engine engine){
        try {
            if (!MyBackPack.hasValidCredentials(this, engine)){
                System.out.println("Invalid credentials for " + name + " (" + id + ") couldn't login to MyBackPack.");
                mybackpackVerified = false;
                return;
            } else {
                mybackpackVerified = true;
            }
            
            MO[] newMOs = MyBackPack.loadMOs(this, engine);
            
            System.out.println("MO's retrieved from MyBackPack. Now updating local version.");
            for (int q = 0; q < newMOs.length; q++){
                boolean duplicate = false;
                for (MO currentMO : mos){
                    if (newMOs[q].equals(currentMO)){
                        duplicate = true;
                    }
                }
                if (!duplicate){
                    mos.add(newMOs[q]);
                }
            }
            System.out.println("Local version updated with latest MO's.");
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    public void setEmail(String email){
        this.email = email;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public void setMyBackPackUsername(String mybackpackUsername){
        this.mybackpackUsername = mybackpackUsername;
    }
    
    @JsonProperty
    public void setMyBackPackPassword(Password mybackpackPassword){
        this.mybackpackPassword = mybackpackPassword;
    }
    
    @JsonIgnore
    public void setMyBackPackPassword(String password){
        mybackpackPassword = PasswordSecure.encryptText(password, Key.CURRENT_KEY);
    }
    
    public void setEmailVerified(boolean emailVerified){
        this.emailVerified = emailVerified;
    }
    
    public void setMOs(List<MO> mos){
        this.mos = mos;
    }
    
    public void setID(long id){
        this.id = id;
    }
    
    public void addMO(MO mo){
        mos.add(mo);
    }
    
    public void removeMO(MO mo){
        mos.remove(mo);
    }
    
    public void setMyBackPackVerified(boolean mybackpackVerified){
        this.mybackpackVerified = mybackpackVerified;
    }
    
    public String getEmail(){
        return email;
    }
    
    public String getName(){
        return name;
    }
    
    public String getMyBackPackUsername(){
        return mybackpackUsername;
    }
    
    @JsonProperty
    public Password getMyBackPackPassword(){
        return mybackpackPassword;
    }
    
    @JsonIgnore
    public String getDecryptedMyBackPackPassword(){
        return PasswordSecure.decryptText(mybackpackPassword, Key.CURRENT_KEY);
    }
    
    public boolean isEmailVerified(){
        return emailVerified;
    }
    
    public long getID(){
        return id;
    }
    
    public List<MO> getMOs(){
        return mos;
    }
    
    public boolean isMyBackPackVerified(){
        return mybackpackVerified;
    }
    
    @JsonIgnore
    public boolean hasUnsentMO(){
        for (MO mo : mos){
            if (!mo.sentToUser()){
                return true;
            }
        }
        return false;
    }
    
    @JsonIgnore
    public int getUnsentMO(){
        int unsentMO = 0;
        for (MO mo : mos){
            if (!mo.sentToUser()){
                unsentMO++;
            }
        }
        return unsentMO;
    }
    
    @JsonIgnore
    public int getCurrentMOs(){
        int moNum = 0;
        for (MO mo : mos){
            if (mo.getType().equals(MO.MO_TYPE)){
                moNum++;
            }
        }
        return moNum;
    }
    
    @JsonIgnore
    public int getDaysLeftTillMORemoved() {
        try {
            List<MO> sortedMO = mos.subList(0, mos.size());
            Collections.sort(sortedMO, new DateComparator());
            String lastMO = sortedMO.get(sortedMO.size()-1).getDate();
            
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            Date today = df.parse(df.format(new Date()));
            
            Date mo = df.parse(lastMO);
            
            long diff = mo.getTime() - today.getTime();
            
            // convert diff to days
            
            diff *= 1000 * 60 * 60 * 24;
            
            if (diff < 0){
                diff = 0;
            }
            
            return (int) diff;
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return 0;
    }
    
}
