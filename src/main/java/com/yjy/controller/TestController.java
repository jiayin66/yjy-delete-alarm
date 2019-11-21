package com.yjy.controller;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yjy.mapperuser.UserMapper;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/test/")
@RestController
@Slf4j
public class TestController {
	@Autowired
	private UserMapper userMapper;
	@GetMapping("xqwg")
	public JSONObject get() {
		JSONObject js=JSONObject.parseObject(str);
		JSONArray array = JSONArray.parseArray(js.get("features").toString());
		Set<String> set=new HashSet<String>();
		for(int i=0;i<array.size();i++) {
			JSONObject jo = JSONObject.parseObject(array.get(i).toString());
			JSONObject att = JSONObject.parseObject(jo.getString("attributes").toString());
			String code = att.getString("tc_code");
			set.add(code);
		}
		System.out.println(array.size());
		System.out.println(set.size());
		
		return  js;
	}
	
	@GetMapping("a")
	public void save() {
		userMapper.insertArea("1", "1", "1", "1");
	}
}