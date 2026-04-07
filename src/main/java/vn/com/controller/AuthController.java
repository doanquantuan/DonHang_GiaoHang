package vn.com.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.dto.AuthResponse;
import vn.com.dto.ErrorResponse;
import vn.com.dto.LoginRequest;
import vn.com.entity.User;
import vn.com.repository.UserRepository;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

        if (userOptional.isEmpty() || !userOptional.get().getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Ten dang nhap hoac mat khau khong chinh xac!"));
        }

        User user = userOptional.get();
        if (!user.getIsActive()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Tai khoan cua ban da bi khoa!"));
        }

        session.setAttribute("username", user.getUsername());
        session.setAttribute("fullName", user.getFullName());
        session.setAttribute("role", user.getRole());

        AuthResponse response = new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole());

        return ResponseEntity.ok(response);
    }
}
