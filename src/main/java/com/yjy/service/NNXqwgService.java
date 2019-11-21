package com.yjy.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.yjy.mapperuser.UserMapper;
import com.yjy.model.OrgstationModel;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
@Slf4j
public class NNXqwgService {
	@Autowired
	private UserMapper userMapper;
	
	/*public static final MediaType param
    = MediaType.parse("application/x-www-form-urlencoded");
	
	OkHttpClient client = new OkHttpClient();
	 
	public  String postSend(String url, String p) throws IOException {
	    RequestBody body = RequestBody.create(param, p);
	    Request request = new Request.Builder()
	        .url(url)
	        .post(body)
	        .build();
	    try (Response response = client.newCall(request).execute()) {
	      return response.body().string();
	    }
	  }*/



	//发送请求存arcgis服务器
	/*public Integer post(String ourAcjsUrl, String ourAcjsModel) {
		JSONObject jo=new JSONObject();
		jo.put("f", "json");
		jo.put("features", ourAcjsModel);
		String jsonString = jo.toJSONString();
		String postSend=null;
		try {
			postSend = postSend(ourAcjsUrl,jsonString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("发送请求拿到的mapcode为：{}",postSend);
		
		return null;
		
	}*/
	//发送请求存arcgis服务器
	

	/**
	 * 1.人员组织架构查出全部部门
	 * @return
	 */
	public Map<String, String> getStationMap() {
		// 1. 拿到org和部门转换
		List<OrgstationModel> orgstationModelList = userMapper.finAllOrgcode();
		Map<String, String> stationMap = new HashMap<String, String>();
		for (OrgstationModel orgstationModel : orgstationModelList) {
			stationMap.put(orgstationModel.getOrgcode(), orgstationModel.getId());
		}
		log.debug("查询到全部的部门信息为：{}", stationMap);
		return stationMap;
	}
	
	/**
	 * 拿到第三方厂家的全部数据
	 * @param url
	 * @return
	 */
	public JSONObject getYuntuAllData(String url) {
		JSONObject jsonObject=null;
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder().url(url).build();
		Response response = null;
		try {
			response = okHttpClient.newCall(request).execute();
			int status = response.code();
			String responseBody = response.body().string();
			jsonObject = JSONObject.parseObject(responseBody);
			log.debug("拿到的请求结果：",jsonObject.toJSONString());
			
		} catch (Exception e) {
			log.error("分局、派出所数据清洗失败，错误原因为{}", e.getMessage());
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return jsonObject;
	}

	
	
}
