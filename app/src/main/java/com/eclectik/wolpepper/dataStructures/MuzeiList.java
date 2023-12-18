package com.eclectik.wolpepper.dataStructures;

import java.util.ArrayList;

/**
 * Created by mj on 5/7/17.
 */

public class MuzeiList {
    private String listName;
    private ArrayList<Papers> listOfPapersInList;
    private boolean isActive;

    public MuzeiList(){

    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public ArrayList<Papers> getListOfPapersInList() {
        return listOfPapersInList;
    }

    public void setListOfPapersInList(ArrayList<Papers> listOfPapersInList) {
        this.listOfPapersInList = listOfPapersInList;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
