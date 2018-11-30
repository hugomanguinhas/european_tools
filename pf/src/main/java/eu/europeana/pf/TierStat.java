/**
 * 
 */
package eu.europeana.pf;

import java.io.PrintStream;
import java.util.LinkedHashMap;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 17 Apr 2018
 */
public class TierStat extends LinkedHashMap<Integer,Integer>
{
    public TierStat(int tmax)
    {
        for ( int i = 0; i <= tmax; i++ ) { put(i, 0); }
    }

    public void newTier(int tier)
    {
        put(tier, get(tier) + 1);
    }

    public void print(PrintStream ps)
    {
        for ( Integer level : keySet() )
        {
            ps.print("T" + level + ":" + get(level));
            ps.print(" ");
        }
    }
}