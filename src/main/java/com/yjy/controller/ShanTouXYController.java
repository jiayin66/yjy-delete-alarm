package com.yjy.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yjy.model.XYModel;
import com.yjy.service.ShanTouXYService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/xy/")
@RestController
@Slf4j
@Api(tags="汕头的坐标转换")
public class ShanTouXYController {
	@Autowired
	private ShanTouXYService shanTouXYService;
	@PostMapping("database")
	public String changeXY(@RequestParam("columName") String columName,
		@ApiParam("默认为longitude")	@RequestParam(value="longitude",required=false) String longitude,
		@ApiParam("默认为latitude")	@RequestParam(value="latitude",required=false) String latitude) {
		longitude= (longitude==null) ? "longitude" :longitude;
		latitude= (latitude==null) ? "latitude" :latitude;
		return shanTouXYService.changeXY(columName,longitude,latitude);
	}
	@PostMapping("changesingle")
	public String changeSingle(@RequestBody XYModel xYModel) {
		return shanTouXYService.changeSingle(xYModel);
	}
}
