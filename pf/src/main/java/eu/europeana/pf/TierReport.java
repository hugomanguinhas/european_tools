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
public class TierReport
{
    private Map<MediaType,TierStat>  _tM = new LinkedHashMap();
    private Map<String,TierStat>     _tC = new LinkedHashMap();
    private Map<Integer,PrintStream> _ps = null;

    public TierReport() { this(null); }

    public TierReport(Map<Integer,PrintStream> ps) { _ps = ps; }

    public TierStat getMedia(MediaType mt)
    { 
        TierStat ts = _tM.get(mt);
        if ( ts == null ) { ts = new TierStat(4); _tM.put(mt, ts); }
        return ts;
    }

    public TierStat getCountry(String country)
    {
        TierStat ts = _tC.get(country);
        if ( ts == null ) { ts = new TierStat(4); _tC.put(country, ts); }
        return ts;
    }

    public void newTierRecord(String record, Integer tier)
    {
        if ( _ps == null ) { return; }

        PrintStream ps = _ps.get(tier);
        if ( ps != null ) { ps.println(record); }
    }

    public Map<Integer,PrintStream> getTierRecords() { return _ps; }

    public void printPerMedia(PrintStream ps)
    {
        for ( MediaType type : _tM.keySet() )
        {
            ps.print(String.format("%1$5s", type.getLabel()));
            ps.print(" ");
            _tM.get(type).print(ps);
            ps.println();
        }
    }

    public void printPerCountry(PrintStream ps)
    {
        for ( String country : _tC.keySet() )
        {
            ps.print(String.format("%1$5s", country));
            ps.print(" ");
            _tC.get(country).print(ps);
            ps.println();
        }
    }
}
