package com.cmacgm.repository;

import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cmacgm.model.ApplicationUrl;

public interface ApplicationUrlRepository extends JpaRepository<ApplicationUrl, Long> {
 
	@Transactional
	@Modifying
	@Query("update ApplicationUrl appurl set  appurl.retryCount =:retryCount,appurl.status =:status,appurl.statusCode =:statusCode,appurl.description =:description,appurl.lastUpdatedTime =:lastUpdatedTime,appurl.tempStatus =:statusCode where appurl.id =:id")
	void update(@Param("retryCount") Integer retryCount , @Param("status") boolean status,@Param("statusCode") String statusCode,@Param("description") String description,@Param("lastUpdatedTime") Date lastUpdatedTime, @Param("id") Long id);

	
	@Transactional
	@Modifying
	@Query("update ApplicationUrl appurl set  appurl.tempStatus =:tempStatus where appurl.id =:id")
	void updateTempStatus(@Param("tempStatus") String tempStatus ,@Param("id") Long id);


	@Transactional
	@Modifying
	@Query("update ApplicationUrl appurl set appurl.retryCount =:retryCount where appurl.id =:id")
	void updateRetryCount(@Param("retryCount") Integer retryCount , @Param("id") Long id);

	@Query("select appUrl from ApplicationUrl appUrl where  appUrl.id= :id")
	public ApplicationUrl findByIdApplicationUrl(@Param("id") Long id);
}




