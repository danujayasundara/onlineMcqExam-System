package com.example.demo.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AnswerWrapper;
import com.example.demo.dto.ExamAttemptDto;
import com.example.demo.dto.ExamDto;
import com.example.demo.dto.ExamDtoRes;
import com.example.demo.dto.QuesDtoRes;
import com.example.demo.dto.QuestionAndAnswersDTO;
import com.example.demo.dto.QuestionReviewDTO;
import com.example.demo.dto.SelectedAnswerDTO;
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
import com.example.demo.repositories.UserRepository;
import com.example.demo.service.AnswerServiceImpl;
import com.example.demo.service.ExamService;
import com.example.demo.service.ExamServiceImpl;
import com.example.demo.service.QuestionService;
import com.example.demo.service.QuestionServiceImpl;
import com.example.demo.service.UserServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//import io.jsonwebtoken.lang.Collections;
import jakarta.servlet.http.HttpSession;


@Controller
//@CrossOrigin("*")
//@RequestMapping("/question")
public class QuestionController {
	
	
	@Autowired 
	private QuestionServiceImpl questionServiceImpl;
	
	@Autowired
	private QuestionRepository questionRepository;
	
	@Autowired
	private QuestionService questionService;
	  
	@Autowired 
	private ExamServiceImpl examServiceImpl;
	  
	@Autowired 
	private AnswerServiceImpl answerServiceImpl;
	
	@Autowired
	private ExamRepository examRepository;
	
	@Autowired
	private SelectedAnswerRepository selectedAnswerRepository;
	
	@Autowired
	private AnswerRepository answerRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ExamAttemptRepository examAttemptRepository;
	
	@Autowired
	private ExamService examService;
	
	@Autowired
	private UserServiceImpl userServiceImpl;
	 
	@GetMapping("/newExam")
    public String showNewExamPage(@RequestParam("examId") Long examId, Model model) {
        
        Exam exam = examServiceImpl.getExamById(examId); 
        if (exam == null) {   
            return "redirect:/exam";
        }

        model.addAttribute("exam", exam);
        
        List<Question> questions = questionServiceImpl.getQuestionsByExamId(exam);
        model.addAttribute("questions", questions);
        
        
        return "newExamTeacher"; 
    }
	
	/*@PostMapping("/newExam")
	//@ResponseBody
	public String saveExamDetails(@ModelAttribute("exam") Exam exam, @RequestParam("examId") Long examId, 
			//@RequestBody QuestionAndAnswersDTO questionAndAnswersDTO,
			Model model) {
       

        Optional<Exam> existingExamOptional = examRepository.findById(examId);
        
        if (existingExamOptional.isPresent()) {
            // Update existing exam 
            Exam existingExam = existingExamOptional.get();
            existingExam.setDuration(exam.getDuration());
            existingExam.setDateTime(exam.getDateTime());
            existingExam.setE_status(true); 

           
            examRepository.save(existingExam);
            
           // Long questionId = questionServiceImpl.saveQuestionAndAnswers(questionAndAnswersDTO);
           
            //model.addAttribute("questionId", questionId);
            return "newExamTeacher";
        } else {
            
            model.addAttribute("error", "Exam not found");
            return "error"; 
        }
       
        
        //return "newExam";
    }*/
	

	  @GetMapping("/{examId}/questions")
	  public String getQuestionsByExamId(@PathVariable Exam examId,@RequestParam(defaultValue = "0") int currentQuestionIndex, Model model) {
		 
	      List<Question> questions = questionServiceImpl.getQuestionsByExamId(examId);
	      
	      if (currentQuestionIndex >= questions.size()) {
	          return "redirect:/error";
	      }
	      
	      Question currentQuestion = questions.get(currentQuestionIndex);
	      List<Answer> currentAnswers = answerServiceImpl.getAnswersByQuesId(currentQuestion.getQuestion_id());
	      
	      model.addAttribute("currentQuestion", currentQuestion);
	      model.addAttribute("currentAnswers", currentAnswers);
	      model.addAttribute("currentQuestionIndex", currentQuestionIndex);
	      model.addAttribute("totalQuestions", questions.size());
	      model.addAttribute("examId", examId);
	      
	      return "exampaper";
	  }
	  
	  
	  @GetMapping("/{examId}/questionIds")
	  public ResponseEntity<?> getQuestionIdByExamId(@PathVariable Long examId, @RequestParam(defaultValue = "0") int currentQuestionIndex) {
	      Exam exam = examServiceImpl.getExamById(examId);
		  List<Question> questions = questionServiceImpl.getQuestionsByExamId(exam);

	      if (currentQuestionIndex >= questions.size()) {
	          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid question index"));
	      }

	      Long questionId = questions.get(currentQuestionIndex).getQuestion_id();

	      return ResponseEntity.ok(Map.of("questionId", questionId, "currentQuestionIndex", currentQuestionIndex, "totalQuestions", questions.size()));
	  }
	  
	  
	  @PostMapping("/saveanswers")
	  public String saveSelectedAnswers(@RequestBody List<SelectedAnswerDTO> selectedAnswers, HttpSession session) {
		  questionServiceImpl.saveSelectedAnswers(selectedAnswers, session);
		  return "sturesult";
	  }
	  
