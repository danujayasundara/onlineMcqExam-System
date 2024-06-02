package com.example.demo.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class SendExamDto {
	
	private Long examId;
    private String examName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;
    private int duration;
    private boolean eStatus;
    private boolean endExamStatus;
    
	public SendExamDto() {
		super();
	}

	public SendExamDto(Long examId, String examName, LocalDateTime dateTime, int duration, boolean eStatus,
			boolean endExamStatus) {
		super();
		this.examId = examId;
		this.examName = examName;
		this.dateTime = dateTime;
		this.duration = duration;
		this.eStatus = eStatus;
		this.endExamStatus = endExamStatus;
	}

	public Long getExamId() {
		return examId;
	}

	public void setExamId(Long examId) {
		this.examId = examId;
	}

	public String getExamName() {
		return examName;
	}

	public void setExamName(String examName) {
		this.examName = examName;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public boolean iseStatus() {
		return eStatus;
	}

	public void seteStatus(boolean eStatus) {
		this.eStatus = eStatus;
	}

	public boolean isEndExamStatus() {
		return endExamStatus;
	}

	public void setEndExamStatus(boolean endExamStatus) {
		this.endExamStatus = endExamStatus;
	}
    
    

}
