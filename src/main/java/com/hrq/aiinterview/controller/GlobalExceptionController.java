package com.hrq.aiinterview.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler(Exception.class)
    public String handle(Exception e, Model model) {
        log.error("系统异常", e);
        model.addAttribute("message", e.getMessage());
        return "error";
    }
}
