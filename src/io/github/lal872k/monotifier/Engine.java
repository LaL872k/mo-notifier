/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lal872k.monotifier;

import io.github.lal872k.monotifier.PasswordSecure.Key;
import java.io.Console;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author L. Arthur Lewis II
 */
public class Engine {
    
    public static final String PASSWORD_COMPARE = "MO-Notifications is an amazing application that everyone should use.";
    
    private History history;
    private Userbase userbase;
    private final Scanner scan;
    private final ArrayList<Command> commands;
    private boolean listening = true;
    
    public Engine(){
        history = new History();
        userbase = new Userbase();
        scan = new Scanner(System.in);
        commands = new ArrayList();
        
        init();
    }
    
    private void init(){
        try {
            GDrive.connectDrive();
            GDrive.createFileIfAbsent(this);
            GMail.connectGmail();
            history = GDrive.downloadHistory();
            userbase = GDrive.downloadUserbase();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        verifyPassword();
        System.out.println("Adding commands...");
        for (Command command : Command.getAllCommands()){
            commands.add(command);
        }
        history.addAction(new Action("Startup", "Started up program."));
        loopInput();
    }
    
    public void verifyPassword(){
        if (userbase.getSalt() == null){
            System.out.println("The Userbase has not yet been set up.");
            String pass = "";
            Console console = System.console();
            while (true){
                System.out.println("Please provide a password to secure the userbase under:");
                // if using console use password
                if (console!=null){
                    pass = new String(console.readPassword());
                } else {
                    pass = scan.nextLine();
                }
                System.out.println("Please verify that you know this password by repeating it:");
                String pass2;
                if (console!=null){
                    pass2 = new String(console.readPassword());
                } else {
                    pass2 = scan.nextLine();
                }
                if (pass.equals(pass2)){
                    break;
                } else {
                    System.out.println("The passwords didn't match.");
                }
            }
            userbase.setSalt(PasswordSecure.getRandomSalt());
            try {
                PasswordSecure.initializeCipher(pass, userbase.getSalt(), Key.CURRENT_KEY);
            } catch (NoSuchAlgorithmException ex) {
                ex.printStackTrace();
            } catch (InvalidKeySpecException ex) {
                ex.printStackTrace();
            }
            userbase.setEncryptedCompare(PasswordSecure.encryptText(PASSWORD_COMPARE, Key.CURRENT_KEY));
        } else {
            while (true){
                System.out.println("Password to login to userbase:");
                String input;
                Console console = System.console();
                if (console!=null){
                    input = new String(console.readPassword());
                } else {
                    input = scan.nextLine();
                }
                try {
                    PasswordSecure.initializeCipher(input, userbase.getSalt(), Key.CURRENT_KEY);
                } catch (NoSuchAlgorithmException ex) {
                    ex.printStackTrace();
                } catch (InvalidKeySpecException ex) {
                    ex.printStackTrace();
                }
                
                if (PASSWORD_COMPARE.equals(userbase.getEncryptedCompare().getPassword())){
                    break;
                } else {
                    System.out.println("password doesn't match.");
                }
            }
        }
    }
    
    private void loopInput(){
        System.out.println("Initiation completed ready for user input.");
        while (listening){
            String command = scan.nextLine();
            handleCommand(command);
        }
    }
    
    private void handleCommand(String keyword){
        for (Command command : commands){
            if (command.getKeyword().equals(keyword)){
                history.addAction(new Action("Ran Command", "Ran the command '" + command.getKeyword() + "'."));
                command.getExecutable().execute(this, scan);
                return;
            }
        }
        System.err.println("Could not find a command that matched the keyword: " + keyword);
    }
    
    public void setListening(boolean listening){
        this.listening = listening;
    }
    
    public History getHistory(){
        return history;
    }
    
    public Userbase getUserbase(){
        return userbase;
    }
    
    public ArrayList<Command> getCommands(){
        return commands;
    }
    
    public boolean isListening(){
        return listening;
    }
    
    public static void printHelp(){
        System.out.println("Missed Obligation Notifier (MON) by L. Arthur Lewis II.");
        System.out.println("View the source code online: https://github.com/LaL872k/mo-notifier/tree/master");
        System.out.println("Type 'list_commands' for a list of commands in the console.");
    }
    
}
