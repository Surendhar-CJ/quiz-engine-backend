package com.app.quiz.controller;

import com.app.quiz.entity.Topic;
import com.app.quiz.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/topics")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TopicController {

    private final TopicService topicService;

    @Autowired
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("")
    public ResponseEntity<List<Topic>> getTopics() {
       return new ResponseEntity<>(topicService.topics(), HttpStatus.OK);
    }
}
