package com.company;

public class Segment
{
    public int start;
    public int end;
    public String desc;
    public String value;
    public enum style
    {
        AUTO, DEFAULT, FILL
    }
    public style type;

    public Segment(int s, int e, String d)
    {
        start = s;
        end = e;
        desc = d;
        type = style.FILL;
        value = null;
    }

    public Segment(int s, int e, String d, int t, String v)
    {
        start = s;
        end = e;
        desc = d;
        if(t == 0)
        {
            type = style.AUTO;
        }
        else if(t == 1)
        {
            type = style.DEFAULT;
        }
        value = v;
    }

    public String toString()
    {
        return start + ", " + end + ", " + desc;
    }

    public int length()
    {
        return end - start + 1;
    }
}
