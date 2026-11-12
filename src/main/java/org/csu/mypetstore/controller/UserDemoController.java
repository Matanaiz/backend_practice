package org.csu.mypetstore.controller;

import org.csu.mypetstore.dto.UserReadResult;
import org.csu.mypetstore.service.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
@Profile("dev")
public class UserDemoController {

    private final UserService userService;

    public UserDemoController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/demo/user")
    @ResponseBody
    public ResponseEntity<UserReadResult> getUser(@RequestParam String username) {
        Optional<UserReadResult> result = userService.findByUsername(username);
        return result.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/demo/user/page")
    public String getUserPage(@RequestParam(defaultValue = "alice") String username, Model model) {
        Optional<UserReadResult> result = userService.findByUsername(username);
        if (result.isEmpty()) {
            return "404";
        }
        model.addAttribute("message", "Thymeleaf 初始化成功！");
        model.addAttribute("user", result.get());
        return "demo/user";
    }
}
