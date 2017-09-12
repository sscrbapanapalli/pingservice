package com.cmacgm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.cmacgm.repository.ApplicationRepository;



@Controller
public class HomeController {

    @Autowired
    private ApplicationRepository applicationRepository;


    @RequestMapping("/")
    String index(){
    	 return "redirect:home";
    }
    
    
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String ApplicationList(Model model){
        model.addAttribute("applications", applicationRepository.findAll());
        return "home";
    }

    
    @RequestMapping(value="/getUrlStatus/{id:[\\d]+}")
    public String Application(@PathVariable("id") long id ,Model model){    	
    	if(id!=0l)
        model.addAttribute("application", applicationRepository.findByIdApplication(id));
    	else
    	model.addAttribute("application","");	
        return "app";
    }

   
}