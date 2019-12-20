package com.yjy.mapper.resource;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yjy.model.XYModel;

public interface CameraMapper {

	@Select("select Id, ${longitude},${latitude} from ${columName} where ${longitude} is not null and ${longitude}< 150 ")
	public List<XYModel> findNeedChangeXY(@Param("columName") String columName,@Param("longitude") String longitude,@Param("latitude") String latitude) ;
	
	@Update("update ${columName} set ${longitude}=#{xYModel.longitude},${latitude}=#{xYModel.latitude} where id=#{xYModel.id}")
	public void updateXY(@Param("columName") String columName,@Param("longitude") String longitude,@Param("latitude") String latitude,@Param("xYModel") XYModel xYModel);
}
