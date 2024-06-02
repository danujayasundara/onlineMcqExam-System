package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.ChainedPersistenceExceptionTranslator;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.dao.QuestionDao;
import com.example.demo.dto.AnswerDtoRes;
import com.example.demo.dto.ExamDtoRes;
import com.example.demo.dto.QuesDtoRes;
import com.example.demo.dto.QuestionAndAnswersDTO;
import com.example.demo.dto.QuestionReviewDTO;
import com.example.demo.dto.SelectedAnswerDTO;
//import com.example.demo.dto.QuestionDto;
import com.example.demo.model.Answer;
import com.example.demo.model.Exam;
import com.example.demo.model.ExamAttempt;
import com.example.demo.model.Question;
import com.example.demo.model.SelectedAnswer;
import com.example.demo.model.User;
import com.example.demo.repositories.AnswerRepository;
import com.example.demo.repositories.ExamAttemptRepository;
import com.example.demo.repositories.ExamRepository;
import com.example.demo.repositories.QuestionRepository;
import com.example.demo.repositories.SelectedAnswerRepository;

import ch.qos.logback.core.model.Model;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Service
public class QuestionServiceImpl implements QuestionService{
	
	@Autowired
	private QuestionRepository questionRepository;
	
	@Autowired
	private AnswerRepository answerRepository;
	
	@Autowired
	private ExamRepository examRepository;
	
	@Autowired
	private AnswerServiceImpl answerServiceImpl;
	
	@Autowired
	private ExamAttemptRepository examAttemptRepository;
	
	@Autowired
	private SelectedAnswerRepository selectedAnswerRepository;
	
	@Autowired
	private QuestionDao questionDao;
	
	@Autowired
	private UserServiceImpl userServiceImpl;

	@Override
	  public List<Question> getAllQuestions() { 
		  return questionRepository.findAll(); 
	  }
	
	  //get question by exam
	  public List<Question> getQuestionsByExamId(Exam examId) {
	       return questionRepository.findByExam(examId);
	    }
	  
	  ///
	
	  
	  /*public List<Answer> getAnswersByQuestionId(Question questionId) {
	        return answerRepository.findByQuestionId(questionId);
	    }*/

	@Override
	public Question createQuestion(Question question) {
		return questionRepository.save(question);
	}

	@Override
	public Optional<Question> getQuestionById(Long id) {
		// TODO Auto-generated method stub
		return questionRepository.findById(id);
	}

	@Override
	public Question updateQuestion(Long id, Question question) throws ChangeSetPersister.NotFoundException{
		// TODO Auto-generated method stub
		Optional<Question> theQuestion = this.getQuestionById(id);
		if(theQuestion.isPresent()) {
			Question updatedQUestion = theQuestion.get();
			updatedQUestion.setQuestion_content(question.getQuestion_content());
			updatedQUestion.setAnswers(question.getAnswers());
			return questionRepository.save(updatedQUestion);
		}
		else {
			throw new ChangeSetPersister.NotFoundException();
		}
 		
	}

	@Override
	public void deleteQuestion(Long id) {
		// TODO Auto-generated method stub
		questionRepository.deleteById(id);
	}
	
	@Override
	public List<Long> getQuestionIdsByExamId(Long examId) {
		List<Long> questionIds = questionDao.findQuestionIdsByExamId(examId);
		System.out.println("QuestionId in Service" + questionIds);
		return questionIds;
	}
	
	/*public void saveQuestionWithExamId(String questionContent, Long examId) {
        Optional<Exam> optionalExam = examRepository.findById(examId);
        if (optionalExam.isPresent()) {
            Exam exam = optionalExam.get();

            Question question = new Question();
            question.setQuestion_content(questionContent);
            question.setExam(exam);

            questionRepository.save(question);
        } else {
            throw new IllegalArgumentException("Exam not found with ID: " + examId);
        }
    }*/
	
	/////////
	public Question saveQuestion(Question question) {
        
        Question savedQuestion = questionRepository.save(question);
       
        List<Answer> answers = question.getAnswers();
        for (Answer answer : answers) {
            answer.setQuestion(savedQuestion);
            answerServiceImpl.saveAnswer(answer);
        }
        return savedQuestion;
    }
	
	//save selected answers 
	public void saveSelectedAnswers(@RequestBody List<SelectedAnswerDTO> selectedAnswers, HttpSession session) {
	    Long attemptId = null;
	    
	    for (SelectedAnswerDTO selectedAnswer : selectedAnswers) {
	        SelectedAnswer saveSelectAns = new SelectedAnswer();
	        saveSelectAns.setSelected_answer(selectedAnswer.getSelected_answer());
	        Question questionId = selectedAnswer.getQuestion_id();
	        saveSelectAns.setQuestion_id(questionId);
	        long questionId2 = selectedAnswer.getQuestion_id().getQuestion_id();
	        Answer correctAnsId = answerRepository.findCorrectAnswer(questionId2);
	        saveSelectAns.setAnswer_id(correctAnsId);

	        ExamAttempt examAttempt = examAttemptRepository.findByUserAndExam(selectedAnswer.getUserId(), selectedAnswer.getExamId());
	        if (examAttempt != null) {
	            saveSelectAns.setAttempt_id(examAttempt);
	            selectedAnswerRepository.save(saveSelectAns);
	            attemptId = examAttempt.getAttempt_id(); 
	        } else {
	            System.out.println("ExamAttempt not found for userId: " + selectedAnswer.getUserId() + " and examId: " + selectedAnswer.getExamId());
	        }
	    }

	    if (attemptId != null) {
	        calculateResultUsingAttemptId(attemptId, session);
	    } else {
	        System.out.println("AttemptId not found, result calculation skipped.");
	    }
	}
	
