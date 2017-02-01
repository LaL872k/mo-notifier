/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lal872k.monotifier;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author L. Arthur Lewis II
 */
public class Action {
    
    private String timeStamp;
    private String title;
    private String description;
    private static DateFormat df;
    
    static {
        df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
    }
    
    public Action(){
        timeStamp = getCurrentTimeStamp();
    }
    
    public Action(String title, String description){
        this.title = title;
        this.description = description;
        timeStamp = getCurrentTimeStamp();
    }
    
    public void setTitle(String title){
        this.title = title;
    }
    
    public void setDescription(String description){
        this.description = description;
    }
    
    public void setTimeStamp(String timeStamp){
        this.timeStamp = timeStamp;
    }
    
    public String getTimeStamp(){
        return timeStamp;
    }
    
    public String getTitle(){
        return title;
    }
    
    public String getDescription(){
        return description;
    }
    
    @JsonIgnore
    public static String getCurrentTimeStamp(){
        Date date = new Date();
        return df.format(date);
    }
    
    public static class DateComparator implements Comparator<Action> {

        @Override
        public int compare(Action o1, Action o2) {
            try {
                Date date1 = df.parse(o1.getTimeStamp());
                Date date2 = df.parse(o2.getTimeStamp());
                return date1.compareTo(date2);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            return 0;
        }
        
        
    }
    
}
