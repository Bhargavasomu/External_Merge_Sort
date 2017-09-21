import java.util.*;        // For functons like Arrays.toString(arr_name)
import java.io.*;               // For File reading and writing

public class sort
{
    public static String inputFilePath,outputFilePath,metadataFilePath,order;
    public static String[] sortBasisColumns = new String[20];   // Since only 20 columns at max can be given as input
    public static int numSortBasisColumns = 0;
    public static long MMSize,tuple_size,numSortedSublists,numTotalLinesFile;
    public static Hashtable<String,Long> columnsPresentTable = new Hashtable<String,Long>();       // this table has key as column name and value as size in bytes
    public static Hashtable<String,Integer> positionColumns = new Hashtable<String,Integer>();
    public static String[] columnsPresentMetadata = new String[20];
    public static int numColumnsPresentMetadata;

    public static void checkSemanticsAndAssign(String[] args)   // Function checks semantics and assigns values to the above static variables
    {
        /* The Semantics are 1)Checking of Sufficient Arguments 2)MMSize should be Integer 3)Order should be only asc or desc */
        // Checking whether a minimum of 5 arguments are given or not
        int argsLength = args.length;
        if(argsLength < 6)
        {
            System.err.println("Error : Insufficient Arguments");
            System.exit(0);
        }
        inputFilePath = args[0];
        outputFilePath = args[1];
        metadataFilePath = args[2];
        // Checking MMSize is integer or not
        try
        {
            MMSize = Integer.parseInt(args[3]);
        }
        catch(Exception e)
        {
            System.err.println("Error : Main Memory Size(Third Argument) should be an integer");
            System.exit(0);
        }
        // Checking whether the order is only either asc or desc
        order = args[4];
        if( (!order.equals("asc")) && (!order.equals("desc")) )
        {
            System.err.println("Error : Order(Fourth Argument) should be either 'asc' or 'desc' ");
            System.exit(0);
        }
        for(int i=5;i<argsLength;i++)
        {
            numSortBasisColumns++;
            sortBasisColumns[i-5] = args[i];
        }
    }

    public static void checkExistenceFilePath(String inputFilePath)
    {
        File file = new File(inputFilePath);
        if ( (!file.exists()) || (file.isDirectory()) )
        {
            System.err.println("Error : Input File "+inputFilePath+" not found");
            System.exit(0);
        }
    }

    public static void checkWithAvailableMemory(long memory)
    {
        long inputMemory = (long)memory * 1000000;
        long availableMemory = Runtime.getRuntime().freeMemory();
        if(inputMemory >= availableMemory)
        {
            System.err.println("Error : The Amount of Main Memory given as Input, is not Available");
            System.exit(0);
        }
    }

    public static void checkInputColumnsValidity()
    {
        for(int i=0;i<sortBasisColumns.length;i++)
        {
            if(sortBasisColumns[i]==null)
                break;
            if ( columnsPresentTable.containsKey(sortBasisColumns[i]) == false)
            {
                System.err.println("Error : The Column " + sortBasisColumns[i] + " doesn't exist");
                System.exit(0);
            }
        }
    }

