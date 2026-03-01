package com.example.Project2.controller;

import com.example.Project2.dto.request.LoginRequest;
import com.example.Project2.dto.request.RefreshTokenRequest;
import com.example.Project2.dto.request.UserRequest;
import com.example.Project2.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth/")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> RegisterAccount(@RequestBody UserRequest userRequest){

        try{
            String result = authService.register(userRequest);
            if(result.equals("Fail")){
                return new ResponseEntity<>("Error" , HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(result , HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage() , HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> LoginAccount(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> RefreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest){
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> Logout(@RequestParam String username){
        System.out.println("AAAAAAA");
        try{
            authService.logout(username);
            return new ResponseEntity<>("Success" , HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage() , HttpStatus.EXPECTATION_FAILED);
        }
    }
}
