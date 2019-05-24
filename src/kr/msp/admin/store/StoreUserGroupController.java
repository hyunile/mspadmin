/**
 * 앱스토어의 사용자 그룹 관리를 하는 콘트롤러
 *  2013.12.03
 */
package kr.msp.admin.store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.base.utils.ControllerUtil;
import kr.msp.admin.store.userGroup.dto.UserDto;
import kr.msp.admin.store.userGroup.dto.UserGroupDto;
import kr.msp.admin.store.userGroup.dto.UserGroupListDto;
import kr.msp.admin.store.userGroup.dto.UserGroupSearchDto;
import kr.msp.admin.store.userGroup.service.StoreUserGroupService;


@Controller
public class StoreUserGroupController extends ControllerUtil {

	private Logger logger = Logger.getLogger(this.getClass());

	@Autowired(required=true)
	protected StoreUserGroupService userGroupService;
	
	@Autowired(required = true) 
    private MessageSource messageSource; 

	//사용자그룹정보 리스트
	@RequestMapping( value="admin/store/userGroup" ,method=RequestMethod.GET)
	public String SelectGroupList(Model model){
		
		UserGroupSearchDto dto = new UserGroupSearchDto();
		dto.setPAGE_NUM(1);
		dto.setPAGE_SIZE(row_size);
		dto.setORDER_TARGET("UG_IDX");
		dto.setORDER_TYPE("DESC");
		
		//사용자 그룹 리스트조회
		List<UserGroupDto> groupList = userGroupService.selectUserGroupList(dto);
		model.addAttribute("userGroupList", groupList);
		
		setPager(model, 1);
		
		return "admin/store/userGroup/userGroupMain";
	}

	//검색버튼 클릭 사용자 그룹  리스트조회
	@RequestMapping( value="admin/store/userGroup" ,method=RequestMethod.POST)
	public String SelectGroupList(Model model, UserGroupSearchDto dto){
		
		dto.setPAGE_SIZE(row_size);
		
		//사용자 그룹 리스트조회
		List<UserGroupDto> groupList = userGroupService.selectUserGroupList(dto);
		model.addAttribute("userGroupList", groupList);
	
		setPager(model, dto.getPAGE_NUM());
	    model.addAttribute("layout","layout/null.vm");
		
	    return "admin/store/userGroup/userGroupList";
	}

	//사용자 그룹 정보 화면 이동 
	@RequestMapping( value="admin/store/userGroup/userGroupAdd" ,method=RequestMethod.GET)
	public String userGroupAdd(Model model, UserGroupDto dto){
		
		 List<UserGroupDto> groupList = userGroupService.selectGroupList(dto) ;
		 List<UserDto> userList   = userGroupService.selectUserList(dto) ;
	
		model.addAttribute("groupList",     groupList);
		model.addAttribute("userList", userList    );
      
		return "admin/store/userGroup/userGroupAdd";
	}
	
	//사용자 그룹정보 저장
	@ResponseBody
	@RequestMapping( value="/admin/store/userGroup/save" , method=RequestMethod.POST)
	public String AppInfoRegistPost( Model model,  HttpServletRequest request, UserGroupListDto  listDto ) throws JsonGenerationException, JsonMappingException, IOException{
				
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		
		List<String> grpIdList = listDto.getGRP_ID();
		List<String> empNoList = listDto.getEMP_NO() ;
		int result = 0;
		HashMap<String, Object> map = new HashMap<String,Object>();
		
	    try{
			if(grpIdList != null && empNoList != null){
				for (int i = 0; i < grpIdList.size(); i++) {
				
					String grpId  = grpIdList.get(i);
					String empNo =  empNoList.get(i);
					
					UserGroupDto groupDto = new UserGroupDto();
					groupDto.setREG_ID(S_ID_USER);
					groupDto.setMOD_ID(S_ID_USER);
					groupDto.setGRP_ID(grpId);
					groupDto.setEMP_NO(empNo);
					
					result = userGroupService.saveUserGroup(groupDto);
				}
			}
		
		} catch (Exception e) {
			logger.error("Exception caught.", e);
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.store.device.alert.saveError", null, LocaleContextHolder.getLocale()) ); //저장중 장애가 발생했습니다."
		}
	
		
		
		map.put("result", result);
		map.put("msg",messageSource.getMessage("menu.store.device.alert.save", null, LocaleContextHolder.getLocale()) ); //"저장 되었습니다."
		
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		
		return data;
	}
	
	//사용자 그룹정보 삭제
	@ResponseBody
	@RequestMapping("admin/store/userGroup/deleteList")
	public String userGroupListDelete( Model model, 	
			  HttpServletRequest request,
			  @ModelAttribute("DEL_LIST") ArrayList<String> DEL_LIST) throws Exception{
		
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		int dbResultSum = 0;
		int result = 0;
		
		try{
			for (int i = 0; i < DEL_LIST.size(); i++) {
				String idx = DEL_LIST.get(i) ;
				
				UserGroupDto groupDto = new UserGroupDto();
				groupDto.setUG_IDX(idx) ;
				groupDto.setMOD_ID(S_ID_USER);
				
				result = userGroupService.deleteUserGroup(groupDto); //그룹 삭제
				dbResultSum += result;
			}
			
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
	
		if(dbResultSum > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //"삭제 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.store.device.alert.saveError", null, LocaleContextHolder.getLocale()) ); //저장중 장애가 발생했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		
		return data;
	}
}
