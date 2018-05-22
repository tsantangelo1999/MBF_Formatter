package com.company;

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
        ArrayList<ArrayList<Object[]>> format = new ArrayList<>();
        //first arraylist defines each line, second arraylist contains parameters for the respective line
        //parameters currently are startIndex, endIndex, fieldDescription
        format.add(new ArrayList<Object[]>());
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
                if(start < 1 || (pass > 0 && start <= (int)format.get(loc).get(pass - 1)[1]))
                    throw new Exception();
            }
            catch(InputMismatchException e)
            {
                System.out.println("Invalid range parameter");
                return;
            }
            catch(Exception e)
            {
                System.out.println("Overlapping range at line " + (loc + 1) + ", parameter " + (pass + 1));
                return;
            }
            Object[] arr = {start, end, desc};
            format.get(loc).add(arr);
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
        sc2.close();
        for(ArrayList<Object[]> a : format)
        {
            for(Object[] b : a)
            {
                int index;
                try
                {
                    index = find((String)b[2], dataParams);
                }
                catch(Exception e)
                {
                    System.out.println("Field not found in data: " + e.getMessage());
                    return;
                }
                if(dataParams.get(index)[1].length() > ((int)b[1] - (int)b[0] + 1))
                {
                    System.out.println("Data for " + dataParams.get(index)[0] + " is too long. Length " + dataParams.get(index)[1].length() + " > " + ((int)b[1] - (int)b[0] + 1));
                    return;
                }
                pw.print(dataParams.get(index)[1]);
                for(int i = dataParams.get(index)[1].length(); i < ((int)b[1] - (int)b[0] + 1); i++)
                {
                    pw.print(" ");
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
            if(data.get(i)[0] == target)
            {
                return i;
            }
        }
        throw new Exception(target);
    }

    public static String print(ArrayList<ArrayList<Object[]>> a)
    {
        String ret = "";
        for(int i = 0; i < a.size(); i++)
        {
            ret += "[";
            ArrayList<Object[]> item = a.get(i);
            for(int j = 0; j < item.size(); j++)
            {
                ret += "[";
                Object[] item2 = item.get(j);
                for(int k = 0; k < item2.length; k++)
                {
                    ret += item2[k];
                    if(k != item2.length - 1)
                        ret += ", ";
                }
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
