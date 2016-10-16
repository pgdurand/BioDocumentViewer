package bzh.plealog.bioinfo.docviewer.service.ebi.model.search;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="indexInfo" type="{http://www.ebi.ac.uk/ebisearch/schemas/EBISearchRestWS}wsIndexInfo" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "indexInfos", propOrder = { "indexInfo" })
public class WsIndexInfos {

	protected List<WsIndexInfo> indexInfo;

	public List<WsIndexInfo> getIndexInfo() {
		if (indexInfo == null) {
			indexInfo = new ArrayList<WsIndexInfo>(0);
		}
		return indexInfo;
	}

	public void setIndexInfo(List<WsIndexInfo> indexInfo) {
		this.indexInfo = indexInfo;
	}

}
