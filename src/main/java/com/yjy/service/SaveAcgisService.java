package com.yjy.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.node.ArrayNode;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SaveAcgisService {
	RestTemplate restTemplate = new RestTemplate();
	
	public String postToArcgis(String url,String features) {
		// 保存线对象到arcgis服务
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		log.debug("features:" + features);
		map.add("features", features);
		map.add("f", "json");
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		HttpEntity<String> response = restTemplate.postForEntity(url, request, String.class);
		log.debug("保存到arcgis,result:" + response.getBody());
		return response.getBody();
	}
}
