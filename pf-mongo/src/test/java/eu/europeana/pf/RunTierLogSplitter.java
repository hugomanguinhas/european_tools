/**
 * 
 */
package eu.europeana.pf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 5 Oct 2017
 */
public class RunTierLogSplitter
{
    private static Pattern _pattern = Pattern.compile(".*[:][>] T([0-4])[:] (/[0-9]+/.*)");

    public void run(InputStream is, Map<Integer,PrintStream> out) throws IOException 
    {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            while ( reader.ready() )
            {
                String line = reader.readLine();
                Matcher m = _pattern.matcher(line);
                if ( !m.matches() ) { continue; }

                PrintStream ps = out.get(Integer.parseInt(m.group(1)));
                if ( ps == null ) { continue; }

                ps.println(m.group(2));
            }
        }
        finally { reader.close(); }
    }

    public static final void main(String[] args) throws IOException
    {
        File dir = new File("D:\\work\\incoming\\tiers\\");
        Map<Integer,PrintStream> map = new HashMap();
        map.put(0, new PrintStream(new File(dir, "T0.txt")));
        map.put(1, new PrintStream(new File(dir, "T1.txt")));
        map.put(2, new PrintStream(new File(dir, "T2.txt")));
        map.put(3, new PrintStream(new File(dir, "T3.txt")));
        map.put(4, new PrintStream(new File(dir, "T4.txt")));
        new RunTierLogSplitter().run(new GZIPInputStream(new FileInputStream(new File(dir, "progress.log.gz"))), map);
        for ( PrintStream ps : map.values() )
        {
            ps.flush();
            ps.close();
        }
    }
}
