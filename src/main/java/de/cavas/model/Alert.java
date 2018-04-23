
package de.cavas.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "alert2")

//@JsonPropertyOrder({
//    "sourceid",
//    "other",
//    "method",
//    "evidence",
//    "pluginId",
//    "cweid",
//    "confidence",
//    "wascid",
//    "description",
//    "messageId",
//    "url",
//    "reference",
//    "solution",
//    "alert",
//    "param",
//    "attack",
//    "name",
//    "risk",
//    "id"
//})


public class Alert {

	
//	@ManyToOne
//	Report report;

	String microserviceName;
	String microservicePort;
	String microserviceIpAddress;
	String microserviceId;
	String timeStamp;
//	private int alertid;
    @JsonProperty("sourceid")
    private String sourceid;
    
    
//    @Column(length=3000)     
    @JsonIgnore
    @JsonProperty("other")
    private String other;
    
    @JsonProperty("method")
    private String method;
    
    @Lob
    @JsonProperty("evidence")
    private String evidence;
    
    @JsonProperty("pluginId")
    private String pluginId;
    
    @JsonProperty("cweid")
    private String cweid;
    
    @JsonProperty("confidence")
    private String confidence;
    
    @JsonProperty("wascid")
    private String wascid;
    
//    @Lob
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("messageId")
    private String messageId;
    
    @Lob
    @JsonProperty("url")
    private String url;
    
    @Lob
    @JsonProperty("reference")
    private String reference;
    
//    @Lob
    @JsonProperty("solution")
    private String solution;
    
    @Lob
    @JsonProperty("alert")
    private String alert;
    
    @Lob
    @JsonProperty("param")
    private String param;
    
    @Lob
    @JsonProperty("attack")
    private String attack;
    @JsonProperty("name")
    private String name;
    @JsonProperty("risk")
    private String risk;
    @JsonProperty("id")
    private int id;
//    @JsonIgnore
//    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    
    
    

    @JsonProperty("sourceid")
    public String getSourceid() {
        return sourceid;
    }

    public Alert(String microserviceName, String microservicePort, String microserviceIpAddress, String microserviceId,
			String timeStamp, String sourceid, String other, String method, String evidence, String pluginId,
			String cweid, String confidence, String wascid, String description, String messageId, String url,
			String reference, String solution, String alert, String param, String attack, String name, String risk,
			int id) {
		super();
		this.microserviceName = microserviceName;
		this.microservicePort = microservicePort;
		this.microserviceIpAddress = microserviceIpAddress;
		this.microserviceId = microserviceId;
		this.timeStamp = timeStamp;
		this.sourceid = sourceid;
		this.other = other;
		this.method = method;
		this.evidence = evidence;
		this.pluginId = pluginId;
		this.cweid = cweid;
		this.confidence = confidence;
		this.wascid = wascid;
		this.description = description;
		this.messageId = messageId;
		this.url = url;
		this.reference = reference;
		this.solution = solution;
		this.alert = alert;
		this.param = param;
		this.attack = attack;
		this.name = name;
		this.risk = risk;
		this.id = id;
	}

	public Alert() {
		// TODO Auto-generated constructor stub
	}

	@JsonProperty("sourceid")
    public void setSourceid(String sourceid) {
        this.sourceid = sourceid;
    }

//    @JsonProperty("other")
//    public String getOther() {
//        return other;
//    }
//
//    @JsonProperty("other")
//    public void setOther(String other) {
//        this.other = other;
//    }

    @JsonProperty("method")
    public String getMethod() {
        return method;
    }

    @JsonProperty("method")
    public void setMethod(String method) {
        this.method = method;
    }

    @JsonProperty("evidence")
    public String getEvidence() {
        return evidence;
    }

    @JsonProperty("evidence")
    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    @JsonProperty("pluginId")
    public String getPluginId() {
        return pluginId;
    }

    @JsonProperty("pluginId")
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    @JsonProperty("cweid")
    public String getCweid() {
        return cweid;
    }

    @JsonProperty("cweid")
    public void setCweid(String cweid) {
        this.cweid = cweid;
    }

    @JsonProperty("confidence")
    public String getConfidence() {
        return confidence;
    }

    @JsonProperty("confidence")
    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    @JsonProperty("wascid")
    public String getWascid() {
        return wascid;
    }

    @JsonProperty("wascid")
    public void setWascid(String wascid) {
        this.wascid = wascid;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("messageId")
    public String getMessageId() {
        return messageId;
    }

    @JsonProperty("messageId")
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("reference")
    public String getReference() {
        return reference;
    }

    @JsonProperty("reference")
    public void setReference(String reference) {
        this.reference = reference;
    }

    @JsonProperty("solution")
    public String getSolution() {
        return solution;
    }

    @JsonProperty("solution")
    public void setSolution(String solution) {
        this.solution = solution;
    }

    @JsonProperty("alert")
    public String getAlert() {
        return alert;
    }

    @JsonProperty("alert")
    public void setAlert(String alert) {
        this.alert = alert;
    }

    @JsonProperty("param")
    public String getParam() {
        return param;
    }

    @JsonProperty("param")
    public void setParam(String param) {
        this.param = param;
    }

    @JsonProperty("attack")
    public String getAttack() {
        return attack;
    }

    @JsonProperty("attack")
    public void setAttack(String attack) {
        this.attack = attack;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("risk")
    public String getRisk() {
        return risk;
    }

    @JsonProperty("risk")
    public void setRisk(String risk) {
        this.risk = risk;
    }

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(int id) {
        this.id = id;
    }
    
    public String getMicroserviceName() {
		return microserviceName;
	}

	public void setMicroserviceName(String microserviceName) {
		this.microserviceName = microserviceName;
	}

	public String getMicroservicePort() {
		return microservicePort;
	}

	public void setMicroservicePort(String microservicePort) {
		this.microservicePort = microservicePort;
	}

	public String getMicroserviceIpAddress() {
		return microserviceIpAddress;
	}

	public void setMicroserviceIpAddress(String microserviceIpAddress) {
		this.microserviceIpAddress = microserviceIpAddress;
	}

	public String getMicroserviceId() {
		return microserviceId;
	}

	public void setMicroserviceId(String microserviceId) {
		this.microserviceId = microserviceId;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public String toString() {
		return "Alert [microserviceName=" + microserviceName + ", microservicePort=" + microservicePort
				+ ", microserviceIpAddress=" + microserviceIpAddress + ", microserviceId=" + microserviceId
				+ ", timeStamp=" + timeStamp + ", sourceid=" + sourceid + ", other=" + other + ", method=" + method
				+ ", evidence=" + evidence + ", pluginId=" + pluginId + ", cweid=" + cweid + ", confidence="
				+ confidence + ", wascid=" + wascid + ", description=" + description + ", messageId=" + messageId
				+ ", url=" + url + ", reference=" + reference + ", solution=" + solution + ", alert=" + alert
				+ ", param=" + param + ", attack=" + attack + ", name=" + name + ", risk=" + risk + ", id=" + id + "]";
	}

//	public Report getReport() {
//		return report;
//	}
//
//	public void setReport(Report report) {
//		this.report = report;
//	}

//
//    @JsonAnyGetter
//    public Map<String, Object> getAdditionalProperties() {
//        return this.additionalProperties;
//    }
//
//    @JsonAnySetter
//    public void setAdditionalProperty(String name, Object value) {
//        this.additionalProperties.put(name, value);
//    }


    
    

}
