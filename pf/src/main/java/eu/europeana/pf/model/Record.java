/**
 * 
 */
package eu.europeana.pf.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import eu.europeana.pf.media.MediaType;
import eu.europeana.util.EmbeddableMedia;
import eu.europeana.util.Reusability;
import static eu.europeana.util.MongoUtils.*;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 13 Sep 2017
 */
public class Record
{
    private Document    _doc;
    private Document    _aggr;
    private Document    _eaggr;
    private Document    _proxy;


    private Map<String,WebResource> _wrs = new HashMap();

    public Record(Document doc) { _doc = doc; }


    public void setAggregation(Document aggr)           { _aggr  = aggr;  }
    public void setEuropeanaAggregation(Document eaggr) { _eaggr = eaggr; }
    public void setProxy(Document proxy)                { _proxy = proxy; }

    public Document getDocument() { return _doc; }

    public Collection<WebResource> getWebResources() { return _wrs.values(); }
    public WebResource getWebResource(String url) { return _wrs.get(url); }

    public void addWebResource(Document wr, Document twr) 
    { 
        WebResource r = new WebResource(this, wr, twr);
        _wrs.put(r.getURL(), r); 
    }


    public String getRecordID() { return _doc.getString("about"); }

    public MediaType getMediaType()
    {
        return MediaType.getMedia(_proxy.getString("edmType"));
    }

    public String getCountry()
    {
        return getAsString(_eaggr.get("edmCountry"));
    }

    public Reusability getReusability()
    { 
        Reusability r;
        r = Reusability.getReusability(getAsString(_eaggr.get("edmRights")));
        return ( r != null ? r
                           : Reusability.getReusability(getAsString(_aggr.get("edmRights"))));
    }

    public boolean hasReusability(Reusability... ra)
    {
        Reusability r1 = getReusability();
        for ( Reusability r : ra )
        {
            if  ( r.equals(r1) ) { return true; }
        }
        return false;
    }

    public boolean hasThumbnail()
    {
        return true;
        //return !StringUtils.isEmpty(_eaggr.getString("edmPreview"));
    }

    public String getIsShownBy()
    {
        return _aggr.getString("edmIsShownBy");
    }

    public String getEdmObject()
    {
        return _aggr.getString("edmObject");
    }

    public String getLandingPage()
    {
        return _aggr.getString("edmIsShownAt");
    }

    public boolean hasWorkingLandingPage()
    {
        return !StringUtils.isEmpty(getLandingPage());
    }

    public boolean hasEmbeddableObject()
    {
        return EmbeddableMedia.isEmbeddable(getIsShownBy());
    }

    /*
    public boolean isComplete()
    {
        _doc != null && 
    }
    */
}
