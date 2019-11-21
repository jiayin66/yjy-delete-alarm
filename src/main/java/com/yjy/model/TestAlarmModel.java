package com.yjy.model;

import java.util.Date;

import lombok.Data;

@Data
public class TestAlarmModel {
	private String sn;
	private String summary;
	private Date alarmTime;
}
