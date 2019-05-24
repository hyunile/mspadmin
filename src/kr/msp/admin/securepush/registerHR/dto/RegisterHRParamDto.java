package kr.msp.admin.securepush.registerHR.dto;

import org.springframework.web.multipart.MultipartFile;

/**
 * 인사정보 등록 메뉴 파라메터 데이터 클래스.
 */
public class RegisterHRParamDto {

	private MultipartFile ATTACH_FILE;

	public MultipartFile getATTACH_FILE() {
		return ATTACH_FILE;
	}

	public void setATTACH_FILE(MultipartFile aTTACH_FILE) {
		ATTACH_FILE = aTTACH_FILE;
	}
	
}
