# Quizflect
Quiz application.
## Overview
Quizflect is a quiz engine developed to support students learning by providing automated feedback and performance tracking across quizzes. It enables users to create quizzes, participate in quizzes created by others, and contribute questions to collaborative quizzes.
## Features
1. **Quiz Creation:** Users can create their quizzes, customize question types, and set difficulty levels.
2. **Quiz Participation:** Users can engage in quizzes created by other users.
3. **Question Types:** Supported question types include Multiple Choice, Multiple Answers, and True or False.
4. **Question Management:** Users can add or delete questions from their quizzes and contribute questions to quizzes created by others.
5. **Feedback:** Users receive an overall feedback of their performance during the quiz.
    - Depending on the option selected, users can receive:
      - Correct answer feedback after every question,
      - Correct answer feedback with explanation after every question, or
      - Correct answers feedback with or without explanation for all the questions at the end of the quiz.
8. **Performance Tracking:** The system offers visualizations such as bar charts and line charts to display areas of strength and weakness and track performance over quizzes.
9. **Customization Options:** Users can customize the difficulty level, limit the number of questions in a quiz and feedback type they want to receive.
## Backend
This repository contains the backend of the application, which is developed using Java and Spring Boot with PostgreSQL as the database. REST APIs are used to expose endpoints to the frontend, which is developed using React.js. Additionally, Spring Security is employed for authentication and authorization, along with JWT. This follows a layered architecture where each layer has its own responsibilities. The feedback content is stored in the database so that it can be modified without disrupting the code. Feedback is fetched from the database based on the grade percentages for every topic that the user quizzes on, with additional feedback tailored for the subtopics as well.