    public static void readMetadataFile(String filepath)
    {
        File file = new File(filepath);
        BufferedReader br=null;
        String line=null;
        try
        {
            br = new BufferedReader(new FileReader(file));
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        int pos=0;
        while(true)
        {
            try
            {
                line = br.readLine();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
            if(line == null)
                break;
            line = line.trim();     // trim all the white spaces in the beginning and end
            String[] words=line.split(",");
            columnsPresentTable.put(words[0],Long.parseLong(words[1]));
            pos++;
            numColumnsPresentMetadata++;
            columnsPresentMetadata[numColumnsPresentMetadata-1] = words[0];
            positionColumns.put(words[0],pos);
        }
    }

    public static String[] splitRemoveSpaces(String line)
    {
        line = line.replaceAll("\\s+"," ");     // Remove extra spaces between fields of string and replace it with a single space
        String[] arr = line.trim().split(" ");
        for(int i=0;i<arr.length;i++)
            arr[i] = arr[i].trim();
        return arr;
    }

    public static long findTupleSize(String line)
    {
        long size=0;
        String str;
        Set<String> keys = columnsPresentTable.keySet();
        //Obtaining iterator over set entries
        Iterator<String> itr = keys.iterator();
        //Displaying Key and value pairs
        while (itr.hasNext())
        {
           // Getting Key
           str = itr.next();
           size += (long)columnsPresentTable.get(str);
        }
        return size;
    }

    public static void checkFeasibility()
    {
        if (numSortedSublists * tuple_size > (MMSize*1000000))
        {
            System.err.println("Error : Not Doable with the given values");
            System.exit(0);
        }
    }
    public static long iterateFileForLines(String inputFilePath)
    {
        File file = new File(inputFilePath);
        BufferedReader br=null;
        String line=null;
        long numLines=0;
        try
        {
            br = new BufferedReader(new FileReader(file));
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        while(true)
        {
            try
            {
                line = br.readLine();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
            if(line==null)
                break;
            numLines++;
        }
        return numLines;
    }

    public static String parseString(String str,int pos)
    {
        int i;
        int start=0;
        String columnName;
        for(i=1;i<pos;i++)
        {
            columnName = columnsPresentMetadata[i-1];
            start += (int)(columnsPresentTable.get(columnName)+2);
        }
        columnName = columnsPresentMetadata[i-1];
        int len = (int)(long)(columnsPresentTable.get(columnName));
        return str.substring(start,len+start-1);
    }

    public static void writeToFile(String[] rows,long lastRowNumConsidered,int listNum)
    {
        File file = new File("tempDir");
        //if directory exists
        if(!file.exists())
        {
            try
            {
                file.mkdir();
            }
            catch(Exception e)
            {
                System.out.println(e);
                System.out.println("Failed to create Directory tempDir!");
            }
        }

        String fileName = "tempDir/" + listNum + ".txt";
        try
        {
            PrintWriter writer = new PrintWriter(fileName,"UTF-8");
            for(int i=0;i<=lastRowNumConsidered;i++)
            {
                writer.print(rows[i]+"\r\n");
            }
            writer.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
            System.out.println("Unable to create file " + fileName);
        }
    }

    public static int compare(String s1, String s2)
    {
        if(s1==null)
            return 1;
        if(s2==null)
            return -1;
        int colPos=0;
        String s11=null,s22=null;
        for(int i=0;i<numSortBasisColumns;i++)
        {
            colPos = positionColumns.get(sortBasisColumns[i]);
            s11 = parseString(s1,colPos);
            s22 = parseString(s2,colPos);
            if(s11.compareTo(s22)==0)
                continue;
            else
                return s11.compareTo(s22);
        }
        return 0;
    }

    public static void sortAndWrite(String[] rows,long lastRowNumConsidered,int listNum)
    {
        for(int i=(int)lastRowNumConsidered+1;i<rows.length;i++)
            rows[i]=null;
        Arrays.sort(rows,new Comparator<String>()
        {
            public int compare(String s1, String s2)
            {
                if(s1==null)
                    return 1;
                if(s2==null)
                    return -1;
                int colPos=0;
                String s11=null,s22=null;
                for(int i=0;i<numSortBasisColumns;i++)
                {
                    colPos = positionColumns.get(sortBasisColumns[i]);
                    s11 = parseString(s1,colPos);
                    s22 = parseString(s2,colPos);
                    if(s11.compareTo(s22)==0)
                        continue;
                    else
                        return s11.compareTo(s22);
                }
                return 0;
            }
        });
        writeToFile(rows,lastRowNumConsidered,listNum);
    }

    public static void readInputFile(String inputFilePath)
    {
        File file = new File(inputFilePath);
        BufferedReader br=null;
        String line=null;
        try
        {
            br = new BufferedReader(new FileReader(file));
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        tuple_size = findTupleSize(line);
        numSortedSublists = (long) Math.ceil((double) file.length() / (MMSize*1000000));
        numTotalLinesFile = iterateFileForLines(inputFilePath);
        long numLinesEachList;
        if(file.length()<=(MMSize*1000000))
            numLinesEachList = numTotalLinesFile;
        else
            numLinesEachList = (MMSize*1000000) / tuple_size;
        String[] rows = new String[(int)numLinesEachList];       // this array contains the rows of the file
        int rows_ind=-1;
        checkFeasibility();
        long memoryOccupied=0;
        int listNum = 0;
        boolean fillFlag=false;
        int files_read=1,prsnt_line=0;
        long sublist_size=0;
        while(files_read-1!=numSortedSublists)
        {
            sublist_size=numLinesEachList;
            if(files_read*numLinesEachList>numTotalLinesFile){
                sublist_size=numTotalLinesFile-((files_read-1)*numLinesEachList);
            }
            for(int j=0;j<sublist_size;j++)
            {
                try
                {
                    rows[j]=br.readLine();
                }
                catch(Exception e)
                {
                    System.out.println(e);
                }
            }
            sortAndWrite(rows,sublist_size-1,files_read);
            files_read++;
        }
    }

    public static String[] findMinStrAndPos(String[] minEachFile)
    {
        String minStr = minEachFile[1];
        int pos = 1;
        int compareValue;
        for(int i=2;i<=numSortedSublists;i++)
        {
            compareValue = compare(minStr,minEachFile[i]);
            if(compareValue>0)
            {
                minStr = minEachFile[i];
                pos=i;
            }
        }
        String[] ans = new String[2];
        ans[0] = minStr;
        ans[1] = Integer.toString(pos);
        return ans;
    }

    public static void phase2()
    {
        int numFiles = (int) numSortedSublists;
        String[] minEachFile = new String[numFiles+1];  // minEachFile[i] Stores the minimum string in file i.txt which are readed till now
        File[] file = new File[numFiles+1];
        BufferedReader[] br = new BufferedReader[numFiles+1];
        try
        {
            for(int i=1;i<=numFiles;i++)
            {
                file[i] = new File("tempDir/" + i + ".txt");
                br[i] = new BufferedReader(new FileReader(file[i]));
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        for(int i=1;i<=numFiles;i++)
        {
            try
            {
                minEachFile[i] = br[i].readLine();
            }
            catch(Exception e)
            {
                System.out.println("Cannot Read Line in the file " + i + ".txt");
                System.out.println(e);
            }
        }
        int numFilesReadCompletely=0;
        String minStr = null;   // stores the minimum string of all the file buffers
        int fileNumber = -1;    // gives the number of file which has the global minimum string. fileNumber=1 means 1.txt
        String[] strPos = new String[2];    // stores the minimum string of all the file buffers and which file contains that minimum
        PrintWriter outputWriter = null;           // pointer to the output file
        try
        {
            outputWriter = new PrintWriter(outputFilePath,"UTF-8");
        }
        catch(Exception e)
        {
            System.out.println("Error: Cannot Create Output File");
        }
        while(numFilesReadCompletely != numFiles)
        {
            strPos = findMinStrAndPos(minEachFile).clone();
            minStr = strPos[0];
            fileNumber = Integer.parseInt(strPos[1]);
            outputWriter.print(minStr+"\r\n");   // write line to output file
            try
            {
                minEachFile[fileNumber] = br[fileNumber].readLine();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
            if(minEachFile[fileNumber]==null)
            {
                numFilesReadCompletely++;
            }
        }
        outputWriter.close();
        // Deleting all files inside directory
        for(int i=1;i<=numFiles;i++)
        {
			try
            {
				File f = new File("tempDir/"+ i + ".txt");
         		f.delete();
			}
			catch(Exception e)
            {
				System.out.print(e);
                System.out.println("Cannot Delete File");
			}
		}
        File f = new File("tempDir");
        try
        {
            f.delete();
        }
        catch(Exception e)
        {
            System.out.println(e);
            System.out.println("Cannot Delete Folder");
        }
    }

	public static void main(String[] args)     // The int main() function
    {
        checkSemanticsAndAssign(args);
        checkExistenceFilePath(inputFilePath);      // checks if input file exists
        checkExistenceFilePath(metadataFilePath);   // checks if metadata file exists
        // checkWithAvailableMemory(MMSize);           // checks if MMSize amount of Main Memory is available or not
        readMetadataFile(metadataFilePath);         // reads the metadataFilePath and stores the columns names in the Hashtable
        checkInputColumnsValidity();                // checks whether the given columns are present in the file or not
        readInputFile(inputFilePath);
        phase2();
        System.out.println("Completed Sorting The File");
	}
}
