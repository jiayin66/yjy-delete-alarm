package com.yjy.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yjy.mapperuser.UserMapper;
import com.yjy.model.OrgstationModel;
import com.yjy.model.geometry;
import com.yjy.service.NNXqwgService;
import com.yjy.service.SaveAcgisService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@RequestMapping("/nn/")
@RestController
@Slf4j
@Api(tags="辖区")
public class NNXqwgController {
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private TestController testController;
	@Autowired
	private SaveAcgisService saveAcgisService;
	@Autowired
	private NNXqwgService nNXqwgService;
	//第三方厂家的url
	private String otherUrl="https://gisserver.nns.gx/server/rest/services/B_XQGW_PG/MapServer/0/query?where=1%3D1&text=&objectIds=&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=*&returnGeometry=true&returnTrueCurves=false&maxAllowableOffset=&geometryPrecision=&outSR=&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVersion=&returnDistinctValues=false&resultOffset=&resultRecordCount=&queryByDistance=&returnExtentsOnly=false&datumTransformation=&parameterValues=&rangeValues=&f=pjson";
	//【正式】我们的acjis地址
	//private String ourAcjis="http://71.30.29.114:6080/arcgis/rest/services/HCZZFeature/FeatureServer/2/addFeatures";
	//【测试】我们的acjis地址
	private String ourAcjis="http://71.30.29.114:6080/arcgis/rest/services/HCZZFeature_test/FeatureServer/2/addFeatures";
	
	@GetMapping("/xqwg")
	@ApiOperation("辖区网格处理")
	public void get() {
		//1. 拿到org和部门转换
		Map<String,String> stationMap=nNXqwgService.getStationMap();
		
		//2. 查询其他厂家到辖区数据
		//String url = "http://172.16.3.137:6080/arcgis/rest/services/JXXQWG/MapServer/0/query?where=1%3D1&text=&objectIds=&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=*&returnGeometry=true&maxAllowableOffset=&geometryPrecision=&outSR=&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVersion=&returnDistinctValues=false&f=pjson";
		/*String url = otherUrl;
		JSONObject jsonObject =nNXqwgService.getYuntuAllData(url);*/
		JSONObject jsonObject = testController.get();
		
		//3.循环全部的数据
		JSONArray array = JSONArray.parseArray(jsonObject.get("features").toString());
		List<String>  error=new ArrayList<String>();
		for(int i=0;i<array.size();i++) {
			String p = array.getString(i); 
			 JSONObject js = JSONObject.parseObject(p);
			 //属性
             JSONObject attributes = JSONObject.parseObject(js.get("attributes").toString());
             //要的数据
             JSONObject geometry = JSONObject.parseObject(js.get("geometry").toString());
             
             //4. 部门过滤
             //部门code
             String code = attributes.getString("tc_code");
             //拿到对应的部门id
             String id = stationMap.get(code);
             
             if(!StringUtils.isEmpty(id)) {
            	 
            	  //5 存到acjs中
            	 String ourAcjsUrl=ourAcjis;
            	 String  ourAcjsModel=getOurAcjsModel(attributes,geometry);
            	 String acgisResponse=null;
            	 acgisResponse = saveAcgisService.postToArcgis(ourAcjsUrl, ourAcjsModel);
            	 Integer mapcode=null;
				try {
					 //6.解析code
	            	 JSONObject parse = JSONObject.parseObject(acgisResponse);
	         		JSONArray parseArray = JSONArray.parseArray(parse.get("addResults").toString());
	         		JSONObject object = JSONObject.parseObject(parseArray.get(0).toString());
	         		mapcode = object.getInteger("objectId");
				} catch (Exception e) {
					log.info("保存arcgis失败");
					log.info("错误信息为：{}",e);
					error.add(ourAcjsModel);
					continue;
				}
            	 
            	 
            	
            	 
            	 //7 存数据库
            	String dateBaseModelResult= getDateBaseModel(geometry,mapcode);
            	 
               
            	Integer updateArea = userMapper.updateArea(dateBaseModelResult,id);
            	if(updateArea==0) {
            		String string = attributes.getString("tc_name");
            		if(StringUtils.isEmpty(string)) {
            			string="默认";
            		}
            		userMapper.insertArea(UUID.randomUUID().toString(),
            				string,
            				id,dateBaseModelResult
            				);
            	}
            	  
            	 
             }else {
            	 log.info("此orgcode在数据库中找不到：{}",code);
             } 
             
             
			 
		 };
		 log.info("---------------------------------------------------------------------------------");
		 log.info("执行完毕");
		 log.error("失败个数：{}，数据为：{}",error.size(),JSON.toJSONString(error));
		
	}
	
