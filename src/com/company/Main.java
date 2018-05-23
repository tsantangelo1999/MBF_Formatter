package com.company;

import com.sun.corba.se.impl.io.TypeMismatchException;

import java.io.*;
import java.util.*;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        File config = new File("configTest.txt");
        File data = new File("data.txt");
        //data is currently experimental and subject to change
        FileWriter fw = new FileWriter("MBF.txt");
        PrintWriter pw = new PrintWriter(fw);
        Scanner sc = new Scanner(config);
        ArrayList<ArrayList<Segment>> format = new ArrayList<>();
        //first arraylist defines each line, second arraylist contains parameters for the respective line
        //parameters currently are startIndex, endIndex, fieldDescription
        format.add(new ArrayList<Segment>());
        int loc = 0;
        int pass = 0;
        while(sc.hasNextLine())
        {
            String[] line = sc.nextLine().split("\t");
            String pos;
            String form;
            String desc;
            try
            {
                pos = line[0];
                form = line[1];
                desc = line[2];
            }
            catch(IndexOutOfBoundsException e)
            {
                System.out.println("Invalid format");
                return;
            }
            String[] range = pos.split("\\-");
            int start;
            int end;
            try
            {
                start = Integer.parseInt(range[0]);
                end = Integer.parseInt(range[1]);
                if(start < 1 || (pass > 0 && start <= (int)format.get(loc).get(pass - 1).end))
                {
                    System.out.println("Overlapping range at line " + (loc + 1) + ", section " + (pass + 1));
                    return;
                }
            }
            catch(InputMismatchException e)
            {
                System.out.println("Invalid range parameter");
                return;
            }
            Segment s;
            if(line.length == 3)
            {
                s = new Segment(start, end, desc);
            }
            else if(line.length >= 5)
            {
                int type;
                String val;
                try
                {
                    type = Integer.parseInt(line[3]);
                    val = line[4];
                }
                catch(TypeMismatchException e)
                {
                    System.out.println("Type must be an integer from 0-2, error found on line " + (pass + 1) + ", section " + (loc + 1));
                    return;
                }
                if(val.length() > (end - start + 1))
                {
                    System.out.println("Given value at " + (loc + 1) + ", section " + (pass + 1) + " is too long");
                    return;
                }
                if(line.length > 5)
                    System.out.println("Extraneous data found on line " + (pass + 1) + ", section " + (loc + 1) + ", continuing with extraneous data ignored");
                s = new Segment(start, end, desc, type, val);
            }
            else
            {
                System.out.println("Unexpected amount of data found on line " + (pass + 1) + ", section " + (loc + 1));
                return;
            }
            format.get(loc).add(s);
            pass++;
        }
        System.out.println(print(format));
        sc.close();
        Scanner sc2 = new Scanner(data);
        ArrayList<String[]> dataParams = new ArrayList<>();
        while(sc2.hasNextLine())
        {
            dataParams.add(sc2.nextLine().split("\t"));
        }
        for(String[] a : dataParams)
        {
            System.out.println(a[0] + ", " + a[1]);
        }
        sc2.close();
        for(ArrayList<Segment> a : format)
        {
            for(Segment b : a)
            {
                int index;
                switch(b.type)
                {
                    case FILL:
                        try
                        {
                            index = find(b.desc, dataParams);
                        }
                        catch(Exception e)
                        {
                            System.out.println("Field not found in data: " + e.getMessage());
                            return;
                        }
                        if(dataParams.get(index)[1].length() > b.length())
                        {
                            System.out.println(
                                "Data for " + dataParams.get(index)[0] + " is too long. Length " + dataParams
                                        .get(index)[1].length() + " > " + b.length());
                            return;
                        }
                        pw.print(dataParams.get(index)[1]);
                        for(int i = dataParams.get(index)[1].length(); i < b.length(); i++)
                        {
                            pw.print(" ");
                        }
                        break;
                    case DEFAULT:
                        try
                        {
                            index = find(b.desc, dataParams);
                        }
                        catch(Exception e)
                        {
                            pw.print(b.value);
                            break;
                        }
                        if(dataParams.get(index)[1].length() > b.length())
                        {
                            System.out.println(
                                    "Data for " + dataParams.get(index)[0] + " is too long. Length " + dataParams
                                            .get(index)[1].length() + " > " + b.length());
                            return;
                        }
                        pw.print(dataParams.get(index)[1]);
                        for(int i = dataParams.get(index)[1].length(); i < b.length(); i++)
                        {
                            pw.print(" ");
                        }
                        break;
                    case AUTO:
                        pw.print(b.value);
                        break;
                }
            }
        }
        pw.close();
        fw.close();
    }

    public static int find(String target, ArrayList<String[]> data) throws Exception
    {
        for(int i = 0; i < data.size(); i++)
        {
            if(target.equals(data.get(i)[0]))
            {
                return i;
            }
        }
        throw new Exception(target);
    }

    public static String print(ArrayList<ArrayList<Segment>> a)
    {
        String ret = "";
        for(int i = 0; i < a.size(); i++)
        {
            ret += "[";
            ArrayList<Segment> item = a.get(i);
            for(int j = 0; j < item.size(); j++)
            {
                ret += "[";
                ret += item.get(j).toString();
                ret += "]";
                if(j != item.size() - 1)
                    ret += ", ";
            }
            ret += "]";
            if(i != a.size() - 1)
                ret += ", ";
        }
        return ret;
    }
}
