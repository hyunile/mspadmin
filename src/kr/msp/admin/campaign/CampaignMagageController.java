package kr.msp.admin.campaign;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.campaign.campaignManage.dto.CampaignDTO;
import kr.msp.admin.campaign.campaignManage.dto.CampaignParamDTO;
import kr.msp.admin.campaign.campaignManage.service.CampaignManageService;

@RequestMapping(value="admin/campaign")
@Controller
public class CampaignMagageController {

	private final static Logger logger = LoggerFactory.getLogger(CampaignMagageController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	private CampaignManageService campaignManageService;
	
	@RequestMapping(value="/campaignManage", method=RequestMethod.GET )
	public String getCampaignManage( Model model, HttpServletRequest request, CampaignParamDTO campaignParamDTO){
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		
		model.addAttribute("R_PAGE_NUM",campaignParamDTO.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		return "admin/campaign/campaignManage/campaignManage";
	}
	
	@RequestMapping(value="/campaignManage", method=RequestMethod.POST)
	public String getCampaignListPost( Model model, HttpServletRequest request, CampaignParamDTO campaignParamDTO){
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		
		List<CampaignDTO> listResult = campaignManageService.getCampaignList(campaignParamDTO);
		
		model.addAttribute("listCampaign",listResult);
		model.addAttribute("R_PAGE_NUM",campaignParamDTO.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",campaignParamDTO.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/campaign/campaignManage/campaignManageList";
	}
	
	//저장
	@RequestMapping(value="/campaignManageDetail", method=RequestMethod.GET )
	public String getCampaignManageDetail( Model model, HttpServletRequest request, CampaignParamDTO campaignParamDTO){
		model.addAttribute("layout", "layout/null.vm" );
		model.addAttribute("popUpType", 1);
		return "admin/campaign/campaignManage/campaignManageDetail";
	}
	//수정
	@RequestMapping(value="/campaignManageDetail", method=RequestMethod.POST )
	public String getCampaignManageDetailPost( Model model, HttpServletRequest request, CampaignParamDTO campaignParamDTO){
		model.addAttribute("layout", "layout/null.vm" );
		model.addAttribute("popUpType", 0);
		return "admin/campaign/campaignManage/campaignManageDetail";
	}
	
	@ResponseBody
	@RequestMapping(value="/addCampaign", method=RequestMethod.POST )
	public String addCampaign( Model model, HttpServletRequest request, CampaignParamDTO campaignParamDTO) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		try{
			String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
			campaignParamDTO.setREG_ID(S_ID_USER);
			campaignParamDTO.setMOD_ID(S_ID_USER);
			campaignManageService.addCampaign(campaignParamDTO);
			
			result = 1;
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		map.put("result", result);
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	@ResponseBody
	@RequestMapping(value="/delCampaign", method=RequestMethod.POST )
	public String deleteCampaign( Model model, HttpServletRequest request, CampaignParamDTO campaignParamDTO) throws JsonGenerationException, JsonMappingException, IOException{
		
		
		int result = 0;
		try{
			campaignManageService.deleteCampaign(campaignParamDTO);
			result = 1;
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		map.put("result", result);
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
}
