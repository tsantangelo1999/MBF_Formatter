package com.company;

public class Segment
{
    public int start;
    public int end;
    public String desc;
    public String value;
    public char filler;
    public boolean rightJust;
    public enum style
    {
        AUTO, DEFAULT, FILL
    }
    public style type;

    public Segment(int s, int e, String d, boolean r, char f, int t, String v)
    {
        start = s;
        end = e;
        desc = d;
        rightJust = r;
        filler = f;
        if(t == 0)
        {
            type = style.AUTO;
        }
        else if(t == 1)
        {
            type = style.DEFAULT;
        }
        else if(t == 2)
        {
            type = style.FILL;
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
