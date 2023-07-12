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
    private final QuizDTOMapper quizDTOMapper;
    private final QuestionDTOMapper questionDTOMapper;
    private final FeedbackService feedbackService;

    @Autowired
    public QuizServiceImplementation(QuizRepository quizRepository,
                                     UserRepository userRepository,
                                     TopicRepository topicRepository,
                                     QuestionRepository questionRepository,
                                     FeedbackRepository feedbackRepository,
                                     ResponseRepository responseRepository,
                                     QuizDTOMapper quizDTOMapper,
                                     QuestionDTOMapper questionDTOMapper,
                                     FeedbackService feedbackService) {

                                    this.quizRepository = quizRepository;
                                    this.userRepository = userRepository;
                                    this.topicRepository = topicRepository;
                                    this.questionRepository = questionRepository;
                                    this.feedbackRepository = feedbackRepository;
                                    this.responseRepository = responseRepository;
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
        topic.setNumberOfQuestions(topic.getQuestionsList().size());

        Optional<Feedback> feedback = feedbackRepository.findById(configureQuiz.getFeedbackId());
        if(feedback.isEmpty()) {
            throw new ResourceNotFoundException("Feedback with "+ configureQuiz.getFeedbackId()+" is not found");
        }

        Quiz newQuiz = new Quiz(user.get(), topic, feedback.get(), false, 0.0, LocalDateTime.now());

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

        // Pick first question
        List<Question> questions = topic.getQuestionsList();
        Question question = questions.get(new Random().nextInt(questions.size()));
        quiz.getServedQuestions().add(question);
        quizRepository.save(quiz);

        return questionDTOMapper.apply(question);


        /* //This is for adaptive quizzing
        *
        * for(Question q : questions) {
            if(q.getDifficultyLevel().getLevel().equalsIgnoreCase("Easy")) {
                quiz.getServedQuestions().add(q);
                quizRepository.save(quiz);
                return q;
            }
        }

        throw new ResourceNotFoundException("No easy questions available"); */
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

        //To validate if the answer response contains the correct last question
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
        if (quiz.quizCompleted()) {
            quiz.setIsCompleted(true);
        }

        quizRepository.save(quiz);

        QuestionDTO questionDTO = questionDTOMapper.apply(nextQuestion);

        return questionDTO;
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
        if(quiz.getFeedbackType().getType().equalsIgnoreCase("DELAYED_ELABORATED")) {
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


    //For Regular quiz
    private Question nextRegularQuestion(Quiz quiz) {

            Topic topic = quiz.getTopic();
            List<Question> servedQuestions = quiz.getServedQuestions();
            List<Question> questionsList = topic.getQuestionsList();
            List<Question> remainingQuestions = questionsList.stream()
                    .filter(question -> !servedQuestions.contains(question))
                    .toList();

        if (remainingQuestions.isEmpty()) {
            throw new ResourceNotFoundException("No more questions available");
        }
        int randomIndex = new Random().nextInt(remainingQuestions.size());
        Question nextQuestion = remainingQuestions.get(randomIndex);

        return nextQuestion;
    }


    private void answerValidation(AnswerResponse answerResponse, Question lastQuestion) {
        // Check if last question was answered correctly
        List<Choice> answerChoices = answerResponse.getAnswerChoices();

        // Convert the last question's choice IDs to a set for faster lookups
        Set<Long> lastQuestionChoiceIds = lastQuestion.getChoices().stream()
                .map(Choice::getId)
                .collect(Collectors.toSet());

        for (Choice choice : answerChoices) {
            // Check if the choice ID is valid for the last question
            if (choice != null && !lastQuestionChoiceIds.contains(choice.getId())) {
                throw new InvalidInputException("Invalid choice id for the given question");
            }
        }
    }


    private void grading(Quiz quiz, Question question, AnswerResponse answerResponse) {

        List<Choice> answerChoices = answerResponse.getAnswerChoices();
        List<Choice> correctChoices = question.getChoices().stream().filter((choice) -> choice.isCorrect() == true).toList();

        int numberOfCorrectAnswerChoices = 0;
        for(Choice correctChoice : correctChoices) {
            for(Choice answerChoice : answerChoices) {
                if(answerChoice == null) {
                    continue;
                }
                if(correctChoice.getId().equals(answerChoice.getId())) {
                    numberOfCorrectAnswerChoices++;
                }
            }
        }

        double answerScore = ((double) numberOfCorrectAnswerChoices / correctChoices.size()) * question.getScore();

        quiz.setFinalScore(quiz.getFinalScore() + answerScore);
    }


    public QuizResult finishQuiz(Long quizId) {
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

        Long userId = quiz.getUser().getId();


        List<Question> questionsServed = quiz.getServedQuestions();
        List<QuestionDTO> questionsServedDTOs = questionsServed.stream().map(questionDTOMapper).toList();

        Map<Long, List<Choice>> userAnswerChoices = new HashMap<>();
        Map<Long, List<Choice>> correctAnswerChoices = new HashMap<>();
        Map<Long, String> answerExplanation = new HashMap<>();


        for(Question question : questionsServed) {
            List<Response> responses = quiz.getResponses();
            userAnswerChoices.put(question.getId(), responses.stream()
                    .filter(response -> response.getQuestion().equals(question))
                    .flatMap(response -> response.getChoices().stream()) // flatMap to flatten the lists of choices
                    .collect(Collectors.toList())); // collect to get a list
        }


        for(Question question : questionsServed) {
            List<Choice> correct = question.getChoices().stream().filter(choice -> choice.isCorrect()).toList();
            correctAnswerChoices.put(question.getId(), correct);
        }

        for(Question question : questionsServed) {
            answerExplanation.put(question.getId(), question.getExplanation());
        }

        Integer totalNoOfQuestions = quiz.getServedQuestions().size();
        Double totalNumberOfMarks = quiz.getServedQuestions().stream()
                                                        .map(question -> question.getScore())
                                                        .reduce(0.0, (a, b) -> a + b);
        Double finalScore = quiz.getFinalScore() ;
        double finalPercentage = ((double) finalScore/totalNumberOfMarks) * 100;
        DecimalFormat df = new DecimalFormat("#.##");
        finalPercentage = Double.valueOf(df.format(finalPercentage));


        QuizResult quizResult = new QuizResult(quiz.getId(), userId, quiz.getTopic(),quiz.getIsCompleted(), totalNoOfQuestions, totalNumberOfMarks, finalScore, finalPercentage, quiz.getCreatedAt(), questionsServedDTOs, userAnswerChoices, correctAnswerChoices, answerExplanation);

        return quizResult;
    }










  /*  private Question nextDifficultyBasedQuestion(Quiz quiz, AnswerResponse answerResponse, Question lastQuestion) {

        // Check if last question was answered correctly
        List<Choice> answerChoices = answerResponse.getAnswerChoices();

        // Convert the last question's choices to a set for faster lookups
        Set<Choice> lastQuestionChoices = new HashSet<>(lastQuestion.getChoices());

        for(Choice choice : answerChoices) {
            Optional<Choice> databaseChoiceOptional = choiceRepository.findById((choice.getId()));
            if (databaseChoiceOptional.isEmpty()) {
                throw new ResourceNotFoundException("Choice with id " + choice.getId() + " is not found");
            } else {
                Choice databaseChoice = databaseChoiceOptional.get();
                // Check if the choice is valid for the last question
                if (!lastQuestionChoices.contains(databaseChoice)) {
                    throw new InvalidInputException("Invalid choice id for the given question");
                }
            }
        }


        Topic topic = quiz.getTopic();
        List<Question> servedQuestions = quiz.getServedQuestions();
        List<Question> questionsList = topic.getQuestionsList();
        Question nextQuestion = questionsList.stream()
                .filter(question -> question.getDifficultyLevel().equals(quiz.difficultyLevel))
                .filter(question -> !servedQuestions.contains(question))
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException("No more questions available"));

        return nextQuestion;

    }

    private String getNextDifficultyLevel(String currentDifficulty) {
        switch (currentDifficulty.toLowerCase()) {
            case "easy":
                return "medium";
            case "medium":
                return "hard";
            default:
                return "hard";
        }
    }

    private String getPreviousDifficultyLevel(String currentDifficulty) {
        switch (currentDifficulty.toLowerCase()) {
            case "hard":
                return "medium";
            case "medium":
                return "easy";
            default:
                return "easy";
        }
    }

    private Question nextAdaptiveQuestion(Quiz quiz, AnswerResponse answerResponse, Question lastQuestion) {

        // Check if last question was answered correctly
        List<Choice> answerChoices = answerResponse.getAnswerChoices();
        List<Choice> databaseChoices = new ArrayList<>();


        // Convert the last question's choices to a set for faster lookups
        Set<Choice> lastQuestionChoices = new HashSet<>(lastQuestion.getChoices());

        for(Choice choice : answerChoices) {
            Optional<Choice> databaseChoiceOptional = choiceRepository.findById((choice.getId()));
            if(databaseChoiceOptional.isEmpty()) {
                throw new ResourceNotFoundException("Choice with id "+choice.getId()+" is not found");
            } else {
                Choice databaseChoice = databaseChoiceOptional.get();

                // Check if the choice is valid for the last question
                if (!lastQuestionChoices.contains(databaseChoice)) {
                    throw new InvalidInputException("Invalid choice id for the given question");
                }

                databaseChoices.add(databaseChoice);
            }
        }

        boolean isCorrect = true; // Assume all choices are correct
        for (Choice choice : databaseChoices) {
            if (!choice.isCorrect()) {
                isCorrect = false; // If any choice is not correct, set isCorrect to false
                break; // No need to check the rest of the choices
            }
        }

        // Select next question based on performance
        String currentDifficulty = lastQuestion.getDifficultyLevel().getLevel();
        String nextDifficulty;
        if (isCorrect) {
            nextDifficulty = getNextDifficultyLevel(currentDifficulty);
        } else {
            nextDifficulty = getPreviousDifficultyLevel(currentDifficulty);
        }

        Topic topic = quiz.getTopic();
        List<Question> allQuestions = topic.getQuestionsList();
        Question nextQuestion = allQuestions.stream()
                .filter(question -> question.getDifficultyLevel().getLevel().equalsIgnoreCase(nextDifficulty))
                .filter(question -> !quiz.getServedQuestions().contains(question)) // make sure question hasn't been served before
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No more questions available"));

        return nextQuestion;
    } */


    //Without using database for choices

    /*private Question nextAdaptiveQuestion(Quiz quiz, AnswerResponse answerResponse, Question lastQuestion) {

        // Check if last question was answered correctly
        List<Choice> answerChoices = answerResponse.getAnswerChoices();
        List<Choice> databaseChoices = new ArrayList<>();

        // Convert the last question's choices to a map for faster lookups
        Map<Long, Choice> lastQuestionChoicesMap = lastQuestion.getChoices().stream()
                .collect(Collectors.toMap(Choice::getId, Function.identity()));

        for(Choice choice : answerChoices) {
            Choice databaseChoice = lastQuestionChoicesMap.get(choice.getId());

            if(databaseChoice == null) {
                throw new ResourceNotFoundException("Choice with id "+choice.getId()+" is not found");
            } else {
                // Check if the choice is valid for the last question
                if (!lastQuestionChoicesMap.containsKey(databaseChoice.getId())) {
                    throw new InvalidInputException("Invalid choice id for the given question");
                }

                databaseChoices.add(databaseChoice);
            }
        }

        boolean isCorrect = true; // Assume all choices are correct
        for (Choice choice : databaseChoices) {
            if (!choice.isCorrect()) {
                isCorrect = false; // If any choice is not correct, set isCorrect to false
                break; // No need to check the rest of the choices
            }
        }
        // Select next question based on performance
        String currentDifficulty = lastQuestion.getDifficultyLevel().getLevel();
        String nextDifficulty;
        if (isCorrect) {
            nextDifficulty = getNextDifficultyLevel(currentDifficulty);
        } else {
            nextDifficulty = getPreviousDifficultyLevel(currentDifficulty);
        }

        Topic topic = quiz.getTopic();
        List<Question> allQuestions = topic.getQuestionsList();
        Question nextQuestion = allQuestions.stream()
                .filter(question -> question.getDifficultyLevel().getLevel().equalsIgnoreCase(nextDifficulty))
                .filter(question -> !quiz.getServedQuestions().contains(question)) // make sure question hasn't been served before
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No more questions available"));

        return nextQuestion;



    } */


}
