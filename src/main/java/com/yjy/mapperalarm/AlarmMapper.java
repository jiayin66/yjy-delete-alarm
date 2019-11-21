package com.yjy.mapperalarm;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.yjy.model.TestAlarmModel;

public interface AlarmMapper {
	
	@Select("SELECT t.id FROM T_PA_ALARM t where t.id=#{alarm,jdbcType=VARCHAR}")
	public String findById(@Param("alarm") String alarm);

	@Select("SELECT t.id FROM T_PA_ALARM t where t.sn=#{sn,jdbcType=VARCHAR}")
	public String findIdBySn(@Param("sn") String sn);

	@Delete("delete from T_PA_ALARM t where t.id=#{alarmId,jdbcType=VARCHAR}")
	public void deleteAlarm(@Param("alarmId") String alarmId);

	@Select("SELECT t.SN,t.SUMMARY,t.ALARMTIME alarmTime FROM T_PA_ALARM t where t.SUMMARY like '%${keyword}%' order by t.ALARMTIME desc")
	public List<TestAlarmModel> findTestAlarm(@Param("keyword") String keyword);

	@Delete("delete from T_PA_ALARM_DEAL t where t.sn=#{sn,jdbcType=VARCHAR}")
	public void deleteALarmDeal(@Param("sn") String sn);

}
