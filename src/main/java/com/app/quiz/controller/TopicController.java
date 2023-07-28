package com.app.quiz.controller;

import com.app.quiz.dto.TopicDTO;
import com.app.quiz.entity.Rating;
import com.app.quiz.requestBody.TopicCreation;
import com.app.quiz.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<TopicDTO>> getTopics() {
       return new ResponseEntity<>(topicService.topics(), HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<List<TopicDTO>> createTopic(@RequestBody TopicCreation topicCreation) {
        return new ResponseEntity<>(topicService.createTopic(topicCreation), HttpStatus.CREATED);
    }

    @DeleteMapping("/{topicId}/{userId}")
    public ResponseEntity<Void> deleteTopic(@PathVariable("topicId") Long topicId, @PathVariable("userId") Long userId) {
        topicService.deleteTopicById(topicId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/rate-topic")
    public ResponseEntity<Double> rateTopic(@RequestBody Rating rating) {
        return new ResponseEntity<>(topicService.rateTopic(rating), HttpStatus.OK);
    }

}
