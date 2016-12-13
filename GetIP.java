public class GetIP{
	public static void main(String[] args) {
		
	}

 /** 
	     * 获取用户真实IP地址
	     * 
	     * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？ 
	     * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。 
	     * 
	     * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130, 
	     * 192.168.1.100 
	     * 
	     * 用户真实IP为： 192.168.1.110 
	     * 
	     * @param request 
	     * @return 
	     */ 
 public static String getIpAddress(HttpServletRequest request) {
 	String ip = request.getHeader("X-Forwarded-For");  
 	if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
 		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
 			ip = request.getHeader("Proxy-Client-IP");  
 		}  
 		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
 			ip = request.getHeader("WL-Proxy-Client-IP");  
 		}  
 		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
 			ip = request.getHeader("HTTP_CLIENT_IP");  
 		}  
 		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
 			ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
 		}  
 		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
 			ip = request.getRemoteAddr();  
 		}  
 	} else if (ip.length() > 15) {  
 		String[] ips = ip.split(",");  
 		for (int index = 0; index < ips.length; index++) {  
 			String strIp = ips[index].trim();  
 			if (!("unknown".equalsIgnoreCase(strIp))) {  
 				ip = strIp;  
 				break;  
 			}  
 		}  
 	}  
 	return ip;  
 }  
}