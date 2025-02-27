package springboot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springboot.LoggedIn;
import springboot.User;
import springboot.service.AccountService;
import springboot.service.AuthenticationService;
import springboot.UserType;

import java.util.ArrayList;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/auth")
public class AuthenticationController {

    //data should contain username, password
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody String[] data) {

        UserType userType = AuthenticationService.authenticate(data[0], data[1]);

        if (userType == null) {
            return ResponseEntity.badRequest().body("Invalid username or password.");
        }
        LoggedIn.logIn(data[0], userType);

        return ResponseEntity.ok(new String[]{"User authenticated successfully as " + data[0] + " " + data[1], userType.toString()});
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers() {
        ArrayList<User> users = null;
            users = (ArrayList<User>) AccountService.readInAllUsers();

        return ResponseEntity.ok(users);

    }

}
