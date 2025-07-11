package com.fia.project_ecommerce.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.fia.project_ecommerce.service.UserService;

@Controller
public class DashboardController {
    private final UserService userService;
 
     public DashboardController(UserService userService) {
         this.userService = userService;
     }
 
    @GetMapping("/admin")
    public String getDashboard(Model model) {
        model.addAttribute("countUsers", userService.countUsers());
        model.addAttribute("countProducts", userService.countProducts());
        model.addAttribute("countOrders", userService.countOrders());
        return "admin/dashboard/test";
    }
 }
