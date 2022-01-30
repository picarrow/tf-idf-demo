import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main
{
    public static void main(
        final String[] args)
    {
        final Scanner sc = new Scanner(System.in);
        
        System.out.print("Enter the relative file directory path: ");
        final String folderPath = sc.nextLine();
        
        final Map<String, List<File>> invertedIndex = createInvertedIndex(folderPath);
        
        String query = "placeholder";
        
        while(!query.equals(""))
        {
            System.out.print("Enter the term query: ");
            query = sc.nextLine();
            
            System.out.println("Printing the 5 documents most relevant to the query...");
            System.out.println();
            
            List<File> documents = invertedIndex.get(query);
            documents = documents == null ? new ArrayList<>() : documents;
            
            final double[] tfidfs = new double[documents.size()];
            final int totalNumberOfDocuments = new File(folderPath).listFiles().length;
            final double idf = documents.size() > 0 ? Math.log(totalNumberOfDocuments / documents.size()) : 0;
            
            for(int i = 0; i < documents.size(); i++)
            {
                int totalTerms = 0;
                
                try(BufferedReader br = new BufferedReader(new FileReader(documents.get(i))))
                {
                    String line = br.readLine();
                    
                    while(line != null)
                    {
                        line = line.toLowerCase().replaceAll("[^a-z]+", " ").trim();
                        String[] terms = line.split(" ");
                        
                        for(String term : terms)
                        {
                            if(query.equals(term))
                            {
                                tfidfs[i]++;
                            }
                            
                            totalTerms++;
                        }
                        
                        line = br.readLine();
                    }
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
                
                tfidfs[i] *= idf;
            }
            
            final List<Pair> pairs = new ArrayList<>();
            
            for(int i = 0; i < documents.size(); i++)
            {
                pairs.add(new Pair(documents.get(i), tfidfs[i]));
            }
            
            Collections.sort(pairs);
            Collections.reverse(pairs);
            
            int i = 0;
            
            while(i < 5 && i < pairs.size())
            {
                System.out.println(pairs.get(i).file.getName() + " with a TF-IDF of " + pairs.get(i).termFrequency + "%");
                i++;
            }
            
            System.out.println();
            System.out.println(Math.min(5, pairs.size()) + " item(s) listed out of " + documents.size() + " documents.");
        }
    }
    
    public static Map<String, List<File>> createInvertedIndex(
        final String folderPath)
    {
        final File folder = new File(folderPath);
        final File[] documents = folder.listFiles();
        final Map<String, List<File>> invertedIndex = new HashMap<>();
        
        for(File document : documents)
        {
            try(BufferedReader br = new BufferedReader(new FileReader(document)))
            {
                String line = br.readLine();
                
                while(line != null)
                {
                    line = line.toLowerCase().replaceAll("[^a-z]+", " ").trim();
                    String[] terms = line.split("\\s");
                    
                    for(String term : terms)
                    {
                        if(!invertedIndex.containsKey(term))
                        {
                            invertedIndex.put(term, new ArrayList<>());
                        }
                        
                        if(!invertedIndex.get(term).contains(document))
                        {
                            invertedIndex.get(term).add(document);
                        }
                    }
                    
                    line = br.readLine();
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        
        return invertedIndex;
    }
    
    private static class Pair
        implements Comparable<Pair>
    {
        private final File file;
        private final double termFrequency;
        
        public Pair(
            final File file,
            final double termFrequency)
        {
            this.file = file;
            this.termFrequency = termFrequency;
        }
        
        public int compareTo(
            final Pair o)
        {
            if(termFrequency > o.termFrequency)
            {
                return 1;
            }
            
            if(termFrequency < o.termFrequency)
            {
                return -1;
            }
            
            return 0;
        }
        
        public String toString()
        {
            return "[" + file.getName() + ", " + termFrequency + "]";
        }
    }
}
