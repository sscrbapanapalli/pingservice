package com.cmacgm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cmacgm.repository.UserRepository;



@Controller
public class UsersController {

    @Autowired
    private UserRepository userRepository;

   
    
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String UsersList(Model model){
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    @RequestMapping(value="/createUser", method = RequestMethod.POST)
    @ResponseBody
    public long createUser(@RequestParam String name, @RequestParam String email){
 
        /*Users user = new Users();
        user.setName(name);
        user.setCreatedOn(new Date());
        user.setEmail(email);             
        userRepository.save(user);*/

        return 0l;// user.getId();
    }

    @RequestMapping(value = "/removeUser", method = RequestMethod.POST)
    @ResponseBody
    public String removeUser(@RequestParam Long Id){
    	userRepository.delete(Id);
        return Id.toString();
    }
}