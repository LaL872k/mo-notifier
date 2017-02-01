/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lal872k.monotifier;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author L. Arthur Lewis II
 */
public class GDrive {
      /** Application name. */
    private static final String APPLICATION_NAME =
        "MO Notifications";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
        "D:/MyProfile/Desktop/.credentials/drive-java-quickstart");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/drive-java-quickstart
     */
    private static final List<String> SCOPES =
        Arrays.asList(DriveScopes.DRIVE_FILE);
    
    private static Drive drive;
    
    private final static String USERBASE_FILE_NAME = "userbase.json";
    private final static String HISTORY_FILE_NAME = "history.json";
    
    private static String userbaseID;
    private static String historyID;
    
    private static String JAR_PATH;
    
    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
            JAR_PATH = new java.io.File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    private static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
            GDrive.class.getResourceAsStream("client_secret.json");
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Drive client service.
     * @return an authorized Drive client service
     * @throws IOException
     */
    private static Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    
    public static void connectDrive() throws IOException {
        System.out.println("Connecting to Google Drive...");
        drive = getDriveService();
    }
    
    public static void createFileIfAbsent(Engine engine) throws IOException{
        System.out.println("Checking for userbase file: " + USERBASE_FILE_NAME);
        FileList list = drive.files().list().setQ("name='" + USERBASE_FILE_NAME + "'").execute();
        
        
        if (list.getFiles().isEmpty()){ // empty
            System.out.println("Userbase file not found. Creating a new one now.");
            File userbase = new File().setName(USERBASE_FILE_NAME);
            File file = drive.files().create(userbase).execute();
            engine.getHistory().addAction(new Action("Created File on Drive", "Created the file " + USERBASE_FILE_NAME + " on the google drive."));
            userbaseID = file.getId();
            System.out.println("Userbase file created.");
        } else { // already made
            System.out.println("Userbase file found.");
            userbaseID = list.getFiles().get(0).getId();
        }
        
        System.out.println("Checking for history file: " + HISTORY_FILE_NAME);
        list = drive.files().list().setQ("name='" + HISTORY_FILE_NAME + "'").execute();
        
        if (list.getFiles().isEmpty()){ // empty
            System.out.println("History file not found. Creating a new one now.");
            File userbase = new File().setName(HISTORY_FILE_NAME);
            engine.getHistory().addAction(new Action("Created File on Drive", "Created the file " + HISTORY_FILE_NAME + " on the google drive."));
            historyID = drive.files().create(userbase).execute().getId();
            System.out.println("History file created.");
        } else { // already made
            System.out.println("History file found.");
            historyID = list.getFiles().get(0).getId();
        }
    }
    
    public static Userbase downloadUserbase() throws IOException{
        System.out.println("Downloading and loading userbase.json");
        
        HttpResponse resp = drive.files().get(userbaseID).executeMedia();
        
        ObjectMapper obj = new ObjectMapper();
        
        String text = convertStreamToString(resp.getContent());
        
        if ("".equals(text)){
            System.out.println("userbase.json is empty.");
            return new Userbase();
        }
        
        return obj.readValue(text, Userbase.class);
    }
    
    public static History downloadHistory() throws IOException {
        System.out.println("Downloading and loading history.json");
        
        HttpResponse resp = drive.files().get(historyID).executeMedia();
        
        ObjectMapper obj = new ObjectMapper();
        
        String text = convertStreamToString(resp.getContent());
        
        if ("".equals(text)){
            System.out.println("history.json is empty.");
            return new History();
        }
        
        return obj.readValue(text, History.class);
    }
    
    public static void uploadUserbase(Userbase userbase, Engine engine) throws IOException{
        System.out.println("Saving userbase.json");
        java.io.File file = new java.io.File(JAR_PATH+"/userbase.json");
        ObjectMapper obj = new ObjectMapper();
        obj.writeValue(file, userbase);
        FileContent media = new FileContent("application/json", file);
        
        File newContent = new File();
        newContent.setTrashed(true);
        
        drive.files().update(userbaseID, newContent, media).execute();
        
        // clear file
        PrintWriter pw = new PrintWriter(file);
        pw.write("This file is only used temporarily for uploading the userbase.");
        pw.close();
        
        System.out.println("Userbase saved to drive.");
        
        engine.getHistory().addAction(new Action("Uploaded to Drive", "Uploaded userbase.json to drive."));
    }
    
    public static void uploadHistory(History history, Engine engine) throws IOException{
        System.out.println("Saving history.json");
        
        java.io.File file = new java.io.File(JAR_PATH+"/history.json");
        
        ObjectMapper obj = new ObjectMapper();
        obj.writeValue(file, history);
        FileContent media = new FileContent("application/json", file);
        
        File newContent = new File();
        newContent.setTrashed(true);
        
        drive.files().update(historyID, newContent, media).execute();
        
        // clear file
        PrintWriter pw = new PrintWriter(file);
        pw.write("This file is only used temporarily for uploading the history.");
        pw.close();
        
        System.out.println("History saved to drive.");
        
        engine.getHistory().addAction(new Action("Uploaded to Drive", "Uploaded history.json to drive."));
    }
    
    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    
    public static void deleteUserbase(Engine engine) throws IOException{
        drive.files().delete(userbaseID).execute();
        engine.getHistory().addAction(new Action("Deleted file from drive", "Deleted userbase from google drive."));
        System.out.println("Userbase deleted. Creating absent files...");
        createFileIfAbsent(engine);
    }
    
    public static void deleteHistory(Engine engine) throws IOException{
        drive.files().delete(historyID).execute();
        engine.getHistory().addAction(new Action("Deleted file from drive", "Deleted history from google drive."));
        System.out.println("History deleted. Creating absent files...");
        createFileIfAbsent(engine);
    }
    
    public static String getUserbaseID(){
        return userbaseID;
    }
    
    public static String getHistoryID(){
        return historyID;
    }
    
    public static final String getJarPath(){
        return JAR_PATH;
    }

}