package com.cmacgm.model;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name="application")
public class Application {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	@Column(name = "APPLICATION_NAME")
	private String applicationName;
	@Column(name = "DESCRIPTION")
	private String description;
	@Column(name = "LAST_UPDATED_TIME")
	private Date lastUpdatedTime;
	@Column(name = "CREATED_BY")
	private String createdBy;
	@Column(name = "ENABLE")
	private boolean enable;
	@Column(name = "LAST_SYNC_TIME")
	private Date lastSyncTime;
	// how often the job runs
	@Column(name = "SYNC_JOB_RATE")
	private long syncJobRate = 3600000l;
	// delay from application startup that the job starts
	@Column(name = "SYNC_JOB_INITIAL_DELAY")
	private long syncJobInitialDelay = 100000l;
	@Column(name = "CREATED_ON")
	private Date createdOn;
	@ManyToMany(fetch=FetchType.EAGER,cascade = CascadeType.ALL)
	@JoinTable(name = "app_appurl", joinColumns = @JoinColumn(name = "app_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "app_url_id", referencedColumnName = "id"))
	@JsonIgnore
	@OrderBy("id ASC")
	private Set<ApplicationUrl> applicationUrl = new LinkedHashSet<>();
	@JsonManagedReference
	@ManyToMany(mappedBy = "applications",fetch=FetchType.EAGER,cascade = CascadeType.ALL)
	public Set<Users> users;
	@Column(name = "fromEmailAddress")
	private String fromEmailAddress;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
	public boolean isEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public Date getLastSyncTime() {
		return lastSyncTime;
	}
	public void setLastSyncTime(Date lastSyncTime) {
		this.lastSyncTime = lastSyncTime;
	}
	public long getSyncJobRate() {
		return syncJobRate;
	}
	public void setSyncJobRate(long syncJobRate) {
		this.syncJobRate = syncJobRate;
	}
	public long getSyncJobInitialDelay() {
		return syncJobInitialDelay;
	}
	public void setSyncJobInitialDelay(long syncJobInitialDelay) {
		this.syncJobInitialDelay = syncJobInitialDelay;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	public Set<ApplicationUrl> getApplicationUrl() {
		return applicationUrl;
	}
	public void setApplicationUrl(Set<ApplicationUrl> applicationUrl) {
		this.applicationUrl = applicationUrl;
	}
	public Set<Users> getUsers() {
		return users;
	}
	public void setUsers(Set<Users> users) {
		this.users = users;
	}
	public String getFromEmailAddress() {
		return fromEmailAddress;
	}
	public void setFromEmailAddress(String fromEmailAddress) {
		this.fromEmailAddress = fromEmailAddress;
	}

	

}
