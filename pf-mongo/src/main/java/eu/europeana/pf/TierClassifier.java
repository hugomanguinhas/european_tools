/**
 * 
 */
package eu.europeana.pf;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.converters.Converters;

import com.mongodb.MongoClient;

import eu.europeana.ld.ResourceCallback;
import eu.europeana.ld.edm.EDM;
import eu.europeana.ld.edm.EuropeanaDataUtils;
import eu.europeana.ld.edm.ORE;
import eu.europeana.ld.mongo.MongoEDMHarvester;
import eu.europeana.pf.alg.TierClassifierAlgorithm;
import eu.europeana.pf.db.TierEntry;
import eu.europeana.pf.db.TierEntryConstants;
import eu.europeana.pf.db.type.MediaTypeConverter;
import eu.europeana.pf.media.MediaClassifier;
import static eu.europeana.pf.media.MediaUtils.*;
import static eu.europeana.util.EDMUtils.*;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 2 Jun 2018
 */
public class TierClassifier implements TierEntryConstants
{
    private MongoEDMHarvester  _mh;
    private List<TierClassifierAlgorithm> _classifiers = null;
    private Morphia            _morphia;
    private Datastore          _ds;

    private TierUpdateHandler  _chdl = new TierUpdateHandler();

    public TierClassifier(MongoClient cli, String dbs, String dbt
                        , Collection<String> filter
                        , TierClassifierAlgorithm... classifiers)
           throws IOException
    {
        _morphia = initMorphia(new Morphia());
        _ds      = initDatastore(cli, dbt);
        _ds.ensureIndexes();
        _mh = new MongoEDMHarvester(cli, cli.getDatabase(dbs), null, false, true
                                  , checkForTechMeta(classifiers));
        _mh.setFilter(filter);
        _classifiers = Arrays.asList(classifiers);
    }

    /***************************************************************************
     * Public Methods
     **************************************************************************/

    public void classify(String uri) 
    { 
        if ( exists(uri) ) { return; }

        _mh.harvest(uri, _chdl);
    }

    public void classify(Collection<String> uris) { _mh.harvest(uris, _chdl); }

    public void classifyAll()                     { _mh.harvestAll(_chdl);    }

    public void classifyDataset(String dataset)
    {
        _mh.harvestBySearch(getDatasetQuery(dataset), _chdl);
    }


    /***************************************************************************
     * Private Methods
     **************************************************************************/

    private boolean checkForTechMeta(TierClassifierAlgorithm... classifiers)
    {
        for ( TierClassifierAlgorithm c : classifiers )
        {
            if ( c instanceof MediaClassifier ) { return true; }
        }
        return false;
    }

    protected String getID(String uri)
    {
        if ( uri != null && uri.startsWith(ITEM_DATA_NS) ) {
            return uri.substring(ITEM_DATA_NS.length()-1);
        }

        return uri;
    }

    protected String getDataset(String id)
    {
        return id.substring(1, id.lastIndexOf('/'));
    }

    protected String getDatasetQuery(String... datasets)
    {
        String str = null;
        for ( String dataset : datasets )
        {
            if ( dataset.trim().isEmpty() ) { continue; }

            str = (str == null ? "" : str + "|") + "(" + dataset + ")";
        }
        return "{'about': { $regex: '^/" + str + "/.*' }}";
    }

    private Morphia initMorphia(Morphia m) throws IOException
    {
        m.mapPackage("eu.europeana.pf2.db");

        Converters converters = m.getMapper().getConverters();
        converters.addConverter(new MediaTypeConverter());
        return m;
    }

    private Datastore initDatastore(MongoClient cli, String dbn)
    {
        Datastore ds = _morphia.createDatastore(cli, dbn);
        ds.ensureIndexes();
        return ds;
    }

    private void update(Resource r, Map tiers)
    {
        String id = getID(r.getURI());

        TierEntry entry = _ds.find(TierEntry.class).field(F_ID).equal(id).get();
        if ( entry == null ) { _ds.save(createTierEntry(r, tiers)); }

        _ds.updateFirst(_ds.find(TierEntry.class).field(F_ID).equal(id)
                      , _ds.createUpdateOperations(TierEntry.class)
                           .set(F_TIER, tiers));
    }

    protected boolean exists(String uri)
    {
        String id = getID(uri);
        return (_ds.exists(new Key(TierEntry.class, "tiers", id)) != null );
    }

    private TierEntry createTierEntry(Resource r, Map<String,Integer> tiers)
    {
        String id = getID(r.getURI());
        Model  m  = r.getModel();
        Resource proxy = getProxy(m, "false");
        Resource eaggr = getResource(m, EDM.EuropeanaAggregation);
        Resource aggr  = getResource(m, ORE.Aggregation);
        return new TierEntry(id, getDataset(id), new Date(), tiers
                           , getMediaType(proxy), getCountry(eaggr)
                           , getProvider(aggr), getDataProvider(aggr));
    }

    private class TierUpdateHandler implements ResourceCallback<Resource>
    {
        public void handle(String id, Resource r
                         , eu.europeana.ld.ResourceCallback.Status s)
        {
            Model m = r.getModel();
            Map<String,Integer> tiers = new HashMap();
            for ( TierClassifierAlgorithm c : _classifiers )
            {
                tiers.put(c.getLabel(), c.classify(m));
            }
            update(r, tiers);
        }
    }
}
