package com.yjy.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.finest.projtool.util.ProjUtil;
import com.yjy.mapper.resource.CameraMapper;
import com.yjy.model.XYModel;
@Service
public class ShanTouXYService {
	@Autowired
	private CameraMapper cameraMapper;

	private  final String initial="WGS:84";
	private final String target="baidu:3857";


	public String changeXY(String columName, String longitude, String latitude) {
		List<XYModel> result=cameraMapper.findNeedChangeXY(columName,longitude,latitude);
		if(CollectionUtils.isEmpty(result)) {
			return "查询待转换的经纬度数据为空，无需修改";
		}
		for(XYModel XYModel:result) {
			changeXY(XYModel);
			cameraMapper.updateXY(columName,longitude,latitude,XYModel);
		}
		
		return "转换完毕！共转换个数："+result.size();
	}

	private void changeXY(XYModel xYModel) {
		
		
		double input[] = new double[2];
		input[0] = Double.valueOf(xYModel.getLongitude());
        input[1] = Double.valueOf(xYModel.getLatitude());
        double[] doubles = ProjUtil.transform(input,initial,target);
        String  longitude = String.valueOf(doubles[0]);
        String  latitude = String.valueOf(doubles[1]);
        xYModel.setLongitude(longitude);
        xYModel.setLatitude(latitude);
	}


	public String changeSingle(XYModel xYModel) {
		changeXY(xYModel);
		return xYModel.toString();
	}
}
