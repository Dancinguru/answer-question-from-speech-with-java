package com.pro.speech.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pro.speech.utils.RevAiStreaming;
import com.pro.speech.utils.SpeechUtils;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/")
public class HomeController {
    
    @GetMapping("/")
    public String index() throws Exception {
        String result = MyRevAiStreaming.streamFromLocalFile("static/assets/1.mp3");
        
        return result;
    }
}
