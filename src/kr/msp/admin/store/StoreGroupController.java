/**
 * 앱스토어의 그룹 관리를 하는 콘트롤러
 *  2013.11.29
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
import kr.msp.admin.store.group.dto.GroupDto;
import kr.msp.admin.store.group.dto.GroupListDto;
import kr.msp.admin.store.group.dto.GroupSearchDto;
import kr.msp.admin.store.group.service.StoreGroupService;


@Controller
@RequestMapping("admin/store/group")
public class StoreGroupController extends ControllerUtil {

	private Logger logger = Logger.getLogger(this.getClass());

	@Autowired(required=true)
	protected StoreGroupService groupService;
	
	@Autowired(required = true) 
    private MessageSource messageSource; 

	//그룹정보 리스트
	@RequestMapping( method=RequestMethod.GET)
	public String SelectGroupList(Model model){
		logger.debug("####################################### GET "+this.getClass().getName()) ;
		GroupSearchDto dto = new GroupSearchDto();
		dto.setPAGE_NUM(1);
		dto.setPAGE_SIZE(row_size);
		
		dto.setORDER_TARGET("REG_DT");
		dto.setORDER_TYPE("DESC");
		//사용권한 조회
		List<GroupDto> groupList = groupService.selectGroupList(dto);
		model.addAttribute("groupList", groupList);
		
		setPager(model, 1);
		
		return "admin/store/group/groupMain";
	}

	//검색버튼 클릭 사용권한 리스트조회
	@RequestMapping( method=RequestMethod.POST)
	public String SelectGroupList(Model model, GroupSearchDto dto){
		logger.debug("####################################### POST " + this.getClass().getName()) ;
		List<GroupDto> groupList = groupService.selectGroupList(dto);
		model.addAttribute("groupList", groupList);
	
		setPager(model, dto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",  dto.getPAGE_SIZE());
	    model.addAttribute("layout","layout/null.vm");
		
		return "admin/store/group/groupList";
	}
	
	//저장
	@ResponseBody
	@RequestMapping( value="/save" , method=RequestMethod.POST)
	//public String NoticeRegistPost( @RequestParam("FILE1") MultipartFile file, Model model,  HttpServletRequest request, NoticeDto noticeDto ) throws JsonGenerationException, JsonMappingException, IOException{
	public String AppInfoRegistPost( Model model,  HttpServletRequest request, GroupListDto  groupListDto ) throws JsonGenerationException, JsonMappingException, IOException{
				
		logger.info("######### 앱정보 등록 START #################33");
	
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		
		List<String> grpNmNewList = groupListDto.getGRP_NM_NEW() ; //행추가
		
		List<String> grpNmList = groupListDto.getGRP_NM() ;
		List<String> grpIdList = groupListDto.getGRP_ID() ; 
		int result = 0;
		String check = "";
		String groupName = "";
		
		//행추가
		if(grpNmNewList != null){
			for (int i = 0; i < grpNmNewList.size(); i++) {
			
				String nm  = grpNmNewList.get(i);
				
				GroupDto groupDto = new GroupDto();
				groupDto.setREG_ID(S_ID_USER);
				groupDto.setMOD_ID(S_ID_USER);
				groupDto.setGRP_NM(nm);
				groupDto.setGRP_ID("");
				
				//그룹이름 중복체크
				int cnt = groupService.checkDupGroupName(groupDto);
				
				if(cnt > 0){
					check = "EXIST";
					groupName = nm;
					result = 0;
					break;
				}
				
				result = groupService.saveGroup(groupDto);
			}
		}
		
		//기존
		if( grpNmList != null && "".equals(check) ){
			for (int i = 0; i < grpNmList.size(); i++) {
				
				String nm  = grpNmList.get(i);
				String id = grpIdList.get(i);
				GroupDto groupDto = new GroupDto();
				groupDto.setREG_ID(S_ID_USER);
				groupDto.setMOD_ID(S_ID_USER);
				groupDto.setGRP_NM(nm);
				groupDto.setGRP_ID(id);
				
				result = groupService.saveGroup(groupDto);
			}
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.store.device.alert.save", null, LocaleContextHolder.getLocale()) ); //"저장 되었습니다."
		} else {
			if("EXIST".equals(check)){
				map.put("msg",messageSource.getMessage("menu.store.controller.sameGroupName", null, LocaleContextHolder.getLocale())+"[" + groupName + "]" ); //그룹이름이 중복되었습니다.
			}else{
				map.put("msg",messageSource.getMessage("menu.store.device.alert.saveError", null, LocaleContextHolder.getLocale()) ); //저장중 오류가 발생했습니다."
			}
			map.put("result", result);
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		
		return data;
	}
	
	
	@ResponseBody
	//앱리스트 삭제
	@RequestMapping("/deleteList")
	public String groupListDelete( Model model, 	
			  HttpServletRequest request,
			  @ModelAttribute("DEL_LIST") ArrayList<String> DEL_LIST) throws Exception{
		
		logger.info(" ### appInfoDelete START ========= ");
		logger.info("DEL_LIST : " + DEL_LIST);
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		
		int dbResultSum = 0;
		int result = 0;
		
		try{
		
			for (int i = 0; i < DEL_LIST.size(); i++) {
				String idx = DEL_LIST.get(i) ;
				
				
				GroupDto groupDto = new GroupDto();
				groupDto.setGRP_ID(idx) ;
				groupDto.setMOD_ID(S_ID_USER);
				 //2. DB삭제
				
				result = groupService.deleteGroup(groupDto); //그룹 삭제
				 
				logger.info("result : " + result);
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
			map.put("msg",messageSource.getMessage("menu.store.device.alert.saveError", null, LocaleContextHolder.getLocale()) ); //저장중 오류가 발생했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		
		return data;
		
	}
	
	@RequestMapping( value = "/regist" , method = RequestMethod.GET)
	public String GroupRegistGet(Model model ){
		
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/store/group/groupModify";
	}
	
	//그룹등록
	@ResponseBody
	@RequestMapping( value = "/regist" , method = RequestMethod.POST)
	public String GroupRegistPost( GroupDto group,HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException{
		
		group.setREG_ID(  (String) request.getSession().getAttribute("S_ID_USER") );
		int result = 0;
		result = groupService.insertStoGroup(group);
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successInsert", null, LocaleContextHolder.getLocale())  ); //"등록 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //"등록에 실패했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//그룹 수정 화면
	@RequestMapping( value = "/edit" , method = RequestMethod.GET)
	public String GroupRegistEdit(Model model, GroupDto group ){
		
		GroupDto groupOne = groupService.selectStoGroupOne(group);
		
		model.addAttribute("groupOne",groupOne);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/store/group/groupModify";
	}
	
	//그룹 수정
	@ResponseBody
	@RequestMapping( value = "/edit" , method = RequestMethod.POST)
	public String GroupEditPost( GroupDto group,HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException{
		
		group.setMOD_ID( (String) request.getSession().getAttribute("S_ID_USER") );
		
		int result = 0;
		
		result = groupService.updateStoGroup(group);
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()) ); //"수정 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()) ); //"수정에 실패했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//그룹 삭제
	@ResponseBody
	@RequestMapping( value = "/delete" , method = RequestMethod.POST)
	public String GroupDeletePost( GroupDto group, HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException {
		
		int result = -1;
		
		try {
			result = groupService.deleteStoGroup(group);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //"삭제 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failDelete", null, LocaleContextHolder.getLocale()) ); //"삭제에 실패했습니다."
		} 
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}

}
