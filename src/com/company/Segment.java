package com.company;

public class Segment
{
    public int start;
    public int end;
    public String form;
    public String desc;
    public String value;
    public char filler;
    public boolean rightJust;
    public enum style
    {
        AUTO, DEFAULT, FILL
    }
    public style type;

    public Segment(int s, int e, String f, String d, boolean r, char c, int t, String v)
    {
        start = s;
        end = e;
        form = f;
        desc = d;
        rightJust = r;
        filler = c;
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
