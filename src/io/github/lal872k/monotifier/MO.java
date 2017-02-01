/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lal872k.monotifier;

import java.util.Comparator;

/**
 *
 * @author L. Arthur Lewis II
 */
public class MO {
    
    public static final String MO_TYPE = "Absent", REMOVED_MO_TYPE = "Removed MO, 24 days";
    public static final int DAYS_TILL_MO_REMOVED = 24;
    
    private String section;
    
    private String date;
    
    private String type;
    
    private boolean sentToUser;
    
    public MO(){}
    
    public MO(String section, String date, String type){
        this.section = section;
        this.date = date;
        this.type = type;
    }
    
    public void setSentToUser(boolean sentToUser){
        this.sentToUser = sentToUser;
    }
    
    public void setSection(String section){
        this.section = section;
    }
    
    public void setDate(String date){
        this.date = date;
    }
    
    public void setType(String type){
        this.type = type;
    }
    
    public boolean sentToUser(){
        return sentToUser;
    }
    
    public String getSection(){
        return section;
    }
    
    public String getDate(){
        return date;
    }
    
    public String getType(){
        return type;
    }
    
    @Override
    public String toString(){
        return "MissedObligation[section='"+section+"',type='"+type+"',date='"+date+"',sentToUser='"+sentToUser+"']";
    }
    
    public static class DateComparator implements Comparator<MO>{

        @Override
        public int compare(MO o1, MO o2) {
            String date1 = o1.getDate();
            String date2 = o2.getDate();
            int month = 0, dom = 1, year = 2;

            String[] date1Fields = date1.split("/");

            String[] date2Fields = date2.split("/");


            if (date1Fields[year].equals(date2Fields[year])){
                if (date1Fields[month].equals(date2Fields[month])){
                    if (date1Fields[dom].equals(date2Fields[dom])){
                        return 0;
                    } else if (Integer.parseInt(date1Fields[dom])<Integer.parseInt(date2Fields[dom])){
                        return -1;
                    } else {
                        return 1;
                    }
                } else if (Integer.parseInt(date1Fields[month]) < Integer.parseInt(date2Fields[month])){
                    return -1;
                } else {
                    return 1;
                }
            } else if (Integer.parseInt(date1Fields[year]) < Integer.parseInt(date2Fields[year])){
                return -1;
            } else {
                return 1;
            }
        }
        
    }
}
