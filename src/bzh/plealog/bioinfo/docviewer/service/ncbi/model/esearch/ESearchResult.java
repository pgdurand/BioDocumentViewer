//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.09.29 at 02:23:41 PM CEST 
//


package bzh.plealog.bioinfo.docviewer.service.ncbi.model.esearch;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dataElements",
    "errorList",
    "warningList"
})
@XmlRootElement(name = "eSearchResult")
public class ESearchResult {

    @XmlElements({
        @XmlElement(name = "Count", required = true, type = Count.class),
        @XmlElement(name = "RetMax", required = true, type = RetMax.class),
        @XmlElement(name = "RetStart", required = true, type = RetStart.class),
        @XmlElement(name = "QueryKey", required = true, type = QueryKey.class),
        @XmlElement(name = "WebEnv", required = true, type = WebEnv.class),
        @XmlElement(name = "IdList", required = true, type = IdList.class),
        @XmlElement(name = "TranslationSet", required = true, type = TranslationSet.class),
        @XmlElement(name = "TranslationStack", required = true, type = TranslationStack.class),
        @XmlElement(name = "QueryTranslation", required = true, type = QueryTranslation.class),
        @XmlElement(name = "ERROR", required = true, type = ERROR.class)
    })
    protected List<Object> dataElements;
    @XmlElement(name = "ErrorList")
    protected ErrorList errorList;
    @XmlElement(name = "WarningList")
    protected WarningList warningList;

    /**
     * Gets the value of the dataElements property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataElements property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCountOrRetMaxOrRetStartOrQueryKeyOrWebEnvOrIdListOrTranslationSetOrTranslationStackOrQueryTranslationOrERROR().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Count }
     * {@link RetMax }
     * {@link RetStart }
     * {@link QueryKey }
     * {@link WebEnv }
     * {@link IdList }
     * {@link TranslationSet }
     * {@link TranslationStack }
     * {@link QueryTranslation }
     * {@link ERROR }
     * 
     * 
     */
    public List<Object> getDataElements() {
        if (dataElements == null) {
            dataElements = new ArrayList<Object>();
        }
        return this.dataElements;
    }

    /**
     * Gets the value of the errorList property.
     * 
     * @return
     *     possible object is
     *     {@link ErrorList }
     *     
     */
    public ErrorList getErrorList() {
        return errorList;
    }

    /**
     * Sets the value of the errorList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ErrorList }
     *     
     */
    public void setErrorList(ErrorList value) {
        this.errorList = value;
    }

    /**
     * Gets the value of the warningList property.
     * 
     * @return
     *     possible object is
     *     {@link WarningList }
     *     
     */
    public WarningList getWarningList() {
        return warningList;
    }

    /**
     * Sets the value of the warningList property.
     * 
     * @param value
     *     allowed object is
     *     {@link WarningList }
     *     
     */
    public void setWarningList(WarningList value) {
        this.warningList = value;
    }

}
