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
	@Modifying(clearAutomatically = true)
	@Query("update ApplicationUrl appurl set appurl.status =:status,appurl.statusCode =:statusCode,appurl.description =:description,appurl.lastUpdatedTime =:lastUpdatedTime where appurl.id =:id")
	void update(@Param("status") boolean status,@Param("statusCode") String statusCode,@Param("description") String description,@Param("lastUpdatedTime") Date lastUpdatedTime, @Param("id") Long id);
}




