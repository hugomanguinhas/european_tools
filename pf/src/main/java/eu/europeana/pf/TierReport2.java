/**
 * 
 */
package eu.europeana.pf;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import eu.europeana.pf.TierStat;
import eu.europeana.pf.media.MediaType;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 13 Oct 2017
 */
public class TierReport2 extends LinkedHashMap<String,TierStat>
{
    public TierReport2() {}

    public void newTierValue(String name, int tier)
    {
        if ( tier < 0 ) { return; }
        get(name).newTier(tier);
    }

    public void init(TierConfig config)
    {
        for ( String key : config.keySet() )
        {
            put(key, new TierStat(config.get(key).getLevels()));
        }
    }
    
}