	@GetMapping("/xqwgno")
	public void xqwgNoDateBase() {
		//1. 拿到org和部门转换
		Map<String,String> stationMap=nNXqwgService.getStationMap();
		
		//2. 查询其他厂家到辖区数据
		//String url = "http://172.16.3.137:6080/arcgis/rest/services/JXXQWG/MapServer/0/query?where=1%3D1&text=&objectIds=&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=*&returnGeometry=true&maxAllowableOffset=&geometryPrecision=&outSR=&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVersion=&returnDistinctValues=false&f=pjson";
		//String url = otherUrl;
		//JSONObject jsonObject =nNXqwgService.getYuntuAllData(url);
		JSONObject jsonObject = testController.get();
		//3.循环全部的数据
		JSONArray array = JSONArray.parseArray(jsonObject.get("features").toString());
		 array.forEach(p -> {
			 
			 JSONObject js = JSONObject.parseObject(p.toString());
			 //属性
             JSONObject attributes = JSONObject.parseObject(js.get("attributes").toString());
             //要的数据
             JSONObject geometry = JSONObject.parseObject(js.get("geometry").toString());
             
             //4. 部门过滤
             //部门code
             String code = attributes.getString("tc_code");
             //拿到对应的部门id
             String id = stationMap.get(code);
             
             if(!StringUtils.isEmpty(id)) {
            	 
            	  //5 存到acjs中
            	 String ourAcjsUrl=ourAcjis;
            	 String  ourAcjsModel=getOurAcjsModel(attributes,geometry);
            	 String acgisResponse = saveAcgisService.postToArcgis(ourAcjsUrl,ourAcjsModel);
            	 
            	 
            	 //6.解析code
            	 JSONObject parse = JSONObject.parseObject(acgisResponse);
         		JSONArray parseArray = JSONArray.parseArray(parse.get("addResults").toString());
         		JSONObject object = JSONObject.parseObject(parseArray.get(0).toString());
         		Integer mapcode = object.getInteger("objectId");
            	 
            	 //7 存数据库
            	String dateBaseModelResult= getDateBaseModel(geometry,mapcode);
            	 
               
            	//userMapper.updateArea(dateBaseModelResult,id);
            	  
            	 
             }else {
            	 log.info("此orgcode在数据库中找不到：{}",code);
             } 
			 
		 });
		
	}
	
