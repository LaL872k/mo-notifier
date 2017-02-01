/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lal872k.monotifier;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import wagu.Block;
import wagu.Board;
import wagu.Table;

/**
 *
 * @author L. Arthur Lewis II
 */
public class Command {
    
    public static final String CANCEL_COMMAND = "\\cancel";
    
    private final String keyword;
    private final Executable exe;
    
    public Command(String keyword, Executable exe){
        this.keyword = keyword;
        this.exe = exe;
    }
    
    public String getKeyword(){
        return keyword;
    }
    
    public Executable getExecutable(){
        return exe;
    }
    
    public static Command[] getAllCommands(){
        return new Command[]{
            new Command("quit", (engine, scan) -> {
                System.out.println("Quiting...");
                System.exit(0);
            }),
            new Command("list_commands", (engine, scan) -> {
                System.out.println("All Commands:");
                for (Command com : engine.getCommands()){
                    System.out.println(" - "+com.getKeyword());
                }
            }),
            new Command("add_user", (engine, scan) -> {
                System.out.println("Type \""+CANCEL_COMMAND+"\" at any time to cancel action.\nName of user?");
                String name = scan.nextLine();
                if (name.equals(CANCEL_COMMAND)){
                    return;
                }
                
                System.out.println("Email of user?");
                String email = scan.nextLine();
                if (email.equals(CANCEL_COMMAND)){
                    return;
                }
                
                System.out.println("MyBackpack username?");
                String backpackUsername = scan.nextLine();
                if (backpackUsername.equals(CANCEL_COMMAND)){
                    return;
                }
                
                System.out.println("MyBackpack password?");
                Console console = System.console();
                String backpackPass;
                if (console != null){
                    backpackPass = new String(console.readPassword());
                } else {
                    backpackPass = scan.nextLine();
                }
                if (backpackPass.equals(CANCEL_COMMAND)){
                    return;
                }
                User newUser = new User(engine, email, name, backpackUsername, backpackPass);
                engine.getUserbase().addUser(newUser);
                System.out.println(name+" has successfully been added to the userbase with the ID:"+newUser.getID()+".");
                engine.getHistory().addAction(new Action("Added User", "User added to userbase under the name "
                        + newUser.getName() + " ("+newUser.getEmail()+")("+newUser.getID()+")"));
            }),
            new Command("list_users", (engine, scan) -> {
                System.out.println("Listing All users in userbase...");
                if (engine.getUserbase().getUsers().size()>0){
                    List<String> headersList = Arrays.asList("ID", "Name", "Email", "MyBackPack Username");
                    List<List<String>> rowsList = new ArrayList();
                    List<Integer> colAlignList = Arrays.asList(
                        Block.DATA_MIDDLE_LEFT, 
                        Block.DATA_MIDDLE_LEFT, 
                        Block.DATA_MIDDLE_LEFT, 
                        Block.DATA_MIDDLE_LEFT);
                    for (User user : engine.getUserbase().getUsers()){
                        rowsList.add(Arrays.asList(String.valueOf(user.getID()), user.getName(), user.getEmail(), user.getMyBackPackUsername()));
                    }
                    Board board = new Board(82);
                    Table table = new Table(board, 82, headersList, rowsList);
                    table.setColAlignsList(colAlignList);
                    List<Integer> colWidthsListEdited = Arrays.asList(6, 16, 30, 25);
                    table.setGridMode(Table.GRID_FULL).setColWidthsList(colWidthsListEdited);
                    String tableString = board.setInitialBlock(table.tableToBlocks()).build().getPreview();
                    System.out.println(tableString);
                } else {
                    System.out.println("No users found.");
                }
            }),
            new Command("remove_user", (engine, scan) -> {
                System.out.println("Type \""+CANCEL_COMMAND+"\" at any time to cancel action.");
                outerloop: while (true){
                    System.out.println("ID of user?");
                    String id = scan.nextLine();
                    if (id.equals(CANCEL_COMMAND)){
                        return;
                    }
                    long idSearch;
                    try{
                        idSearch = Long.parseLong(id);
                    } catch (Exception e){
                        System.out.println("The ID given was invalid.");
                        continue;
                    }
                    for (User user : engine.getUserbase().getUsers()){
                        if (user.getID()==idSearch){
                            System.out.println("Are you sure you would like to remove "+user.getName()+"("+user.getID()+") from the userbase? (y/n)");
                            String response = scan.nextLine();
                            if (response.equals("y")){
                                engine.getUserbase().removeUser(user);
                                System.out.println(user.getName()+" has been removed from the userbase.");
                                engine.getHistory().addAction(new Action("Removed User", "Removed user from userbase with name "
                                        +user.getName()+"("+user.getEmail()+")("+user.getID()+")"));
                                return;
                            } else {
                                continue outerloop;
                            }
                        }
                    }
                    System.out.println("Could not find a user with that id.");
                }
            }),
            new Command("update_mo", (engine, scan) -> {
                System.out.println("Type \""+CANCEL_COMMAND+"\" at any time to cancel action.");
                while (true){
                    System.out.println("ID of user?");
                    String rawID = scan.nextLine();
                    if (rawID.equals(CANCEL_COMMAND)){
                        return;
                    }
                    long id = 0l;
                    try{
                        id = Long.parseLong(rawID);
                    } catch (Exception e){
                        System.out.println("The ID given was invalid.");
                        continue;
                    }
                    if (engine.getUserbase().userExists(id)){
                        engine.getUserbase().getUser(id).updateMOs(engine);
                        return;
                    } else {
                        System.out.println("No user with that ID was found.");
                    }
                }
            }),
            new Command("update_mo_all_users", (engine, scan) -> {
                for (User user : engine.getUserbase().getUsers()){
                    user.updateMOs(engine);
                }
            }),
            new Command("check_mybackpack", (engine, scan) -> {
                System.out.println("Type \""+CANCEL_COMMAND+"\" at any time to cancel action.");
                while (true){
                    System.out.println("ID of user?");
                    String rawID = scan.nextLine();
                    if (rawID.equals(CANCEL_COMMAND)){
                        return;
                    }
                    long id = 0l;
                    try{
                        id = Long.parseLong(rawID);
                    } catch (Exception e){
                        System.out.println("The ID given was invalid.");
                        continue;
                    }
                    if (engine.getUserbase().userExists(id)){
                        try {
                            boolean valid = MyBackPack.hasValidCredentials(engine.getUserbase().getUser(id), engine);
                            System.out.println("Valid credentials for "+engine.getUserbase().getUser(id).getName() + ": "+valid);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        return;
                    } else {
                        System.out.println("No user with that ID was found.");
                    }
                }
            }),
            new Command("upload_userbase", (engine, scan) -> {
                try {
                    GDrive.uploadUserbase(engine.getUserbase(), engine);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }),
            new Command("upload_history", (engine, scan) -> {
                try {
                    GDrive.uploadHistory(engine.getHistory(), engine);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }),
            new Command("upload_files", (engine, scan) -> {
                try {
                    GDrive.uploadUserbase(engine.getUserbase(), engine);
                    GDrive.uploadHistory(engine.getHistory(), engine);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }),
            new Command("download_userbase", (engine, scan) -> {
                try {
                    GDrive.downloadUserbase();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }),
            new Command("download_history", (engine, scan) -> {
                try {
                    GDrive.downloadHistory();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }),
            new Command("download_files", (engine, scan) -> {
                try {
                    GDrive.downloadUserbase();
                    GDrive.downloadHistory();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }),
            new Command("delete_userbase", (engine, scan) -> {
                try {
                    GDrive.deleteUserbase(engine);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }),
            new Command("delete_history", (engine, scan) -> {
                try {
                    GDrive.deleteHistory(engine);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }),
            new Command("delete_files", (engine, scan) -> {
                try {
                    GDrive.deleteUserbase(engine);
                    GDrive.deleteHistory(engine);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }),
            new Command("get_userbase_id", (engine, scan) -> {
                System.out.println("Userbase ID: " + GDrive.getUserbaseID());
            }),
            new Command("get_history_id", (engine, scan) -> {
                System.out.println("History ID: " + GDrive.getHistoryID());
            }),
            new Command("email_user", (engine, scan) -> {
                System.out.println("Type \""+CANCEL_COMMAND+"\" at any time to cancel action.");
                while (true){
                    System.out.println("ID of user?");
                    String rawID = scan.nextLine();
                    if (rawID.equals(CANCEL_COMMAND)){
                        return;
                    }
                    long id = 0l;
                    try{
                        id = Long.parseLong(rawID);
                    } catch (Exception e){
                        System.out.println("The ID given was invalid.");
                        continue;
                    }
                    if (engine.getUserbase().userExists(id)){
                        MOEmail.sendEmail(engine.getUserbase().getUser(id), engine);
                        return;
                    } else {
                        System.out.println("No user with that ID was found.");
                    }
                }
            }),
            new Command("email_all_users", (engine, scan) -> {
                for (User user : engine.getUserbase().getUsers()){
                    MOEmail.sendEmail(user, engine);
                }
            }),
            new Command("edit_user", (engine, scan) -> {
                System.out.println("Type \""+CANCEL_COMMAND+"\" at any time to cancel action.");
                while (true){
                    System.out.println("ID of user?");
                    String rawID = scan.nextLine();
                    if (rawID.equals(CANCEL_COMMAND)){
                        return;
                    }
                    long id = 0l;
                    try{
                        id = Long.parseLong(rawID);
                    } catch (Exception e){
                        System.out.println("The ID given was invalid.");
                        continue;
                    }
                    if (engine.getUserbase().userExists(id)){
                        User user = engine.getUserbase().getUser(id);
                        String[] commands = {"\\name", "\\email", "\\mbpname", "\\mbppass"};
                        System.out.println("Edit Page for " + user.getName() + "\n" + commands[0] + 
                                " - to edit name\n" + commands[1] + " - to edit email\n" + 
                                commands[2] + " - to edit MyBackPack username\n" + commands[3] + 
                                " - to edit MyBackPack password");
                        while (true){
                            System.out.println("Command?");
                            String command = scan.nextLine();
                            if (command.equals(CANCEL_COMMAND)){
                                return;
                            }
                            if (command.equals(commands[0])){
                                System.out.println("New value for " + user.getName() + "'s name (type '"+CANCEL_COMMAND+"' to cancel):");
                                String input = scan.nextLine();
                                if (input.equals(CANCEL_COMMAND)){
                                    return;
                                }
                                user.setName(input);
                                System.out.println("Name changed.");
                                continue;
                            }
                            if (command.equals(commands[1])){
                                System.out.println("New value for " + user.getName() + "'s email (type '"+CANCEL_COMMAND+"' to cancel):");
                                String input = scan.nextLine();
                                if (input.equals(CANCEL_COMMAND)){
                                    return;
                                }
                                user.setEmail(input);
                                System.out.println("Email changed.");
                                continue;
                            }
                            if (command.equals(commands[2])){
                                System.out.println("New value for " + user.getName() + "'s MyBackPack Username (type '"+CANCEL_COMMAND+"' to cancel):");
                                String input = scan.nextLine();
                                if (input.equals(CANCEL_COMMAND)){
                                    return;
                                }
                                user.setMyBackPackUsername(input);
                                System.out.println("MyBackPack Username changed.");
                                continue;
                            }
                            if (command.equals(commands[3])){
                                System.out.println("New value for " + user.getName() + "'s MyBackPack Password (type '"+CANCEL_COMMAND+"' to cancel):");
                                Console console = System.console();
                                String input;
                                if (console != null){
                                    input = new String(console.readPassword());
                                } else {
                                    input = scan.nextLine();
                                }
                                if (input.equals(CANCEL_COMMAND)){
                                    return;
                                }
                                user.setMyBackPackPassword(input);
                                System.out.println("MyBackPack Password changed.");
                                continue;
                            }
                            System.out.println("command not recognized please use one of the commands above or '"+CANCEL_COMMAND+"' to exit.");
                        }
                    } else {
                        System.out.println("No user with that ID was found.");
                    }
                }
            }),
            new Command("list_user_mo", (engine, scan) -> {
                System.out.println("Type \""+CANCEL_COMMAND+"\" at any time to cancel action.");
                while (true){
                    System.out.println("ID of user?");
                    String rawID = scan.nextLine();
                    if (rawID.equals(CANCEL_COMMAND)){
                        return;
                    }
                    long id = 0l;
                    try{
                        id = Long.parseLong(rawID);
                    } catch (Exception e){
                        System.out.println("The ID given was invalid.");
                        continue;
                    }
                    if (engine.getUserbase().userExists(id)){
                        User user = engine.getUserbase().getUser(id);
                        // print list
                        List<String> headersList = Arrays.asList("Date", "Section", "Type", "Sent to User");
                        List<List<String>> rowsList = new ArrayList();
                        List<Integer> colAlignList = Arrays.asList(
                            Block.DATA_MIDDLE_LEFT, 
                            Block.DATA_MIDDLE_LEFT, 
                            Block.DATA_MIDDLE_LEFT, 
                            Block.DATA_MIDDLE_LEFT);
                        for (MO mo : user.getMOs()){
                            rowsList.add(Arrays.asList(mo.getDate(), mo.getSection(), mo.getType(), String.valueOf(mo.sentToUser())));
                        }
                        if (rowsList.isEmpty()){
                            System.out.println(user.getName()+" ("+user.getID()+") has no mo's.");
                            return;
                        }
                        Board board = new Board(82);
                        Table table = new Table(board, 82, headersList, rowsList);
                        table.setColAlignsList(colAlignList);
                        List<Integer> colWidthsListEdited = Arrays.asList(10, 43, 12, 12);
                        table.setGridMode(Table.GRID_FULL).setColWidthsList(colWidthsListEdited);
                        String tableString = board.setInitialBlock(table.tableToBlocks()).build().getPreview();
                        System.out.println(tableString);
                        return;
                    } else {
                        System.out.println("No user with that ID was found.");
                    }
                }
            }),
            new Command("set_all_mo_sent", (engine, scan) -> {
                System.out.println("Type \""+CANCEL_COMMAND+"\" at any time to cancel action.");
                while (true){
                    System.out.println("ID of user?");
                    String rawID = scan.nextLine();
                    if (rawID.equals(CANCEL_COMMAND)){
                        return;
                    }
                    long id = 0l;
                    try{
                        id = Long.parseLong(rawID);
                    } catch (Exception e){
                        System.out.println("The ID given was invalid.");
                        continue;
                    }
                    if (engine.getUserbase().userExists(id)){
                        for (MO mo : engine.getUserbase().getUser(id).getMOs()){
                            mo.setSentToUser(true);
                        }
                        return;
                    } else {
                        System.out.println("No user with that ID was found.");
                    }
                }
            }),
            new Command("change_password", (engine, scan) -> {
                System.out.println("Type \""+CANCEL_COMMAND+"\" at any time to cancel action.");
                Console console = System.console();
                while (true){
                    System.out.println("Old Password:");
                    String oldPass;
                    if (console != null){
                        oldPass = new String(console.readPassword());
                    } else {
                        oldPass = scan.nextLine();
                    }
                    if (oldPass.equals(CANCEL_COMMAND)){
                        return;
                    }
                    try {
                        PasswordSecure.initializeCipher(oldPass, engine.getUserbase().getSalt(), PasswordSecure.Key.TEMP_KEY);
                        if (Engine.PASSWORD_COMPARE.equals(PasswordSecure.decryptText(engine.getUserbase().getEncryptedCompare(), PasswordSecure.Key.TEMP_KEY))){
                            while (true){
                                // old password successfully given
                                System.out.println("New Password:");
                                String newPass;
                                if (console != null){
                                    newPass = new String(console.readPassword());
                                } else {
                                    newPass = scan.nextLine();
                                }
                                if (newPass.equals(CANCEL_COMMAND)){
                                    return;
                                }
                                System.out.println("Retype new password:");
                                String newPass2;
                                if (console != null){
                                    newPass2 = new String(console.readPassword());
                                } else {
                                    newPass2 = scan.nextLine();
                                }
                                if (newPass2.equals(CANCEL_COMMAND)){
                                    return;
                                }
                                if (newPass.equals(newPass2)){
                                    PasswordSecure.moveKey(PasswordSecure.Key.CURRENT_KEY, PasswordSecure.Key.OLD_KEY);
                                    PasswordSecure.initializeCipher(newPass, engine.getUserbase().getSalt(), PasswordSecure.Key.CURRENT_KEY);
                                    for (User user : engine.getUserbase().getUsers()){
                                        user.getMyBackPackPassword().changePasswordToCurrentKey();
                                    }
                                    engine.getUserbase().getEncryptedCompare().changePasswordToCurrentKey();
                                    System.out.println("Password changed");
                                    engine.getHistory().addAction(new Action("Password Change", "The Password to login to the userbase was changed."));
                                    return;
                                } else {
                                    System.out.println("The new passwords didn't match.");
                                }
                            }
                        } else {
                            System.out.println("password given does not match.");
                        }
                    } catch (NoSuchAlgorithmException ex) {
                        ex.printStackTrace();
                    } catch (InvalidKeySpecException ex) {
                        ex.printStackTrace();
                    }
                }
            }),
            new Command("list_history", (engine, scan) -> {
                System.out.println("Type \""+CANCEL_COMMAND+"\" at any time to cancel action.");
                List<Action> actions = engine.getHistory().getActions().subList(0, engine.getHistory().getActions().size()-1);
                Collections.sort(actions, new Action.DateComparator());
                
                int pageLength = 30;
                int pages = (int) Math.ceil(actions.size() / ((double)pageLength));
                
                int page = 1;
                while (true){
                    System.out.println("Viewing page "+page+"/"+pages+" of the history...");
                    for (int q = (page - 1) * pageLength; q < page * pageLength && q < actions.size(); q++){
                        Action action = actions.get(q);
                        System.out.println("[" + action.getTimeStamp() + "] " + action.getTitle()
                                + " - " + action.getDescription());
                    }
                    while (true){
                        System.out.println("Page number?");
                        String input = scan.nextLine();
                        if (input.equals(CANCEL_COMMAND)){
                            return;
                        }
                        try {
                            Integer.parseInt(input);
                        } catch (NumberFormatException e){
                            System.out.println("Didn't recognize input as a number.");
                            continue;
                        }
                        int newPage = Integer.parseInt(input);
                        if (newPage<1 || newPage>pages){
                            System.out.println("Page number outside of range please select a value from 1-" + pages);
                            continue;
                        }
                        page = newPage;
                        break;
                    }
                }
            }),
            new Command("download_history_to_file", (engine, scan) -> {
                File file = new File(GDrive.getJarPath()+"/monotifications-history.log");
                try {
                    if (!file.exists()){
                        file.createNewFile();
                    }
                    List<String> lines = new ArrayList();
                    List<Action> actions = engine.getHistory().getActions().subList(0, engine.getHistory().getActions().size()-1);
                    Collections.sort(actions, new Action.DateComparator());
                    for (Action action : actions){
                        lines.add("[" + action.getTimeStamp() + "] " + action.getTitle() + " - " 
                                + action.getDescription());
                    }
                    Files.write(file.toPath(), lines, Charset.forName("UTF-8"));
                    System.out.println("A copy of the history has been saved to: " + file.getPath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }),
            new Command("help", (engine, scan) -> {
                Engine.printHelp();
            })
        };
    }
    
    /*
    new Command("", (engine, scan) -> {
                
            })
    */
    
}
