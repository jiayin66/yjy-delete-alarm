package com.yjy.mapperuser;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yjy.model.OrgstationModel;

public interface UserMapper {
	@Select("select t.id,t.orgcode from T_POLICE_STATION_INFO t")
	public List<OrgstationModel> finAllOrgcode();

	@Update("update T_JURISDICTIONAL_AREA t set t.CONTROL_CIRCLE=#{xy} where t.STATION_ID=#{id}")
	public Integer updateArea(@Param("xy") String xy,@Param("id")  String id);
	
	@Insert("insert into T_JURISDICTIONAL_AREA(ID,NAME,STATION_ID,CONTROL_CIRCLE) values(#{id},#{name},#{stationId}，#{da})")
	public Integer insertArea(@Param("id") String id,
							@Param("name") String name,
							@Param("stationId") String stationId,
							@Param("da") String da
			
			);
	

}
