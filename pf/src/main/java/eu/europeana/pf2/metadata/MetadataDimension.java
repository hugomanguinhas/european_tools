/**
 * 
 */
package eu.europeana.pf2.metadata;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 30 Nov 2018
 */
public enum MetadataDimension
{
    LANGUAGE("lang", "Language")
  , ENABLING("enabling", "Enabling Elements")
  , CONTEXTUAL("entity", "Contextual Classes")
  , COMBINED("metadata", "Metadata Tier");

    private String _id;
    private String _label;

    private MetadataDimension(String id, String label)
    {
        _id    = id;
        _label = label;
    }

    public String getID()    { return _id;    }
    public String getLabel() { return _label; }
}