	//calculate result
	public void calculateResultUsingAttemptId(long attemptId, HttpSession session) {
	    ExamAttempt examAttempt = examAttemptRepository.findByAttemptId(attemptId);
	    if (examAttempt == null) {
	        System.out.println("ExamAttempt not found for attemptId: " + attemptId);
	        return;
	    }

	    List<Question> questions = questionRepository.findByExam(examAttempt.getExam());
	    int quesCount = questions.size();
	    List<SelectedAnswer> answers = selectedAnswerRepository.getSelectAnsByAttId(attemptId);
	    int ansCount = 0;
	    for (SelectedAnswer selectAns : answers) {
	        if (selectAns.getSelected_answer().equals(selectAns.getAnswer_id().getAnswer_id())) {
	            ansCount++;
	        }
	    }
	    double resultOrgin = ((double) ansCount / quesCount) * 100;
	    double result = Math.round(resultOrgin * 100.0) / 100.0;
	    session.setAttribute("result", result);

	    String pOrF = result >= 40 ? "Pass" : "Fail";
	    session.setAttribute("passFail", pOrF);

	    String grade = "";
	    if (result >= 80) {
	        grade = "A+";
	    } else if (result >= 70) {
	        grade = "A";
	    } else if (result >= 60) {
	        grade = "B";
	    } else if (result >= 50) {
	        grade = "C";
	    } else if (result >= 40) {
	        grade = "C-";
	    } else {
	        grade = "F";
	    }
	    session.setAttribute("grade", grade);

	    // Display review to student (correct or wrong)
	    List<QuestionReviewDTO> questionReviews = new ArrayList<>();
	    for (Question question : questions) {
	        boolean isAnswerCorrect = false;
	        Answer correctAnswer = answerRepository.findCorrectAnswer(question.getQuestion_id());
	        for (SelectedAnswer selectAnsR : answers) {
	            if (selectAnsR.getQuestion_id().getQuestion_id().equals(question.getQuestion_id())) {
	                if (selectAnsR.getSelected_answer().equals(correctAnswer.getAnswer_id())) {
	                    isAnswerCorrect = true;
	                    break;
	                }
	            }
	        }
	        QuestionReviewDTO questionReviewDTO = new QuestionReviewDTO(question, isAnswerCorrect);
	        questionReviews.add(questionReviewDTO);
	    }
	    session.setAttribute("questionReviews", questionReviews);
	}
	
	//ended exam with no attempt
	public boolean hasExamEndedAndNotAttempted(Long examId, Long userId) {
        Exam exam = examRepository.findByExamId(examId);
        User user = userServiceImpl.getUserById(userId);
        ExamAttempt attempt = examAttemptRepository.findByUserAndExam(user, exam);

        return exam.isEndExamStatus() && (attempt == null || !attempt.getAttempt_status());
    }
	
	//attended exams of user
	public boolean hasUserAttendedExam(long attemptId) {
        ExamAttempt attempt = examAttemptRepository.findByAttemptId(attemptId);
        return attempt != null && attempt.getAttempt_status();
    }

	public QuesDtoRes getQuestionAndAnswersById(Long questionId) throws NotFoundException {
        Optional<Question> optionalQuestion = questionRepository.findById(questionId);

        if (optionalQuestion.isPresent()) {
            Question question = optionalQuestion.get();
            List<Answer> answers = answerRepository.findByQuestion(question);

            QuesDtoRes questionAndAnswerDto = new QuesDtoRes();
            questionAndAnswerDto.setQuestionId(question.getQuestion_id());
            questionAndAnswerDto.setQuestion(question.getQuestion_content());

            List<AnswerDtoRes> answerDtoList = new ArrayList<>();
            for (Answer answer : answers) {
                AnswerDtoRes answerDto = new AnswerDtoRes();
                answerDto.setAnswerId(answer.getAnswer_id());
                answerDto.setAnswer(answer.getAnswer());
                answerDto.setCorrectAnswer(answer.getCorrectAnswer());
                answerDtoList.add(answerDto);
            }

            questionAndAnswerDto.setAnswers(answerDtoList);
            return questionAndAnswerDto;
        } else {
            throw new NotFoundException();
        }
    }

	
	
	
	 
}
