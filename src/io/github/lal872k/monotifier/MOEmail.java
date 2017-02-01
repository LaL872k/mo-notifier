/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lal872k.monotifier;

import java.io.IOException;

/**
 *
 * @author L. Arthur Lewis II
 */
public class MOEmail {
    
    public static void sendEmail(User user, Engine engine){
        System.out.println("Sending email to " + user.getName() + " (" + user.getID() + ")...");
        
        String subject = "MO Report For "+user.getName();
        String message = "Hello "+user.getName()+",\n\n"
                + "Here is your new report for MO's:\n\n";
        
        if (user.hasUnsentMO()){
            message += "You have recieved " + user.getUnsentMO() + " mo's since your last mo report. Here "
                    + "are your latest mo's:\n\n";
        }
        
        for (MO mo : user.getMOs()){
            if (!mo.sentToUser()){
                message += "\t"+mo.getSection()+" on " + mo.getDate()+" for "+mo.getType()+"\n\n";
            }
            mo.setSentToUser(true);
        }
        
        message += "You currently have " + user.getCurrentMOs() + " mo's on your account."; 
        
        if (user.getCurrentMOs()>0){
            message += "The next one will be removed in around " + user.getDaysLeftTillMORemoved() + " days.";
        }
        
        message += "\n\nMyBackPack Verified: " + user.isMyBackPackVerified();

        message += "\n\nIf you have any questions or concerns please email back to MONotifications1864@gmail.com";

        try {
            GMail.sendEmail(user.getEmail(), subject, message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("Sent email to " + user.getName() + " (" + user.getID() + ")");
        
        engine.getHistory().addAction(new Action("Sent Email", "Sent email to " + user.getName() + " (" + user.getID() + ")(" + user.getEmail()+") to update the user on his currnet mo's."));
        
    }
    
}
