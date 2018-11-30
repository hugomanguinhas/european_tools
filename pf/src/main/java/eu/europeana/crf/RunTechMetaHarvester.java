/**
 * 
 */
package eu.europeana.crf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;

import com.mongodb.MongoClient;

import eu.europeana.pf.media.MediaType;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 13 Oct 2017
 */
public class RunTechMetaHarvester
{
    public static final void main(String[] args)
           throws IOException
    {
        MongoClient cli = new MongoClient(args[0], 27017);

        File file = new File(args[3]);
        Collection<String> filter = FileUtils.readLines(file);
        try
        {
            Map<MediaType,PrintStream> targets = getTargets(args[2]);
            new TechMetadataHarvester(cli.getDatabase(args[1])
                                    , targets, 10, filter).classifyAll();
            for ( PrintStream ps : targets.values() )
            {
                try { ps.flush(); } finally { ps.close(); }
            }
        }
        finally
        {
            cli.close();
        }
    }

    private static Map<MediaType,PrintStream> getTargets(String target)
            throws IOException
    {
        Map<MediaType,PrintStream> map = new LinkedHashMap();
        File   file = new File(target);
        String name = file.getName();
        File   dir  = file.getParentFile();
        for ( MediaType mt : MediaType.values() )
        {
            String fn = name + "." + mt.getLabel() + ".csv.gz";
            map.put(mt, new PrintStream(new GZIPOutputStream(
                        new FileOutputStream(new File(dir, fn)))));
        }
        return map;
    }
}
