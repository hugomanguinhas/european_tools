/**
 * 
 */
package eu.europeana.pf2.db;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;

import eu.europeana.pf2.media.MediaType;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 4 Jun 2018
 */
@Entity("tiers")
public class TierEntry
{
    @Id
    private String _uri;

    @Indexed(options = @IndexOptions(name="idx_dataset"))
    @Property("ds")
    private String _ds;

    @Indexed(options = @IndexOptions(name="idx_ts"))
    @Property("timestamp")
    private Date _timestamp;

    @Indexed(options = @IndexOptions(name="idx_tiers"))
    @Property("tiers")
    private Map<String,Integer> _tiers;

    @Indexed(options = @IndexOptions(name="idx_type"))
    @Property("type")
    private MediaType _type;

    @Indexed(options = @IndexOptions(name="idx_country"))
    @Property("country")
    private String _country;

    @Indexed(options = @IndexOptions(name="idx_dprov"))
    @Property("dataProvider")
    private String _dataProvider;

    @Indexed(options = @IndexOptions(name="idx_prov"))
    @Property("provider")
    private String _provider;

    protected TierEntry() {}

    public TierEntry(String uri, String ds, Date timestamp
                   , Map<String,Integer> tiers, MediaType type
                   , String country, String provider, String dataProvider)
    {
        _uri          = uri;
        _ds           = ds;
        _timestamp    = timestamp;
        _tiers        = tiers;
        _type         = type;
        _country      = country;
        _provider     = provider;
        _dataProvider = dataProvider;
    }

    public String              getURI()          { return _uri;          }
    public String              getDataset()      { return _ds;           }
    public Map<String,Integer> getTiers()        { return _tiers;        }
    public MediaType           getType()         { return _type;         }
    public String              getCountry()      { return _country;      }
    public String              getProvider()     { return _provider;     }
    public String              getDataProvider() { return _dataProvider; }

    public void setTier(String label, int tier) { _tiers.put(label, tier); }

}
