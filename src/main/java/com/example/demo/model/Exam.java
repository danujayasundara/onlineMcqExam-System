package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.List;
//import java.util.Set;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Exam {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long examId;
	private String examName;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDateTime dateTime;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDateTime lastUpdate;
	private int duration;
	@Column(name = "e_status") 
    private boolean eStatus;
	private boolean endExamStatus;
	
	@OneToMany(mappedBy = "exam", fetch = FetchType.EAGER)
	@JsonIgnore
	private List<ExamAttempt> examAttemptsExam;
	
	@OneToMany(mappedBy = "exam", fetch = FetchType.EAGER)
	@JsonManagedReference
	private List<Question> questions = new ArrayList<>();
	
	@ManyToOne(fetch = FetchType.EAGER)
	private User user;

	public Exam() {
		super();
	}

	public Exam(String examName, LocalDateTime dateTime, int duration, boolean eStatus,
			List<ExamAttempt> examAttemptsExam, List<Question> questions) {
		super();
		this.examName = examName;
		this.dateTime = dateTime;
		this.duration = duration;
		this.eStatus = eStatus;
		this.examAttemptsExam = examAttemptsExam;
		this.questions = questions;
	}
	
	public Exam(String examName, LocalDateTime dateTime, int duration, boolean eStatus) {
		super();
		this.examName = examName;
		this.dateTime = dateTime;
		this.duration = duration;
		this.eStatus = eStatus;
	}
	
	public Exam(String examName, LocalDateTime lastUpdate, boolean eStatus) {
		super();
		this.examName = examName;
		this.lastUpdate = lastUpdate;
		this.eStatus = eStatus;
	}

	public Long getExam_id() {
		return examId;
	}

	public void setExam_id(Long exam_id) {
		this.examId = exam_id;
	}

	public String getExam_name() {
		return examName;
	}

	public void setExam_name(String examName) {
		this.examName = examName;
	}

	public int getDuration() {
		return duration;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public boolean isE_status() {
		return eStatus;
	}

	public void setE_status(boolean eStatus) {
		this.eStatus = eStatus;
	}

	public List<ExamAttempt> getExamAttemptsExam() {
		return examAttemptsExam;
	}

	public void setExamAttemptsExam(List<ExamAttempt> examAttemptsExam) {
		this.examAttemptsExam = examAttemptsExam;
	}

	public List<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

	public LocalDateTime getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(LocalDateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	public boolean isEndExamStatus() {
		return endExamStatus;
	}

	public void setEndExamStatus(boolean endExamStatus) {
		this.endExamStatus = endExamStatus;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
	    return "Exam{examId=" + examId + "}";
	}
	
	public Exam(String examId) {
        this.examId = Long.parseLong(examId);
    }

}
