package com.company;

import java.util.ArrayList;

public class RecordType
{
    public String name;
    public String[] precedes;
    public ArrayList<Segment> parameters;
    public boolean leads;
    public boolean ends;

    public RecordType(String s, String[] p, boolean l)
    {
        name = s;
        precedes = p;
        leads = l;
        parameters = new ArrayList<>();
        if(precedes == null)
            ends = true;
        else
            ends = false;
    }

    public boolean before(String t)
    {
        for(String a : precedes)
        {
            if(a.equals(t))
                return true;
        }
        return false;
    }
}
