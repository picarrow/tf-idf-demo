import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main
{
    public static final int DOCUMENTS_TO_SHOW = 10;

    public static void main(final String[] args)
    {
        try(final Scanner inputScanner = new Scanner(System.in))
        {
            System.out.print("Enter the path to where the documents are: ");
            final Path directory = Paths.get(inputScanner.nextLine());

            final Map<String, Set<Path>> invertedIndex = createInvertedIndex(directory);

            final long totalDocuments = Files.list(directory).count();

            while(true)
            {
                System.out.print("Enter the query term: ");
                final String queryTerm = inputScanner.nextLine();

                if(queryTerm.equals(""))
                {
                    break;
                }

                Set<Path> matchingDocuments = invertedIndex.get(queryTerm);
                matchingDocuments = matchingDocuments == null ? new HashSet<>() : matchingDocuments;

                final List<Pair> pairs = calcTermScores(queryTerm, matchingDocuments, totalDocuments);

                System.out.println("Identifying the " + DOCUMENTS_TO_SHOW + " document(s) most relevant to the query...");
                System.out.println();

                for(int i = 0; i < DOCUMENTS_TO_SHOW && i < pairs.size(); ++i)
                {
                    System.out.println(pairs.get(i).document.getFileName() + " with a TF-IDF of " + pairs.get(i).tfidf);
                }

                System.out.println();
                System.out.println(Math.min(DOCUMENTS_TO_SHOW, pairs.size()) + " out of " + matchingDocuments.size() + " documents listed.");
            }
        }
        catch(final IOException e)
        {
            System.out.println("Error: No such directory.");
        }
    }

    public static List<Pair> calcTermScores(final String queryTerm, final Set<Path> matchingDocuments, final long totalDocuments) throws IOException
    {
        final List<Pair> pairs = new ArrayList<>();
        final double idf = matchingDocuments.size() > 0 ? Math.log(totalDocuments / matchingDocuments.size()) : 0;

        for(Path document : matchingDocuments)
        {
            int tf = 0;

            try(final Scanner termScanner = new Scanner(document))
            {
                termScanner.useDelimiter("[^A-Z|a-z]+");

                while(termScanner.hasNext())
                {
                    final String term = termScanner.next();

                    if(term.equalsIgnoreCase(queryTerm))
                    {
                        ++tf;
                    }
                }
            }

            pairs.add(new Pair(document, tf * idf));
        }

        Collections.sort(pairs);
        Collections.reverse(pairs);

        return pairs;
    }

    public static Map<String, Set<Path>> createInvertedIndex(final Path directory) throws IOException
    {
        final Map<String, Set<Path>> invertedIndex = new HashMap<>();

        final Iterator<Path> documentIterator = Files.list(directory).iterator();

        while(documentIterator.hasNext())
        {
            indexByDocument(documentIterator.next(), invertedIndex);
        }

        return invertedIndex;
    }

    private static void indexByDocument(final Path document, final Map<String, Set<Path>> invertedIndex) throws IOException
    {
        try(final Scanner termScanner = new Scanner(document))
        {
            termScanner.useDelimiter("[^A-Z|a-z]+");

            while(termScanner.hasNext())
            {
                final String term = termScanner.next().toLowerCase();

                if(!invertedIndex.containsKey(term))
                {
                    invertedIndex.put(term, new HashSet<>());
                }

                invertedIndex.get(term).add(document);
            }
        }
    }

    private static class Pair implements Comparable<Pair>
    {
        private final Path document;
        private final double tfidf;

        public Pair(final Path document, final double tfidf)
        {
            this.document = document;
            this.tfidf = tfidf;
        }

        public int compareTo(final Pair o)
        {
            if(tfidf > o.tfidf)
            {
                return 1;
            }

            if(tfidf < o.tfidf)
            {
                return -1;
            }

            return 0;
        }

        public String toString()
        {
            return "[" + document.getFileName() + ", " + tfidf + "]";
        }
    }
}