	@GetMapping("/xqwgtest")
	public void getxqwgtest() {
		//1. 拿到org和部门转换
		Map<String,String> stationMap=nNXqwgService.getStationMap();
		
		//2. 查询其他厂家到辖区数据
		//String url = "http://172.16.3.137:6080/arcgis/rest/services/JXXQWG/MapServer/0/query?where=1%3D1&text=&objectIds=&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=*&returnGeometry=true&maxAllowableOffset=&geometryPrecision=&outSR=&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVersion=&returnDistinctValues=false&f=pjson";
		//String url = otherUrl;
		//JSONObject jsonObject =nNXqwgService.getYuntuAllData(url);
		JSONObject jsonObject = testController.get();
		
		//3.循环全部的数据
		JSONArray array = JSONArray.parseArray(jsonObject.get("features").toString());
		
		Set<String> set=new HashSet<String>();
		for(int i=0;i<0;i++) {
			JSONObject jo = JSONObject.parseObject(array.get(i).toString());
			String code = jo.getString("tc_code");
			set.add(code);
		}
		log.info("第三方厂家数据个数：{}",array.size());
		log.info("总共的部门个数：{}",set.size());
		
	}
	/**
	 * 测试生成保存的模型
	 */
	/*@GetMapping("SaveAcjsModel")
	public void testSaveAcjsModel() {
		JSONObject jsonObject = getJSONObject();
		String ourAcjsModel = getOurAcjsModel(jsonObject);
		log.debug("我们系统的模型：",ourAcjsModel);
	}
	*/
	/**
	 * 测试保存到arcgis服务的方法
	 */
	/*@GetMapping("SaveAcjs")
	public void testSaveAcjs() {
		String ourAcjsUrl="http://172.16.14.30:6080/arcgis/rest/services/HCZZFeature/FeatureServer/2/addFeatures";
		JSONObject jsonObject = getJSONObject();
		String ourAcjsModel = getOurAcjsModel(jsonObject);
		
		String postToArcgis = saveAcgisService.postToArcgis(ourAcjsUrl,ourAcjsModel);
		log.debug(postToArcgis);
		String str="{\"addResults\":[{\"objectId\":11876,\"success\":true}]}";
		JSONObject parse = JSONObject.parseObject(str);
		JSONArray parseArray = JSONArray.parseArray(parse.get("addResults").toString());
		JSONObject object = JSONObject.parseObject(parseArray.get(0).toString());
		Integer objectId = object.getInteger("objectId");
		
	}*/
	/**
	 * 测试保存数据库模型
	 */
	@GetMapping("saveDatabaseModel")
	public void saveDatabaseModel() {
		JSONObject jsonObject = getJSONObject();
		String dateBaseModel = getDateBaseModel(jsonObject,123);
		log.debug("数据库模型：",dateBaseModel);
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

	private String getOurAcjsModel(JSONObject attributes,JSONObject geometry) {
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

	public JSONObject getJSONObject() {
		String str="{\"rings\":[[[108.71776200031712,23.06312399987354],[108.71126900036512,23.0656139995873],[108.7108029995598,23.066239999677123],[108.71054100006813,23.06728700029572],[108.70993800013963,23.073317999626852],[108.70901399979732,23.081503000264092],[108.7083240003534,23.090048000415777],[108.70830899966165,23.0900879995624],[108.70737999998806,23.091079000296475],[108.70522100024999,23.092252999577113],[108.703215000373,23.092557000106353],[108.70026300003849,23.092447000429615],[108.6991859998351,23.09316800040375],[108.69781599960976,23.09519300025761],[108.6972189999579,23.097069999581663],[108.69707799975095,23.099223999988453],[108.69649299975316,23.101381999680427],[108.6957280004479,23.102398999814852],[108.69387599957878,23.102552999722036],[108.69470600008287,23.108876999974427],[108.69622800012314,23.11344600023358],[108.6985130002758,23.116966999828037],[108.70108300008144,23.120013000000824],[108.70421899990794,23.120062999608592],[108.71010899993144,23.120538999975736],[108.71659300036771,23.120478999906766],[108.72194699995515,23.11893299965908],[108.72968000043272,23.117028999989202],[108.73658000026785,23.117742999640598],[108.74556299993355,23.119051999750866],[108.75763800009469,23.116196000246077],[108.75954199976451,23.114470999837636],[108.76239700012252,23.11137800019543],[108.7639440004163,23.107451999910722],[108.76620500036154,23.096328000437722],[108.7685840003524,23.09424599975398],[108.76932100016506,23.08846600030654],[108.76917899991196,23.07574500003261],[108.76755499966453,23.069501999918202],[108.76703700005856,23.065216000119165],[108.76079099980586,23.0614089999263],[108.75960099978738,23.059625000394306],[108.75977999994808,23.05801900007765],[108.76810799998526,23.05218900012312],[108.76828600009986,23.048083999677715],[108.75888800018208,23.04136199995719],[108.7505949999603,23.0366830000213],[108.74999500017014,23.03687199974388],[108.74627500039202,23.03801100010844],[108.74600699972427,23.038063999854558],[108.74303899955123,23.038620000313642],[108.7416450000178,23.039352999941855],[108.73646500036051,23.046430999891584],[108.73570899967166,23.048314000391656],[108.73434000039174,23.050053999693205],[108.73186799970944,23.051221999596464],[108.72630100019205,23.051560999941046],[108.72398699960138,23.052441000052966],[108.72184299965573,23.054181000253834],[108.71940000031083,23.057368999780408],[108.7183450002226,23.05995799956287],[108.71776200031712,23.06312399987354]]],\"geometry\":{\"spatialReference\":{\"wkid\":4326},\"rings\":[[[108.71776200031712,23.06312399987354],[108.71126900036512,23.0656139995873],[108.7108029995598,23.066239999677123],[108.71054100006813,23.06728700029572],[108.70993800013963,23.073317999626852],[108.70901399979732,23.081503000264092],[108.7083240003534,23.090048000415777],[108.70830899966165,23.0900879995624],[108.70737999998806,23.091079000296475],[108.70522100024999,23.092252999577113],[108.703215000373,23.092557000106353],[108.70026300003849,23.092447000429615],[108.6991859998351,23.09316800040375],[108.69781599960976,23.09519300025761],[108.6972189999579,23.097069999581663],[108.69707799975095,23.099223999988453],[108.69649299975316,23.101381999680427],[108.6957280004479,23.102398999814852],[108.69387599957878,23.102552999722036],[108.69470600008287,23.108876999974427],[108.69622800012314,23.11344600023358],[108.6985130002758,23.116966999828037],[108.70108300008144,23.120013000000824],[108.70421899990794,23.120062999608592],[108.71010899993144,23.120538999975736],[108.71659300036771,23.120478999906766],[108.72194699995515,23.11893299965908],[108.72968000043272,23.117028999989202],[108.73658000026785,23.117742999640598],[108.74556299993355,23.119051999750866],[108.75763800009469,23.116196000246077],[108.75954199976451,23.114470999837636],[108.76239700012252,23.11137800019543],[108.7639440004163,23.107451999910722],[108.76620500036154,23.096328000437722],[108.7685840003524,23.09424599975398],[108.76932100016506,23.08846600030654],[108.76917899991196,23.07574500003261],[108.76755499966453,23.069501999918202],[108.76703700005856,23.065216000119165],[108.76079099980586,23.0614089999263],[108.75960099978738,23.059625000394306],[108.75977999994808,23.05801900007765],[108.76810799998526,23.05218900012312],[108.76828600009986,23.048083999677715],[108.75888800018208,23.04136199995719],[108.7505949999603,23.0366830000213],[108.74999500017014,23.03687199974388],[108.74627500039202,23.03801100010844],[108.74600699972427,23.038063999854558],[108.74303899955123,23.038620000313642],[108.7416450000178,23.039352999941855],[108.73646500036051,23.046430999891584],[108.73570899967166,23.048314000391656],[108.73434000039174,23.050053999693205],[108.73186799970944,23.051221999596464],[108.72630100019205,23.051560999941046],[108.72398699960138,23.052441000052966],[108.72184299965573,23.054181000253834],[108.71940000031083,23.057368999780408],[108.7183450002226,23.05995799956287],[108.71776200031712,23.06312399987354]]]},\"mapCode\":717}";
		JSONObject js=JSONObject.parseObject(str);
		return JSONObject.parseObject(js.get("geometry").toString());
	}
	
	
}
