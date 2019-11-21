package com.yjy.mapperins;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface InsMapper {

	@Select("select t.id from T_CMD_INSTRUCTION t where t.id=#{ins,jdbcType=VARCHAR}")
	public String findByInsId(@Param("ins") String ins);
	
	@Select("select t.INSTRUCTION_ID from T_CMD_INSTRUCTION_BUSINESS t where t.BUSINESS_KEY=#{alarmId,jdbcType=VARCHAR}")
	public String findInsByBusinessKey(@Param("alarmId") String alarmId);

	@Delete("delete from T_CMD_INSTRUCTION_BUSINESS t where t.BUSINESS_KEY=#{alarmId,jdbcType=VARCHAR}")
	public void deleteRelation(@Param("alarmId") String alarmId);

	@Delete("delete from T_CMD_INSTRUCTION t where t.id=#{insId,jdbcType=VARCHAR}")
	public void deleteIns(@Param("insId") String insId);

	@Delete("delete from T_CMD_INSTRUCTION_RECIEVE t where t.INSTRUCTION_ID=#{insId,jdbcType=VARCHAR}")
	public void deleteReceive(@Param("insId") String insId);

	@Delete("delete from T_CMD_INSTRUCTION_TASKSCHEDULE t where t.INSID=#{insId,jdbcType=VARCHAR}")
	public void deleteTaskSchedule(@Param("insId") String insId);
	
	@Delete("delete from T_CMD_INSTRUCTION_RESULT t where t.INSTRUCTION_ID=#{insId,jdbcType=VARCHAR}")
	public void deleteStepApp(@Param("insId") String insId);


}
