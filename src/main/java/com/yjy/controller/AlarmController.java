package com.yjy.controller;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.server.PathParam;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yjy.mapperalarm.AlarmMapper;
import com.yjy.mapperins.InsMapper;
import com.yjy.model.TestAlarmModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/alarm/")
@RestController
@Slf4j
@Api(tags="警情删除")
public class AlarmController {
	@Autowired
	private AlarmMapper alarmMapper;
	
	@Autowired
	private InsMapper insMapper;
	
	@GetMapping("1/delete/find")
	@ApiOperation("(1)查找到警情摘要中带xx关键字的警情（eg:测试）")
	public List<TestAlarmModel> findTestAlarm(@RequestParam("keyword") String keyword) {
		List<TestAlarmModel> result=alarmMapper.findTestAlarm(keyword);
		logParam(result);
		return result;
	}
	
	private void logParam(List<TestAlarmModel> result) {
		StringBuffer sb=new StringBuffer();
		sb.append("[");
		int index=1;
		for(TestAlarmModel testAlarmModel:result) {
			sb.append("\"");
			sb.append(testAlarmModel.getSn());
			sb.append("\"");
			if(index!=result.size()) {
				sb.append(",");
			}
			index++;
		}
		sb.append("]");
		log.info("待删除的警情如入参为："+sb.toString());
	}

	@PostMapping("2/delete")
	@ApiOperation("(2)根据流水号集合，删除警情")
	public String delete(@RequestBody List<String> snList) {
		for(int i=1;i<=snList.size();i++) {
			String sn = snList.get(i-1);
			String alarmId=alarmMapper.findIdBySn(sn);
			if(StringUtils.isEmpty(alarmId)) {
				log.error("此sn：{}查询警情为空",sn);
				continue;
			}
			String insId=insMapper.findInsByBusinessKey(alarmId);
			if(StringUtils.isEmpty(insId)){
				alarmMapper.deleteAlarm(alarmId);
				log.error("此sn：{}查询指令为空，所以只删除警情",sn);
				continue;
			}
			alarmMapper.deleteAlarm(alarmId);
			alarmMapper.deleteALarmDeal(sn);
			insMapper.deleteRelation(alarmId);
			insMapper.deleteIns(insId);
			insMapper.deleteReceive(insId);
			insMapper.deleteTaskSchedule(insId);
			insMapper.deleteStepApp(insId);
		}
		return "请求完毕";
	}
	
	
	@GetMapping("/{alarm}/{ins}")
	public String getId(@PathParam("alarm") String alarm,@PathParam("ins") String ins) {
		System.out.println("1");
		String id1=alarmMapper.findById(alarm);
		String id2=insMapper.findByInsId(ins);
		return id1+id2;
	}
	
	@PostMapping("/count")
	@ApiOperation("警情删除前的数量统计")
	public String count(@RequestBody  List<String> snList) {
		List<String> idList=new ArrayList<String>();
		for(int i=1;i<=snList.size();i++) {
			String sn = snList.get(i-1);
			String id=alarmMapper.findIdBySn(sn);
			if(StringUtils.isEmpty(id)) {
				log.debug("第：{}个数据我们警情库没有，请核实:{}",i,sn);
				idList.add(sn);
			}
		}
		log.info("总共查询：{}个数据，其中：{}个数据我们库没有，没有的全部sn为：{}",snList.size(),idList.size(),idList);
		return "请求完毕";
	}
	
}
