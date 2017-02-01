/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lal872k.monotifier;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author L. Arthur Lewis II
 */
public class Userbase {
    
    private byte[] salt;
    private Password encryptedCompare;
    private List<User> users;
    
    public Userbase(){
        users = new ArrayList();
    }
    
    public void addUser(User user){
        users.add(user);
    }
    
    public void removeUser(User user){
        users.remove(user);
    }
    
    public void setUsers(List<User> users){
        this.users = users;
    }
    
    public User getUser(long id){
        synchronized (users){
            for (User user : users){
                if (user.getID() == id){
                    return user;
                }
            }
            return null;
        }
    }
    
    public boolean userExists(long id){
        return getUser(id)!=null;
    }
    
    public void setSalt(byte[] salt){
        this.salt = salt;
    }
    
    public void setEncryptedCompare(Password encryptedCompare){
        this.encryptedCompare = encryptedCompare;
    }
    
    public List<User> getUsers(){
        return users;
    }
    
    @JsonIgnore
    public long getLargestID(){
        long max = 0;
        for (User user : users){
            if (user.getID()>max){
                max = user.getID();
            }
        }
        return max;
    }
    
    public byte[] getSalt(){
        return salt;
    }
    
    public Password getEncryptedCompare(){
        return encryptedCompare;
    }
    
}