	//display result page
	  @GetMapping("/result/{examId}/{userId}")
	  public String displayResult(@PathVariable("examId") Long examId, @PathVariable("userId") Long userId, Model model, HttpSession session) {
	      Exam exam = examServiceImpl.getExamById(examId);
	      User user = userServiceImpl.getUserById(userId);
	      ExamAttempt examAttempt = examAttemptRepository.findByUserAndExam(user, exam);

	      if (examAttempt == null) {
	          model.addAttribute("result", "Exam attempt not found");
	          return "sturesult";
	      }

	      Long attId = examAttempt.getAttempt_id();
	      boolean examEndedAndNotAttempted = questionServiceImpl.hasExamEndedAndNotAttempted(examId, userId);
	      boolean userAttendedExam = questionServiceImpl.hasUserAttendedExam(attId);

	      // Redirect to result page if the exam ended and not attempted or if user attended
	      if (examEndedAndNotAttempted || userAttendedExam) {
	          // Call the calculateResultUsingAttemptId method
	          questionServiceImpl.calculateResultUsingAttemptId(attId, session);

	          // Add attributes to the model from the session
	          model.addAttribute("result", session.getAttribute("result"));
	          model.addAttribute("passFail", session.getAttribute("passFail"));
	          model.addAttribute("grade", session.getAttribute("grade"));
	          model.addAttribute("questionReviews", session.getAttribute("questionReviews"));

	          return "sturesult"; 
	      }

	      return "redirect:/" + examId + "/questions?examId=" + examId + "&userId=" + userId;
	  }
	  
	  @GetMapping("/hasExamEndedAndNotAttempted")
	    public ResponseEntity<Map<String, Boolean>> hasExamEndedAndNotAttempted(
	            @RequestParam Long examId, @RequestParam Long userId) {
	        boolean result = questionServiceImpl.hasExamEndedAndNotAttempted(examId, userId);
	        Map<String, Boolean> response = Collections.singletonMap("result", result);
	        return ResponseEntity.ok(response);
	    }
	  
	  @GetMapping("/hasUserAttendedExam")
	  public ResponseEntity<Map<String, Boolean>> hasUserAttendedExam(@RequestParam Long examId, @RequestParam Long userId) {
		  Exam exam = examServiceImpl.getExamById(examId);
	      User user = userServiceImpl.getUserById(userId);
	      ExamAttempt examAttempt = examAttemptRepository.findByUserAndExam(user, exam);
	      Long attemptId = examAttempt.getAttempt_id();
	      boolean result = questionServiceImpl.hasUserAttendedExam(attemptId);
	      Map<String, Boolean> response = Collections.singletonMap("result", result);
	      return ResponseEntity.ok(response);
	  }
	  
	  //questionId by examId
	  @GetMapping("exam/{examId}/questions")
	  public ResponseEntity<List<Long>> getQuestionIdsByExamId(@PathVariable Long examId) {
		    List<Long> questionIds = questionServiceImpl.getQuestionIdsByExamId(examId);
		    System.out.println("Question IDs: " + questionIds);

		    return new ResponseEntity<>(questionIds, HttpStatus.OK);
		}

	  
	//update the question
		 
	@PutMapping("/{id}/update") 
	public Question updateQuestion(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
		Question updatedQuestion =questionService.updateQuestion(id, null); return updatedQuestion;
	}
		 
	//delete question
		 
	@DeleteMapping("/{id}/delete")
	public void deleteQuestion(@PathVariable Long id) {
		questionService.deleteQuestion(id); 
	}
	
	//send question details
	  @GetMapping("/getQuestionAndAnswersById/{questionId}")
	    public ResponseEntity<QuesDtoRes> getExamDetails(@PathVariable Long questionId, Model model) {
	       
	            QuesDtoRes quesDto;
				try {
					quesDto = questionServiceImpl.getQuestionAndAnswersById(questionId);
					System.out.println("Questions *****" +quesDto);
		            model.addAttribute("quesDto", quesDto);
		            return ResponseEntity.ok(quesDto);
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return ResponseEntity.notFound().build();
				}
	            
	            //return "newExamTeacher";
	        
	    }

}
