/**
 * 
 */
package eu.europeana.pf.media;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 15 Sep 2017
 */
public enum MediaType
{

    TEXT("TEXT", new TextTierCalculator())
  , IMAGE("IMAGE", new ImageTierCalculator())
  , SOUND("SOUND", new SoundTierCalculator())
  , VIDEO("VIDEO", new VideoTierCalculator())
  , M3D("_3D", new M3DTierCalculator());

    private MediaTierCalculator _calculator;
    private String              _label;

    private MediaType(String label, MediaTierCalculator c)
    { 
        _label      = label;
        _calculator = c;
    }

    public String              getLabel()          { return _label;      }
    public MediaTierCalculator getTierCalculator() { return _calculator; }

    public static MediaType getMedia(String type)
    {
        if ( type == null ) { return null; }

        for ( MediaType mt : values() )
        {
            if ( mt.getLabel().equals(type) ) { return mt; }
        }
        return null;
    }
};