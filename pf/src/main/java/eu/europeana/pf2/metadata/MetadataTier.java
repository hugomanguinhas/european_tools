/**
 * 
 */
package eu.europeana.pf2.metadata;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 30 Nov 2018
 */
public enum MetadataTier
{
    T0("0"), TA("A"), TB("B"), TC("C");

    private String _label;

    private MetadataTier(String label) { _label = label; }

    public String getLabel() { return _label; }
}
