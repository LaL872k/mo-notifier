/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lal872k.monotifier;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author L. Arthur Lewis II
 */
public class History {
    
    private List<Action> actions;
    
    public History(){
        actions = new ArrayList();
    }
    
    public boolean addAction(Action action){
        synchronized (actions){
            return actions.add(action);
        }
    }
    
    public boolean removeAction(Action action){
        synchronized (actions){
            return actions.remove(action);
        }
    }
    
    public void setActions(List<Action> actions){
        this.actions = actions;
    }
    
    public List<Action> getActions(){
        return actions;
    }
    
}
