package com.yjy.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yjy.mapperuser.UserMapper;
import com.yjy.service.NNXqwgService;
import com.yjy.service.SaveAcgisService;
import com.yjy.service.XQWGService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/xqwg/")
@RestController
@Slf4j
@Api(tags="辖区网格-新版")
public class XQWGController {
	@Autowired
	private XQWGService xQWGService;
	@Autowired
	private NNXqwgService nNXqwgService;
	@Autowired
	private XQWGController xQWGController;
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private SaveAcgisService saveAcgisService;
	//第三方厂家的url
	private String otherUrl="https://gisserver.nns.gx/server/rest/services/B_XQGW_PG/MapServer/0/query?where=1%3D1&text=&objectIds=&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=*&returnGeometry=true&returnTrueCurves=false&maxAllowableOffset=&geometryPrecision=&outSR=&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVersion=&returnDistinctValues=false&resultOffset=&resultRecordCount=&queryByDistance=&returnExtentsOnly=false&datumTransformation=&parameterValues=&rangeValues=&f=pjson";
	
	//【正式】我们的acjis地址
	//private String ourAcjis="http://71.30.29.114:6080/arcgis/rest/services/HCZZFeature/FeatureServer/2/addFeatures";
	//【测试】我们的acjis地址
	//private String ourAcjis="http://71.30.29.114:6080/arcgis/rest/services/HCZZFeature_test/FeatureServer/2/addFeatures";
	//南宁开发环境
	String ourAcjis = "http://172.16.14.30:6080/arcgis/rest/services/HCZZFeature/FeatureServer/2/addFeatures";
		

	@ApiOperation("[南宁https不适用]测试是否能取到第三方厂家的数据-get")
	@GetMapping("input/get")
	public String testXQWGGet() {
		return xQWGService.testXQWGGet(otherUrl);
	}
	@ApiOperation("[南宁https不适用]测试是否能取到第三方厂家的数据-post")
	@PostMapping("input/post")
	public String testXQWGPost(@RequestBody String url) {
		return xQWGService.testXQWGGet(url);
	}
	
	
	@ApiOperation("导入arcgis的数据，设置utf-8格式的文本")
	@PostMapping("input/txt")
	public String txtRead(@RequestParam("file") MultipartFile file) throws IOException {
		// 【1】 拿到org和部门转换
		Map<String, String> stationMap = xQWGService.getStationMap();
		// 【2】拿到全部的辖区网格数据
		String str = xQWGService.txtRead(file);
		// 【3】数据解析
		JSONObject jsonObject = JSONObject.parseObject(str);
		// 1.循环全部的数据
		JSONArray array = JSONArray.parseArray(jsonObject.get("features").toString());
		List<String> error = new ArrayList<String>();
		//2. 循环拿到的全部辖区数据
		for (int i = 0; i < array.size(); i++) {
			String p = array.getString(i);
			JSONObject js = JSONObject.parseObject(p);
			// 属性
			JSONObject attributes = JSONObject.parseObject(js.get("attributes").toString());
			// 要的数据
			JSONObject geometry = JSONObject.parseObject(js.get("geometry").toString());
			// 4. 部门过滤
			// 部门code
			String code = attributes.getString("tc_code");
			// 拿到对应的部门id
			String id = stationMap.get(code);
			if (!StringUtils.isEmpty(id)) {
				// 5 存到acjs中
				String ourAcjsModel = xQWGService.getOurAcjsModel(attributes, geometry);
				String acgisResponse = saveAcgisService.postToArcgis(ourAcjis, ourAcjsModel);
				Integer mapcode = null;
				try {
					// 6.解析我们arcgis返回的code
					JSONObject parse = JSONObject.parseObject(acgisResponse);
					JSONArray parseArray = JSONArray.parseArray(parse.get("addResults").toString());
					JSONObject object = JSONObject.parseObject(parseArray.get(0).toString());
					mapcode = object.getInteger("objectId");
				} catch (Exception e) {
					log.info("保存arcgis失败");
					log.info("错误信息为：{}", e);
					error.add(ourAcjsModel);
					continue;
				}

				// 7 存我们的数据库
				String dateBaseModelResult = getDateBaseModel(geometry, mapcode);

				
				
				// 8 采用全新添加的方式：
				String tc_name = attributes.getString("tc_name");
				if (StringUtils.isEmpty(tc_name)) {
					tc_name = "默认";
				}
				userMapper.insertArea(UUID.randomUUID().toString(), tc_name, id, dateBaseModelResult);
			} else {
				log.info("此orgcode在数据库中找不到：{}", code);
			}

		}
		;
		log.info("---------------------------------------------------------------------------------");
		log.info("执行完毕");
		log.error("失败个数：{}，数据为：{}", error.size(), JSON.toJSONString(error));

		return "true";
	}
	
	
	
	private String getDateBaseModel(JSONObject geometry, Integer mapcode) {
		JSONObject spa=new JSONObject();
		spa.put("wkid",4326);
		
		Object object = geometry.get("rings");
		JSONObject geo=new JSONObject();
		geo.put("spatialReference", spa);
		geo.put("rings", object);
		
		JSONObject result=new JSONObject();
		result.put("rings", geometry.get("rings"));
		result.put("geometry", geo);
		result.put("mapCode", mapcode);
		
		JSONArray ja=new JSONArray();
		ja.add(result);
		return JSON.toJSONString(ja,SerializerFeature.DisableCircularReferenceDetect);
	}

}
