package com.cmacgm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

import com.cmacgm.repository.ApplicationRepository;
import com.cmacgm.repository.ApplicationUrlRepository;
import com.cmacgm.repository.ServerTypeRepository;
import com.cmacgm.repository.UserRepository;

@SpringBootApplication
public class PingApplication extends SpringBootServletInitializer  {

	@Autowired
    UserRepository userRepository;

	@Autowired
    ApplicationRepository applicationRepository;
	
	@Autowired
	ServerTypeRepository serverTypeRepository;
	
	@Autowired
	ApplicationUrlRepository applicationUrlRepository;



	    @Override
	    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
	        return application.sources(PingApplication.class);
	    }

	    public static void main(String[] args) throws Exception {
	        SpringApplication.run(PingApplication.class, args);
	    }


}
