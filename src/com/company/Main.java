package com.company;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
        ArrayList<RecordType> format = new ArrayList<>();
        //first arraylist defines each line, second arraylist contains parameters for the respective line
        //parameters currently are startIndex s, endIndex e, fieldDescription d, right alignment r, default fill character f, field type t, default value v
        int loc = 0;
        int pass = 0;
        boolean setup = true;
        boolean first = true;
        RecordType current = null;
        while(sc.hasNextLine())
        {
            //establish record types and hierarchy
            String[] line = sc.nextLine().split("\t");
            if(setup)
            {
                if(line[0].charAt(0) == '-')
                {
                    setup = false;
                    continue;
                }
                format.add(new RecordType(line[0], (line.length > 1 ? line[1].split(",") : null), first));
                first = false;
                continue;
            }

            //move on to next record type
            if(line[0].charAt(0) == '-')
            {
                try
                {
                    loc = getIndex(line[0].substring(1), format);
                }
                catch(Exception e)
                {
                    System.out.println("No such record type " + e.getMessage() + " exists");
                    return;
                }
                pass = 0;
                current = format.get(loc);
                continue;
            }

            //run a check
            if(current == null)
            {
                System.out.println("No current record type specified");
                return;
            }

            //add a parameter
            int posIndex = detailIndex(line, 'p');
            //String form;
            //int formIndex = detailIndex(line, 'f');
            String desc;
            int descIndex = detailIndex(line, 'd');
            boolean right = false;
            int rightIndex = detailIndex(line, 'r');
            char fill = ' ';
            int fillIndex = detailIndex(line, 'c'); //it's c for character i guess?
            int type = 2;
            int typeIndex = detailIndex(line, 't');
            String val = "";
            int valIndex = detailIndex(line, 'v');

            //set position data
            if(posIndex < 0)
            {
                missing("position", pass, loc);
                return;
            }
            String[] range = line[posIndex].substring(2).split("\\-");
            int start;
            int end;
            try
            {
                start = Integer.parseInt(range[0]);
                end = Integer.parseInt(range[1]);
                if(start < 1 || (pass > 0 && start <= current.parameters.get(pass - 1).end))
                {
                    System.out.println("Overlapping range at line " + (pass + 1)
                            + ", section " + (loc + 1));
                    return;
                }
            }
            catch(InputMismatchException e)
            {
                System.out.println("Invalid range parameter");
                return;
            }
            //if form is useful, put that stuff here

            //set description
            if(descIndex < 0)
            {
                missing("description", pass, loc);
                return;
            }
            desc = line[descIndex].substring(2);

            //set right justification
            if(rightIndex >= 0)
            {
                String r = line[rightIndex].substring(2);
                if(!(r.equalsIgnoreCase("true") || r.equalsIgnoreCase("false") || r.equals("")))
                    System.out.println("Right justification boolean is not true, false, or blank at line " + (pass + 1) + ", section " + (loc + 1) + ", continuing as if it were false");
                right = Boolean.valueOf(r);
            }

            //set filler character
            if(fillIndex >= 0)
            {
                try
                {
                    fill = line[fillIndex].charAt(2);
                }
                catch(IndexOutOfBoundsException e)
                {
                    System.out.println("Fill parameter blank at line " + (pass + 1) + ", section " + (loc + 1) + ", using spaces");
                    fill = ' ';
                }
            }

            //set type
            if(typeIndex >= 0)
            {
                try
                {
                    type = Integer.parseInt(line[typeIndex].substring(2));
                    if(type < 0 || type > 2)
                        throw new Exception();
                }
                catch(Exception e)
                {
                    System.out.println("Type must be an integer from 0-2, error found on line " + (pass + 1) + ", section " + (loc + 1));
                    return;
                }
            }

            //set default value
            if(valIndex < 0 && (type == 0 || type == 1))
            {
                System.out.println("No default value found on line " + (pass + 1) + ", section " + (loc + 1) + ", continuing by filling space with filler character");
            }
            if(valIndex >= 0)
            {
                val = line[valIndex].substring(2);

                //special cases
                if(val.charAt(0) == '$')
                {
                    if(val.substring(1).equals("date"))
                    {
                        DateFormat df = new SimpleDateFormat("yyyyMMdd");
                        Date date = new Date();
                        val = df.format(date);
                    }
                }
                if(val.length() > (end - start + 1))
                {
                    System.out.println("Given value at line " + (pass + 1) + ", section " + (loc + 1) + " is too long");
                    return;
                }
            }

            Segment s = new Segment(start, end, desc, right, fill, type, val);
            current.parameters.add(s);
            pass++;
        }
        System.out.println(print(format));
        sc.close();
        Scanner sc2 = new Scanner(data);
        ArrayList<Data> dataParams = new ArrayList<>();
        int where = -1;
        current = null;
        while(sc2.hasNextLine())
        {
            String[] line = sc2.nextLine().split("\t");
            if(line[0].charAt(0) == '-')
            {
                try
                {
                    loc = getIndex(line[0].substring(1), format);
                }
                catch(Exception e)
                {
                    System.out.println(line[0].substring(1) + " is not found as a valid record type");
                    return;
                }
                if(!format.get(loc).leads && (!(current != null && current.before(format.get(loc).name))))
                {
                    System.out.println(format.get(loc).name + " either may not lead" + (current == null ? "" : " or come after " + current.name));
                    return;
                }
                dataParams.add(new Data(line[0].substring(1)));
                where++;
                current = format.get(loc);
                continue;
            }
            dataParams.get(where).data.add(line);
        }
        sc2.close();

        for(Data d : dataParams)
        {
            try
            {
                loc = getIndex(d.name, format);
            }
            catch(Exception e)
            {
                System.out.println("there should never be an error here");
            }
            ArrayList<String[]> dataHere = d.data;
            for(Segment b : format.get(loc).parameters)
            {
                int index;
                switch(b.type)
                {
                    case FILL:
                        try
                        {
                            index = find(b.desc, dataHere);
                        }
                        catch(Exception e)
                        {
                            System.out.println("Field not found in data: " + e.getMessage());
                            return;
                        }
                        if(dataHere.get(index)[1].length() > b.length())
                        {
                            System.out.println(
                                    "Data for " + dataHere.get(index)[0] + " is too long. Length " + dataHere
                                            .get(index)[1].length() + " > " + b.length());
                            return;
                        }
                        if(b.rightJust)
                        {
                            for(int j = dataHere.get(index)[1].length(); j < b.length(); j++)
                            {
                                pw.print(b.filler);
                            }
                        }
                        pw.print(dataHere.get(index)[1]);
                        if(b.rightJust)
                        {
                            for(int j = dataHere.get(index)[1].length(); j < b.length(); j++)
                            {
                                pw.print(b.filler);
                            }
                        }
                        break;
                    case DEFAULT:
                        try
                        {
                            index = find(b.desc, dataHere);
                        }
                        catch(Exception e)
                        {
                            if(b.rightJust)
                            {
                                for(int j = b.value.length(); j < b.length(); j++)
                                {
                                    pw.print(b.filler);
                                }
                            }
                            pw.print(b.value);
                            if(!b.rightJust)
                            {
                                for(int j = b.value.length(); j < b.length(); j++)
                                {
                                    pw.print(b.filler);
                                }
                            }
                            break;
                        }
                        if(dataHere.get(index)[1].length() > b.length())
                        {
                            System.out.println(
                                    "Data for " + dataHere.get(index)[0] + " is too long. Length " + dataHere
                                            .get(index)[1].length() + " > " + b.length());
                            return;
                        }
                        if(b.rightJust)
                        {
                            for(int j = dataHere.get(index)[1].length(); j < b.length(); j++)
                            {
                                pw.print(b.filler);
                            }
                        }
                        pw.print(dataHere.get(index)[1]);
                        if(!b.rightJust)
                        {
                            for(int j = dataHere.get(index)[1].length(); j < b.length(); j++)
                            {
                                pw.print(b.filler);
                            }
                        }
                        break;
                    case AUTO:
                        if(b.rightJust)
                        {
                            for(int j = b.value.length(); j < b.length(); j++)
                            {
                                pw.print(b.filler);
                            }
                        }
                        pw.print(b.value);
                        if(!b.rightJust)
                        {
                            for(int j = b.value.length(); j < b.length(); j++)
                            {
                                pw.print(b.filler);
                            }
                        }
                        break;
                }
            }
            pw.println();
        }
        pw.close();
        fw.close();
    }

    public static int detailIndex(String[] line, char s)
    {
        for(int i = 0; i < line.length; i++)
        {
            if(line[i].charAt(0) == s)
                return i;
        }
        return -1;
    }

    public static int getIndex(String target, ArrayList<RecordType> data) throws Exception
    {
        for(int i = 0; i < data.size(); i++)
        {
            if(target.equals(data.get(i).name))
                return i;
        }
        throw new Exception(target);
    }

    public static int find(String target, ArrayList<String[]> data) throws Exception
    {
        for(int i = 0; i < data.size(); i++)
        {
            if(target.equals(data.get(i)[0]))
                return i;
        }
        throw new Exception(target);
    }

    public static void missing(String detail, int pass, int loc)
    {
        System.out.println("Missing " + detail + " parameter at line " + (loc + 1) + ", section " + (pass + 1));
    }

    public static String print(ArrayList<RecordType> a)
    {
        String ret = "";
        for(int i = 0; i < a.size(); i++)
        {
            ret += "[";
            RecordType item = a.get(i);
            for(int j = 0; j < item.parameters.size(); j++)
            {
                ret += "[";
                ret += item.parameters.get(j).toString();
                ret += "]";
                if(j != item.parameters.size() - 1)
                    ret += ", ";
            }
            ret += "]";
            if(i != a.size() - 1)
                ret += ", ";
        }
        return ret;
    }
}
