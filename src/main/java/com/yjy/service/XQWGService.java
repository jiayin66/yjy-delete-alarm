package com.yjy.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yjy.mapperuser.UserMapper;
import com.yjy.model.OrgstationModel;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
@Slf4j
public class XQWGService {
	@Autowired
	private UserMapper userMapper;
	
	public String testXQWGGet(String otherUrl) {
		return getYuntuAllData(otherUrl).toJSONString();
	}

	
	/**
	 * 通过连接拿到第三方厂家的全部数据
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
			log.error("分局、派出所数据清洗失败，错误原因为{}", e);
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return jsonObject;
	}

	


	//这种方式可以全部读取，但是通过数组转字符串时候缺失了部分字段。(感觉两种方式都是没有缺失，只是exlipse展示问题)
	/*public String txtRead(MultipartFile file) throws IOException {
		FileInputStream inputStream = (FileInputStream) file.getInputStream();
		//设置100m
		byte[] b=new byte[10*1024*1024];
		int len = inputStream.read(b);
		
		String str=new String(b,0,len,"UTF-8");
		return "1";
	}*/
	
	public String txtRead(MultipartFile file) throws IOException {
		StringBuffer sb=new StringBuffer();
		FileInputStream inputStream = (FileInputStream) file.getInputStream();
		InputStreamReader isr=new InputStreamReader(inputStream,"UTF-8");
		int len=-1;
		while((len=isr.read())!=-1) {
			
			sb.append((char)len);
		}
		return sb.toString();
	}
	public String getOurAcjsModel(JSONObject attributes,JSONObject geometry) {
		JSONObject spa=new JSONObject();
		spa.put("wkid",4326);
		JSONObject geo=new JSONObject();
		geo.put("spatialReference", spa);
		geo.put("rings", geometry.get("rings"));
		JSONObject att=new JSONObject();
		att.put("name", attributes.getString("tc_name"));
		JSONObject result=new JSONObject();
		result.put("geometry", geo);
		result.put("attributes", att);
		JSONArray ja=new JSONArray();
		ja.add(result);
		return ja.toJSONString();
	}

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


}
