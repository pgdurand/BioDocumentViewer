package bzh.plealog.bioinfo.docviewer.service.ebi.model.search;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="referenceFacet" type="{http://www.ebi.ac.uk/ebisearch/schemas/EBISearchRestWS}wsFacet" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "referenceFacets", propOrder = { "referenceFacets" })
public class WsReferenceFacets {

	@XmlElement(name = "referenceFacet")
	protected List<WsFacet> referenceFacets;

	public List<WsFacet> getReferenceFacets() {
		if (referenceFacets == null) {
			referenceFacets = new ArrayList<WsFacet>(0);
		}
		return referenceFacets;
	}

	public void setReferenceFacets(List<WsFacet> referenceFacets) {
		this.referenceFacets = referenceFacets;
	}

}
