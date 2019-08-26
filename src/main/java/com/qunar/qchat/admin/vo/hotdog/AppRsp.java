package com.qunar.qchat.admin.vo.hotdog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppRsp implements DappJsonResult{

    private Map<String,Object> bstatus = new HashMap<String,Object>();
    private Object data;
	private List<Object> res  = new ArrayList<Object>();

	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public Map<String,Object> getBstatus() {
		return bstatus;
	}
	public void setBstatus(Map<String,Object> bstatus) {
		this.bstatus = bstatus;
	}
	public List<Object> getRes() {
		return res;
	}
	public void setRes(List<Object> res) {
		this.res = res;
	}
	public void setBstatus(AppCodeEnum codeEnum) {
		Map<String, Object> bMap = new HashMap<String, Object>();
		bMap.put("code", codeEnum.getCode());
		bMap.put("des", codeEnum.getDesc());
		this.bstatus = bMap;
	}
	public void setBstatus(Integer status, String desc) {
		Map<String, Object> bMap = new HashMap<String, Object>();
		bMap.put("code", status);
		bMap.put("des", desc);
		
		this.bstatus = bMap;
	}
	public void setBstatus(AppCodeEnum codeEnum, String desc) {
		Map<String, Object> bMap = new HashMap<String, Object>();
		bMap.put("code", codeEnum.getCode());
		bMap.put("des", desc);
		
		this.bstatus = bMap;
	}
	public Map<String, Object> toMap(){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("data", data);
		map.put("bstatus", bstatus);
		map.put("res", res);
		return map;
	}
}
