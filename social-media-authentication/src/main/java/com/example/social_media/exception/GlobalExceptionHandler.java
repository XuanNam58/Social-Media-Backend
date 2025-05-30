package com.example.social_media.exception;

import com.example.social_media.dto.response.ApiResponse;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .code(1400)
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(value = FirebaseAuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleFirebaseAuth(FirebaseAuthException e) {
        String message = e.getMessage();
        if (message.contains("invalid-credential"))
            message = "Incorrect email or password. Please try again";
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .code(1401)
                        .message(message)
                        .build());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>builder()
                        .code(1500)
                        .message("An error occurred: " + e.getMessage())
                        .build());
    }
}
