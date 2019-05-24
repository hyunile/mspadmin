package kr.msp.common.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminCommonUtil {
    private final static Logger logger = LoggerFactory.getLogger(AdminCommonUtil.class);

	public static String getGraphJSonString(List<HashMap<String,Object>> dbList, String title){
		String result = null;
		
		List<HashMap<String,Object>> listData = new ArrayList<HashMap<String,Object>>();
		
		if(dbList!=null && dbList.size()>0){
            for(int i=0; i<dbList.size(); i++){
                Map<String,Object> dbMap = dbList.get(i);
                int intCnt = Integer.parseInt(dbMap.get("CNT").toString());
                String strTitle = dbMap.get(title).toString();
                
                HashMap<String,Object> hashData = new HashMap<String, Object>();
                hashData.put("CNT", intCnt);
                hashData.put("TITLE", strTitle);
                listData.add(hashData);
            }
        }
		
		ObjectMapper mapper = new ObjectMapper();
        try {
        	result = mapper.writeValueAsString(dbList);
        } catch (IOException e) {
            logger.error("Exception caught.", e);
        }
        return result;
	}
}