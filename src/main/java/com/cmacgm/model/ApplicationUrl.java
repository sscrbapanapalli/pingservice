package com.cmacgm.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "application_url")
public class ApplicationUrl {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private long id;
	@Column(name = "APP_NAME")
	private String appName;
	@OneToOne(fetch = FetchType.EAGER)
	@JsonIgnore
	@OrderBy("name ASC")
	private ServerType serverType;
	@Column(name = "APPLICATION_URL")
	private String applicationUrl;
	@Column(name = "IP_ADDRESS")
	private String ipAddress;
	@Column(name = "HOST_PORT_NO")
	private String hostPortNo;
	@Column(name = "STATUS_CODE")
	private String statusCode;
	@Column(name = "STATUS")
	private boolean status;
	@Column(name = "DESCRIPTION")
	private String description;
	@Column(name = "CREATED_ON")
	private Date createdOn;
	@Column(name = "LAST_UPDATED_TIME")
	private Date lastUpdatedTime;
	@Column(name = "CREATED_BY")
	private String createdBy;
	@Column(name = "CREATED_ON")
	@JsonManagedReference
	@ManyToMany(mappedBy = "applicationUrl", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public Set<Application> application;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public ServerType getServerType() {
		return serverType;
	}
	public void setServerType(ServerType serverType) {
		this.serverType = serverType;
	}
	public String getApplicationUrl() {
		return applicationUrl;
	}
	public void setApplicationUrl(String applicationUrl) {
		this.applicationUrl = applicationUrl;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getHostPortNo() {
		return hostPortNo;
	}
	public void setHostPortNo(String hostPortNo) {
		this.hostPortNo = hostPortNo;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	public Date getLastUpdatedTime() {
		return lastUpdatedTime;
	}
	public void setLastUpdatedTime(Date lastUpdatedTime) {
		this.lastUpdatedTime = lastUpdatedTime;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Set<Application> getApplication() {
		return application;
	}
	public void setApplication(Set<Application> application) {
		this.application = application;
	}

	
}
