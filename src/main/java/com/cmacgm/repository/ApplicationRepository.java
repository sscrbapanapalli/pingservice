package com.cmacgm.repository;

import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cmacgm.model.Application;


public interface ApplicationRepository extends JpaRepository<Application, Long> {


	@Query("select app from Application app where  app.id= :id")
	public Application findByIdApplication(@Param("id") Long id);

	@Transactional
	@Modifying
	@Query("update Application app set app.lastSyncTime =:lastSyncTime where app.id =:id")
	void updateLastSyncTime(@Param("lastSyncTime") Date lastSyncTime, @Param("id") Long id);
}




