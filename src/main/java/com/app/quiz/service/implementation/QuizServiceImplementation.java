package com.app.quiz.service.implementation;

import com.app.quiz.dto.QuestionDTO;
import com.app.quiz.dto.QuizDTO;
import com.app.quiz.dto.mapper.QuestionDTOMapper;
import com.app.quiz.dto.mapper.QuizDTOMapper;
import com.app.quiz.entity.*;
import com.app.quiz.exception.custom.InvalidCredentialsException;
import com.app.quiz.exception.custom.InvalidInputException;
import com.app.quiz.exception.custom.ResourceNotFoundException;
import com.app.quiz.repository.*;
import com.app.quiz.requestBody.AnswerResponse;
import com.app.quiz.requestBody.ConfigureQuiz;
import com.app.quiz.service.feedback.FeedbackService;
import com.app.quiz.service.QuizService;
import com.app.quiz.utils.FeedbackResponse;
import com.app.quiz.utils.QuizResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class QuizServiceImplementation implements QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final FeedbackRepository feedbackRepository;
    private final ResponseRepository responseRepository;
    private final RatingRepository ratingRepository;
    private final DifficultyLevelRepository difficultyLevelRepository;
    private final QuizDTOMapper quizDTOMapper;
    private final QuestionDTOMapper questionDTOMapper;
    private final FeedbackService feedbackService;

    @Autowired
    public QuizServiceImplementation(QuizRepository quizRepository,
                                     UserRepository userRepository,
                                     TopicRepository topicRepository,
                                     QuestionRepository questionRepository,
                                     FeedbackRepository feedbackRepository,
                                     FeedbackContentRepository feedbackContentRepository,
                                     ResponseRepository responseRepository,
                                     DifficultyLevelRepository difficultyLevelRepository,
                                     RatingRepository ratingRepository,
                                     QuizDTOMapper quizDTOMapper,
                                     QuestionDTOMapper questionDTOMapper,
                                     FeedbackService feedbackService) {

                                    this.quizRepository = quizRepository;
                                    this.userRepository = userRepository;
                                    this.topicRepository = topicRepository;
                                    this.questionRepository = questionRepository;
                                    this.feedbackRepository = feedbackRepository;
                                    this.responseRepository = responseRepository;
                                    this.difficultyLevelRepository = difficultyLevelRepository;
                                    this.ratingRepository = ratingRepository;
                                    this.quizDTOMapper = quizDTOMapper;
                                    this.questionDTOMapper = questionDTOMapper;
                                    this.feedbackService = feedbackService;
    }


    @Override
    public QuizDTO createQuiz(ConfigureQuiz configureQuiz) {
        Optional<User> user = userRepository.findById(configureQuiz.getUserId());
        if(user.isEmpty()) {
            throw new ResourceNotFoundException("User with "+ configureQuiz.getUserId()+" is not found");
        }

        Optional<Topic> existingTopic = topicRepository.findById(configureQuiz.getTopicId());
        if(existingTopic.isEmpty()) {
            throw new ResourceNotFoundException("Topic with "+ configureQuiz.getTopicId()+" is not found");
        }
        Topic topic = existingTopic.get();

        //Checks and omits questions with isDeleted = true
        long nonDeletedQuestionsCount = topic.getQuestionsList().stream()
                .filter(question -> !Boolean.TRUE.equals(question.getIsDeleted()))
                .count();

        if (nonDeletedQuestionsCount == 0) {
            throw new InvalidInputException("Topic does not have any questions");
        }

        topic.setNumberOfQuestions((int) nonDeletedQuestionsCount);

        Optional<Feedback> feedback = feedbackRepository.findById(configureQuiz.getFeedbackId());
        if(feedback.isEmpty()) {
            throw new ResourceNotFoundException("Feedback with "+ configureQuiz.getFeedbackId()+" is not found");
        }

        if(configureQuiz.getQuestionsLimit() != null && configureQuiz.getQuestionsLimit() > nonDeletedQuestionsCount) {
            throw new InvalidInputException("Requested number of questions exceed available questions for this topic");
        }

        DifficultyLevel difficultyLevel = null;
        if(configureQuiz.getDifficultyLevel() != null) {
            Optional<DifficultyLevel> existingDifficultyLevel = difficultyLevelRepository.findByLevel(configureQuiz.getDifficultyLevel().toUpperCase());
            if(existingDifficultyLevel.isEmpty()) {
                throw new ResourceNotFoundException("Difficult level "+configureQuiz.getDifficultyLevel()+ " is not found");
            }
            else {
                difficultyLevel = existingDifficultyLevel.get();
            }
        }

        Quiz newQuiz = new Quiz(user.get(), topic, feedback.get(), false, configureQuiz.getQuestionsLimit(), difficultyLevel, 0.0, LocalDateTime.now());

       quizRepository.save(newQuiz);

       return quizDTOMapper.apply(newQuiz);
    }



    @Override
    public QuestionDTO startQuiz(Long quizId, Long topicId) {
        Optional<Quiz> existingQuiz = quizRepository.findById(quizId);

        Quiz quiz;
        if(existingQuiz.isEmpty()) {
            throw new ResourceNotFoundException("Quiz with "+quizId+" is not found");
        } else {
            quiz = existingQuiz.get();
        }

        if(quiz.getTopic().getId() != topicId) {
            throw new InvalidInputException("Invalid topic id received as response for the quiz created");
        }

        Optional<Topic> existingTopic = topicRepository.findById(topicId);

        Topic topic;
        if(existingTopic.isEmpty()) {
            throw new ResourceNotFoundException("Topic with "+topicId+" is not found");
        } else {
            topic = existingTopic.get();
        }

        List<Question> questions;
        if(quiz.getDifficultyLevel() != null) {
            // If a difficulty level is specified, only get questions with the matching difficulty level
            questions = topic.getQuestionsList().stream()
                    .filter(question -> question.getDifficultyLevel().getLevel().equalsIgnoreCase(quiz.getDifficultyLevel().getLevel()))
                    .filter(question -> !Boolean.TRUE.equals(question.getIsDeleted())) // Exclude soft-deleted questions
                    .collect(Collectors.toList());
        } else {
            // No difficulty level specified, get all questions
            questions = topic.getQuestionsList().stream()
                    .filter(question -> !Boolean.TRUE.equals(question.getIsDeleted())) // Exclude soft-deleted questions
                    .collect(Collectors.toList());
        }

        if (questions.isEmpty()) {
            throw new ResourceNotFoundException("No questions available for the specified difficulty level");
        }

        // Pick a question randomly
        Question question = questions.get(new Random().nextInt(questions.size()));
        quiz.getServedQuestions().add(question);

        // Check if all questions have been served, and if so, mark the quiz as completed -> QuestionLimit 1.
        Integer effectiveQuestionLimit = quiz.getQuestionsLimit() != null
                ? quiz.getQuestionsLimit()
                : (int) topic.getQuestionsList().stream()
                .filter(question1 -> (!question1.getIsDeleted()))
                .count();
        if (quiz.getServedQuestions().size() >= effectiveQuestionLimit) {
            quiz.setIsCompleted(true);
        }

        quizRepository.save(quiz);

        return questionDTOMapper.apply(question);
    }





    @Override
    public QuestionDTO nextQuestion(AnswerResponse answerResponse) {
        Optional<Quiz> existingQuiz = quizRepository.findById(answerResponse.getQuizId());

        Quiz quiz;
        if(existingQuiz.isEmpty()) {
            throw new ResourceNotFoundException("Quiz with id "+answerResponse.getQuizId()+" is not found");
        } else {
            quiz = existingQuiz.get();
        }

        int receivedSequence = answerResponse.getSequenceNumber();
        Question receivedQuestion = quiz.getServedQuestions().get(receivedSequence);

        //To validate if the answer response contains the correct last served question
        if (!receivedQuestion.equals(questionRepository.findById(answerResponse.getQuestionId()).orElseThrow(
                () -> new ResourceNotFoundException("Question with id "+answerResponse.getQuestionId()+" is not found")))) {
            throw new InvalidInputException("Invalid question received as response");
        }

        // Validate the answer choices
        answerValidation(answerResponse, receivedQuestion);

        // Grading the response
        grading(quiz, receivedQuestion, answerResponse);

        //Adding the response
        Response response = addResponse(quiz, receivedQuestion, answerResponse);
        responseRepository.save(response);

        quizRepository.save(quiz);

        // If the quiz is not completed, get the next question
        Question nextQuestion;
        if (receivedSequence < quiz.getServedQuestions().size() - 1) {
            nextQuestion = quiz.getServedQuestions().get(receivedSequence + 1);
        } else {
            nextQuestion = nextRegularQuestion(quiz);
            // Add the selected question to the servedQuestions list
            quiz.getServedQuestions().add(nextQuestion);
        }

        // Now check if all questions have been served, and if so, mark the quiz as completed
        Integer effectiveQuestionLimit = quiz.getQuestionsLimit();
        if (quiz.getDifficultyLevel() != null) {
            long questionsOfDifficulty = quiz.getTopic().getQuestionsList().stream()
                    .filter(q -> !q.getIsDeleted())
                    .filter(q -> q.getDifficultyLevel().equals(quiz.getDifficultyLevel()))
                    .count();
            effectiveQuestionLimit = effectiveQuestionLimit != null ? Math.min(effectiveQuestionLimit, (int)questionsOfDifficulty) : (int)questionsOfDifficulty;
        } else {
            effectiveQuestionLimit = effectiveQuestionLimit != null
                    ? effectiveQuestionLimit
                    : (int) quiz.getTopic().getQuestionsList().stream()
                    .filter(q -> !q.getIsDeleted())
                    .count();
        }

        if (quiz.getServedQuestions().size() >= effectiveQuestionLimit) {
            quiz.setIsCompleted(true);
        }

        quizRepository.save(quiz);

        QuestionDTO questionDTO = questionDTOMapper.apply(nextQuestion);

        return questionDTO;

    }




    private Question nextRegularQuestion(Quiz quiz) {

        Integer effectiveQuestionLimit = quiz.getQuestionsLimit() != null
                ? quiz.getQuestionsLimit()
                : (int) quiz.getTopic().getQuestionsList().stream().filter(question -> !question.getIsDeleted()).count();

        if (quiz.getServedQuestions().size() >= effectiveQuestionLimit) {
            throw new ResourceNotFoundException("Questions limit reached");
        }
        Topic topic = quiz.getTopic();
        List<Question> servedQuestions = quiz.getServedQuestions();
        List<Question> questionsList = topic.getQuestionsList().stream()
                .filter(question -> !question.getIsDeleted()) // Exclude deleted questions
                .toList();
        List<Question> remainingQuestions;

        // If a difficulty level is specified, only get questions with the matching difficulty level
        if (quiz.getDifficultyLevel() != null) {
            remainingQuestions = questionsList.stream()
                    .filter(question -> !servedQuestions.contains(question))
                    .filter(question -> quiz.getDifficultyLevel() == null || question.getDifficultyLevel().getLevel().equalsIgnoreCase(quiz.getDifficultyLevel().getLevel()))
                    .collect(Collectors.toList());

            if (remainingQuestions.isEmpty()) {
                throw new ResourceNotFoundException("No more questions available for the specified difficulty level");
            }
        } else {
            remainingQuestions = questionsList.stream()
                    .filter(question -> !servedQuestions.contains(question))
                    .collect(Collectors.toList());

            if (remainingQuestions.isEmpty()) {
                throw new ResourceNotFoundException("No more questions available");
            }
        }

        int randomIndex = new Random().nextInt(remainingQuestions.size());
        Question nextQuestion = remainingQuestions.get(randomIndex);

        return nextQuestion;
    }





    @Override
    public FeedbackResponse getFeedback(AnswerResponse answerResponse) {
        Optional<Quiz> existingQuiz = quizRepository.findById(answerResponse.getQuizId());

        Quiz quiz;
        if(existingQuiz.isEmpty()) {
            throw new ResourceNotFoundException("Quiz with id "+answerResponse.getQuizId()+" is not found");
        } else {
            quiz = existingQuiz.get();
        }

        int receivedSequence = answerResponse.getSequenceNumber();
        Question receivedQuestion = quiz.getServedQuestions().get(receivedSequence);

        //To validate if the answer response contains the correct last question
        if (!receivedQuestion.equals(questionRepository.findById(answerResponse.getQuestionId()).orElseThrow(
                () -> new ResourceNotFoundException("Question with id "+answerResponse.getQuestionId()+" is not found")))) {
            throw new InvalidInputException("Invalid question received as response");
        }

        // Validate the answer choices
        answerValidation(answerResponse, receivedQuestion);

        FeedbackResponse feedbackResponse;
        if(quiz.getFeedbackType().getType().equalsIgnoreCase("POST_QUIZ FEEDBACK")) {
            feedbackResponse = null;
        }
        else {
            feedbackResponse = feedbackService.generateFeedback(quiz, receivedQuestion, answerResponse);
        }

        return feedbackResponse;
    }





    private Response addResponse(Quiz quiz, Question question, AnswerResponse answerResponse) {
        // Find the existing response for the current question
        Optional<Response> existingResponse = quiz.getResponses().stream()
                .filter(response -> response.getQuestion().equals(question))
                .findFirst();

        Response response;
        if (existingResponse.isPresent()) {
            // If a response exists, update its choices
            response = existingResponse.get();
            response.getChoices().clear();
            response.getChoices().addAll(answerResponse.getAnswerChoices());
        } else {
            // If a response does not exist, create a new one
            response = new Response(quiz, question);
            response.getChoices().addAll(answerResponse.getAnswerChoices());
            quiz.getResponses().add(response);
        }

        return response;
    }






    private void answerValidation(AnswerResponse answerResponse, Question question) {
        List<Choice> answerChoices = answerResponse.getAnswerChoices();

        // Convert the question's choice IDs to a set for faster lookups
        Set<Long> questionChoiceIds = question.getChoices().stream()
                .map(Choice::getId)
                .collect(Collectors.toSet());

        for (Choice choice : answerChoices) {
            // Check if the choice ID is valid for the question
            if (choice != null && !questionChoiceIds.contains(choice.getId())) {
                throw new InvalidInputException("Invalid choice id for the given question");
            }
        }
    }





    private void grading(Quiz quiz, Question question, AnswerResponse answerResponse) {
        // List of answer choices from the current response
        List<Choice> answerChoices = answerResponse.getAnswerChoices();

        // List of correct choices for this question
        List<Choice> correctChoices = question.getChoices().stream()
                .filter(Choice::isCorrect)
                .collect(Collectors.toList());

        // Count of correct choices in the current response
        int numberOfCorrectAnswerChoices = 0;

        // Count of incorrect choices in the current response
        int numberOfIncorrectAnswerChoices = 0;

        for(Choice answerChoice : answerChoices) {
            if(answerChoice == null) {
                continue;
            }
            if(correctChoices.stream().anyMatch(choice -> choice.getId().equals(answerChoice.getId()))) {
                numberOfCorrectAnswerChoices++;
            } else {
                numberOfIncorrectAnswerChoices++;
            }
        }

        // Score for each correct and incorrect choice
        double scorePerCorrectChoice = (double) question.getScore() / correctChoices.size();
        double scorePerIncorrectChoice = (double) question.getScore() / question.getChoices().size();

        // Score for the current response
        double answerScore = numberOfCorrectAnswerChoices * scorePerCorrectChoice;

        if(numberOfCorrectAnswerChoices == correctChoices.size() && answerChoices.size() > numberOfCorrectAnswerChoices) {
            answerScore = answerScore - (numberOfIncorrectAnswerChoices * scorePerIncorrectChoice);
        }

        if(answerScore < 0) answerScore = 0; // Ensure the score doesn't drop below zero

        // Find the existing response for the current question
        Optional<Response> existingResponseOpt = quiz.getResponses().stream()
                .filter(response -> response.getQuestion().getId().equals(question.getId()))
                .findFirst();

        if (existingResponseOpt.isPresent()) {
            // If a response exists, calculate its score and subtract from the final score
            Response existingResponse = existingResponseOpt.get();

            // Count of correct choices in the existing response
            int existingNumberOfCorrectAnswerChoices = 0;
            int existingNumberOfIncorrectAnswerChoices = 0;

            for(Choice answerChoice : existingResponse.getChoices()) {
                if(answerChoice == null) {
                    continue;
                }
                if(correctChoices.stream().anyMatch(choice -> choice.getId().equals(answerChoice.getId()))) {
                    existingNumberOfCorrectAnswerChoices++;
                } else {
                    existingNumberOfIncorrectAnswerChoices++;
                }
            }

            // Score for the existing response
            double existingAnswerScore = existingNumberOfCorrectAnswerChoices * scorePerCorrectChoice;
            if(existingNumberOfCorrectAnswerChoices == correctChoices.size() && existingResponse.getChoices().size() > existingNumberOfCorrectAnswerChoices) {
                existingAnswerScore = existingAnswerScore - (existingNumberOfIncorrectAnswerChoices * scorePerIncorrectChoice);
            }

            if(existingAnswerScore < 0) existingAnswerScore = 0; // Ensure the score doesn't drop below zero

            // Subtract the score for the existing response from the quiz's final score
            quiz.setFinalScore(quiz.getFinalScore() - existingAnswerScore);
        }

        // Add the score for the current response to the quiz's final score
        quiz.setFinalScore(quiz.getFinalScore() + answerScore);
    }





    @Override
    public void submitQuiz(Long quizId) {
        Optional<Quiz> existingQuiz = quizRepository.findById(quizId);

        Quiz quiz;
        if(existingQuiz.isEmpty()) {
            throw new ResourceNotFoundException("Quiz with "+quizId+" is not found");
        } else {
            quiz = existingQuiz.get();
        }

        if(!quiz.getIsCompleted()) {
            throw new InvalidCredentialsException("Quiz is not completed");
        }

        quiz.setCompletedAt(LocalDateTime.now());

        quizRepository.save(quiz);
    }





    @Override
    public QuizResult getQuizResult(Long quizId) {

        Quiz quiz = fetchQuiz(quizId);
        Long userId = quiz.getUser().getId();

        List<Question> questionsServed = quiz.getServedQuestions();
        List<QuestionDTO> questionsServedDTOs = questionsServed.stream().map(questionDTOMapper).toList();

        Map<Long, List<Choice>> userAnswerChoices = getUserAnswerChoices(quiz, questionsServed);
        Map<Long, List<Choice>> correctAnswerChoices = getCorrectAnswerChoices(questionsServed);
        Map<Long, String> answerExplanation = getAnswerExplanation(questionsServed);

        Integer totalNoOfQuestions = questionsServed.size();
        Double totalNumberOfMarks = getTotalMarks(questionsServed);
        Double finalScore = formatDouble(quiz.getFinalScore());
        double finalPercentage = formatDouble((finalScore / totalNumberOfMarks) * 100);

        Map<String, Double> marksScoredPerSubtopic = getMarksScoredPerSubtopic(quiz, questionsServed);
        Map<String, Double> totalMarksPerSubtopic = getTotalMarksPerSubtopic(questionsServed);
        Map<String, Double> percentageScorePerSubtopic = getPercentageScorePerSubtopic(marksScoredPerSubtopic, totalMarksPerSubtopic);

        // Start the feedback with the overall feedback
        String overallFeedback = feedbackService.overallFeedback(finalPercentage);

        String feedbackBySubTopic = getFeedbackBySubtopic(percentageScorePerSubtopic);

        Optional<Rating> userRating = ratingRepository.findByUserIdAndTopicId(userId, quiz.getTopic().getId());
        boolean isRated = userRating.isPresent();


        QuizResult quizResult = new QuizResult(quiz.getId(),
                                               userId,
                                               quiz.getTopic(),
                                               quiz.getFeedbackType().getType(),
                                               quiz.getIsCompleted(),
                                               totalNoOfQuestions,
                                               quiz.getQuestionsLimit(),
                                               quiz.getDifficultyLevel() != null ? quiz.getDifficultyLevel().getLevel() : null,
                                               totalNumberOfMarks,
                                               finalScore,
                                               finalPercentage,
                                               overallFeedback,
                                               feedbackBySubTopic,
                                               quiz.getCreatedAt(),
                                               quiz.getCompletedAt(),
                                               questionsServedDTOs,
                                               userAnswerChoices,
                                               correctAnswerChoices,
                                               answerExplanation,
                                               marksScoredPerSubtopic,
                                               totalMarksPerSubtopic,
                                               percentageScorePerSubtopic,
                                                isRated
                    );

        return quizResult;
    }


    private Quiz fetchQuiz(Long quizId) {
        Optional<Quiz> existingQuiz = quizRepository.findById(quizId);
        if(existingQuiz.isEmpty()) {
            throw new ResourceNotFoundException("Quiz with "+quizId+" is not found");
        }
        Quiz quiz = existingQuiz.get();
        if(!quiz.getIsCompleted() || quiz.getCompletedAt() == null) {
            throw new InvalidInputException("Quiz is not completed");
        }
        return quiz;
    }


    private Map<Long, List<Choice>> getUserAnswerChoices(Quiz quiz, List<Question> questions) {
        Map<Long, List<Choice>> userAnswerChoices = new HashMap<>();

        for(Question question : questions) {
            List<Response> responses = quiz.getResponses();
            userAnswerChoices.put(question.getId(), responses.stream()
                    .filter(response -> response.getQuestion().equals(question))
                    .flatMap(response -> response.getChoices().stream()) // flatMap to flatten the lists of choices
                    .collect(Collectors.toList())); // collect to get a list
        }

        return userAnswerChoices;
    }

    private Map<Long, List<Choice>> getCorrectAnswerChoices(List<Question> questions) {
        Map<Long, List<Choice>> correctAnswerChoices = new HashMap<>();

        for(Question question : questions) {
            List<Choice> correct = question.getChoices().stream()
                    .filter(Choice::isCorrect)
                    .toList();
            correctAnswerChoices.put(question.getId(), correct);
        }

        return correctAnswerChoices;
    }

    private Map<Long, String> getAnswerExplanation(List<Question> questions) {
        Map<Long, String> answerExplanation = new HashMap<>();

        for(Question question : questions) {
            answerExplanation.put(question.getId(), question.getExplanation());
        }

        return answerExplanation;
    }


    private Double getTotalMarks(List<Question> questions) {
        return questions.stream()
                .mapToDouble(Question::getScore)
                .sum();
    }


    private double formatDouble(Double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(value));
    }

    private Map<String, Double> getMarksScoredPerSubtopic(Quiz quiz, List<Question> questions) {
        return questions.stream()
                .collect(Collectors.groupingBy(
                        question -> question.getSubtopic().getName(),  // Group by subtopic name
                        Collectors.summingDouble(question -> {  // Sum the scores of answered questions
                            List<Response> responses = quiz.getResponses();
                            return responses.stream()
                                    .filter(response -> response.getQuestion().equals(question))
                                    .mapToDouble(response -> {
                                        List<Choice> chosenChoices = response.getChoices();
                                        List<Choice> correctChoices = question.getChoices().stream()
                                                .filter(Choice::isCorrect)
                                                .collect(Collectors.toList());
                                        int numberOfCorrectChosen = (int) chosenChoices.stream()
                                                .filter(correctChoices::contains)
                                                .count();
                                        int numberOfIncorrectChosen = chosenChoices.size() - numberOfCorrectChosen;

                                        double scorePerCorrectChoice = question.getScore() / (double) correctChoices.size();
                                        double scorePerIncorrectChoice = question.getScore() / (double) question.getChoices().size();

                                        double responseScore = numberOfCorrectChosen * scorePerCorrectChoice;
                                        if(numberOfCorrectChosen == correctChoices.size() && chosenChoices.size() > numberOfCorrectChosen) {
                                            responseScore = responseScore - (numberOfIncorrectChosen * scorePerIncorrectChoice);
                                        }

                                        DecimalFormat dfr = new DecimalFormat("#.##");
                                        responseScore = Double.parseDouble(dfr.format(responseScore));

                                        return Math.max(responseScore, 0);
                                    })
                                    .sum();
                        })
                ));
    }

    private Map<String, Double> getTotalMarksPerSubtopic(List<Question> questions) {
        return questions.stream()
                .collect(Collectors.groupingBy(
                        question -> question.getSubtopic().getName(),  // Group by subtopic name
                        Collectors.summingDouble(Question::getScore)  // Sum the scores of all questions
                ));
    }


    private Map<String, Double> getPercentageScorePerSubtopic(Map<String, Double> scored, Map<String, Double> total) {
        Map<String, Double> percentageScorePerSubtopic = new HashMap<>();

        for (Map.Entry<String, Double> entry : scored.entrySet()) {
            String subtopic = entry.getKey();
            double marksScored = entry.getValue();

            // Ensure total marks for the subtopic is not zero to avoid division by zero
            if (total.containsKey(subtopic) && total.get(subtopic) != 0) {
                double totalMarks = total.get(subtopic);
                double percentageScore = (marksScored / totalMarks) * 100;

                // Format the score to 2 decimal places
                DecimalFormat dfs = new DecimalFormat("#.##");
                percentageScore = Double.valueOf(dfs.format(percentageScore));

                percentageScorePerSubtopic.put(subtopic, percentageScore);
            }
        }

        return percentageScorePerSubtopic;
    }


    private String getFeedbackBySubtopic(Map<String, Double> percentageScores) {
        String feedbackBySubTopic="";
        // Add feedback for each subtopic (excluding "General")
        for (Map.Entry<String, Double> entry : percentageScores.entrySet()) {
            String subtopic = entry.getKey();
            Double percentage = entry.getValue();

            if (!subtopic.equals("General")) {  // Skip the "General" subtopic
                feedbackBySubTopic += feedbackService.subtopicFeedback(percentage, subtopic)+" ";
            }
        }
        return feedbackBySubTopic;
    }


}
