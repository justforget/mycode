package com.crigh.common.intercepter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.sun.corba.se.impl.encoding.CodeSetConversion.BTCConverter;

/**
 * 文件类型拦截器
 * 
 * @author zhou
 * 
 */
public class FileShellInterceptor extends HandlerInterceptorAdapter {

	private List<String> allowed; //允许上传的文件类型

	public List<String> getAllowed() {
		return allowed;
	}

	public void setAllowed(List<String> allowed) {
		this.allowed = allowed;
	}

	public final static Map<String, String> FILE_TYPE_MAP = new HashMap<String, String>();
	static {
		getAllFileType(); // 初始化文件类型信息
	}

	// 常见的文件头类型
	private static void getAllFileType() {
		FILE_TYPE_MAP.put("jpg", "FFD8FF"); // JPEG (jpg)
		FILE_TYPE_MAP.put("png", "89504E47"); // PNG (png)
		FILE_TYPE_MAP.put("gif", "47494638"); // GIF (gif)
		FILE_TYPE_MAP.put("tif", "49492A00"); // TIFF (tif)
		FILE_TYPE_MAP.put("bmp", "424D"); // Windows Bitmap (bmp)
		FILE_TYPE_MAP.put("dwg", "41433130"); // CAD (dwg)
		FILE_TYPE_MAP.put("html", "68746D6C3E"); // HTML (html)
		FILE_TYPE_MAP.put("rtf", "7B5C727466"); // Rich Text Format (rtf)
		FILE_TYPE_MAP.put("xml", "3C3F786D6C");
		FILE_TYPE_MAP.put("zip", "504B0304");
		FILE_TYPE_MAP.put("rar", "52617221");
		FILE_TYPE_MAP.put("psd", "38425053"); // Photoshop (psd)
		FILE_TYPE_MAP.put("eml", "44656C69766572792D646174653A"); // Email
		
		FILE_TYPE_MAP.put("dbx", "CFAD12FEC5FD746F"); // Outlook Express (dbx)
		FILE_TYPE_MAP.put("pst", "2142444E"); // Outlook (pst)
		FILE_TYPE_MAP.put("xls", "D0CF11E0"); // MS Word
		FILE_TYPE_MAP.put("doc", "D0CF11E0"); // MS Excel 注意：word 和 excel的文件头一样
		FILE_TYPE_MAP.put("mdb", "5374616E64617264204A"); // MS Access (mdb)
		FILE_TYPE_MAP.put("wpd", "FF575043"); // WordPerfect (wpd)
		FILE_TYPE_MAP.put("eps", "252150532D41646F6265");
		FILE_TYPE_MAP.put("ps", "252150532D41646F6265");
		FILE_TYPE_MAP.put("pdf", "255044462D312E"); // Adobe Acrobat (pdf)
		FILE_TYPE_MAP.put("qdf", "AC9EBD8F"); // Quicken (qdf)
		FILE_TYPE_MAP.put("pwl", "E3828596"); // Windows Password (pwl)
		FILE_TYPE_MAP.put("wav", "57415645"); // Wave (wav)
		FILE_TYPE_MAP.put("avi", "41564920");
		FILE_TYPE_MAP.put("ram", "2E7261FD"); // Real Audio (ram)
		FILE_TYPE_MAP.put("rm", "2E524D46"); // Real Media (rm)
		FILE_TYPE_MAP.put("mpg", "000001BA"); //    
		FILE_TYPE_MAP.put("mov", "6D6F6F76"); // Quicktime (mov)
		FILE_TYPE_MAP.put("asf", "3026B2758E66CF11"); // Windows Media (asf)
		FILE_TYPE_MAP.put("mid", "4D546864"); // MIDI (mid)
	}

	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		String backURL = request.getHeader("Referer");

		boolean isAllowed = true;
		String message = "";
		MultipartResolver res = new org.springframework.web.multipart.commons.CommonsMultipartResolver();
		if (res.isMultipart(request)) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			Map<String, MultipartFile> files = multipartRequest.getFileMap();
			Iterator<String> iterator = files.keySet().iterator();
			File file = null;
			try {
				while (iterator.hasNext()) {
					String formKey = (String) iterator.next();
					MultipartFile multipartFile = multipartRequest.getFile(formKey);
					if(multipartFile.isEmpty()){
						continue;
					}
					String originalFilename = multipartFile.getOriginalFilename();
					byte[] bytes = multipartFile.getBytes();
					file = new File("test"); //此处在服务器会有权限问题,需要写到硬盘中
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(bytes);
					String fileType1 = getImageFileType(file);
					String fileType2 = getFileByFile(file);
					boolean flag = checkFile(originalFilename, fileType1, fileType2);
//					String originalFilename = multipartFile.getOriginalFilename();
					if (!flag) {
						message = "文件类型错误！<br>注：本系统不支持xlsx文件，请先转为xls文件再上传！";
						isAllowed = false;
					}
				}
			} catch (Exception e) {
				throw new RuntimeException();
			}
		} 
		if(!isAllowed){
			request.getSession().setAttribute("message", message);
			response.sendRedirect(backURL);
			return false;
		}else{
			return true;
		}
	}
	
/**
 * 检查文件是否被允许上传到服务器	
 * @param originalFilename
 * @param fileType1
 * @param fileType2
 * @return
 */
    private boolean checkFile(String originalFilename, String fileType1, String fileType2) {
    	/*检查文件后缀*/
    	String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1,
    			originalFilename.length());
    	if(!allowed.contains(suffix)){
    		return false;
    	}
    	if(fileType1 != null){
    		if(allowed.contains(fileType1)){
    			return true;
    		} else {
    			return false;
    		}
    	}
    	
    	/*判断根据文件头获取的文件类型*/
    	if(fileType2 != null ){
    		if(allowed.contains(fileType2)){
    			return true;
    		}
    	}
    	
    	return false;
	}
	
    /**  
     * Created on 2010-7-1   
     * <p>Discription:[getImageFileType,获取图片文件实际类型,若不是图片则返回null]</p>  
     * @param File  
     * @return fileType  
     * @author:[shixing_11@sina.com]  
     */    
    public final static String getImageFileType(File f)    
    {    
        if (isImage(f))  
        {  
            try  
            {  
                ImageInputStream iis = ImageIO.createImageInputStream(f);  
                Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);  
                if (!iter.hasNext())  
                {  
                    return null;  
                }  
                ImageReader reader = iter.next();  
                iis.close();  
                return reader.getFormatName();  
            }  
            catch (IOException e)  
            {  
                return null;  
            }  
            catch (Exception e)  
            {  
                return null;  
            }  
        }  
        return null;  
    }    
    
    /**  
     * Created on 2010-7-1   
     * <p>Discription:[getFileByFile,获取文件类型,包括图片,若格式不是已配置的,则返回null]</p>  
     * @param file  
     * @return fileType  
     * @author:[shixing_11@sina.com]  
     */    
    public final static String getFileByFile(File file)
    {    
        String filetype = null;    
        byte[] b = new byte[50];    
        try    
        {    
            InputStream is = new FileInputStream(file);    
            is.read(b);    
            filetype = getFileTypeByStream(b);    
            is.close();    
        }    
        catch (FileNotFoundException e)    
        {    
            return null;   
        }    
        catch (IOException e)    
        {    
        	return null;  
        }    
        return filetype;    
    }    
        
    /**  
     * Created on 2010-7-1   
     * <p>Discription:[getFileTypeByStream]</p>  
     * @param b  
     * @return fileType  
     * @author:[shixing_11@sina.com]  
     */    
    public final static String getFileTypeByStream(byte[] b)    
    {    
        String filetypeHex = String.valueOf(getFileHexString(b));    
        Iterator<Entry<String, String>> entryiterator = FILE_TYPE_MAP.entrySet().iterator();    
        while (entryiterator.hasNext()) {    
            Entry<String,String> entry =  entryiterator.next();    
            String fileTypeHexValue = entry.getValue();    
            if (filetypeHex.toUpperCase().startsWith(fileTypeHexValue)) {    
                return entry.getKey();    
            }    
        }    
        return null;    
    }    
        
    /** 
     * Created on 2010-7-2  
     * <p>Discription:[isImage,判断文件是否为图片]</p> 
     * @param file 
     * @return true 是 | false 否 
     * @author:[shixing_11@sina.com] 
     */  
    public static final boolean isImage(File file){  
        boolean flag = false;  
        try  
        {  
            BufferedImage bufreader = ImageIO.read(file);  
            int width = bufreader.getWidth();  
            int height = bufreader.getHeight();  
            if(width==0 || height==0){  
                flag = false;  
            }else {  
                flag = true;  
            }  
        }  
        catch (IOException e)  
        {  
            flag = false;  
        }catch (Exception e) {  
            flag = false;  
        }  
        return flag;  
    }  
      
    /**  
     * Created on 2010-7-1   
     * <p>Discription:[getFileHexString]</p>  
     * @param b  
     * @return fileTypeHex  
     * @author:[shixing_11@sina.com]  
     */    
    public final static String getFileHexString(byte[] b)    
    {    
        StringBuilder stringBuilder = new StringBuilder();    
        if (b == null || b.length <= 0)    
        {    
            return null;    
        }    
        for (int i = 0; i < b.length; i++)    
        {    
            int v = b[i] & 0xFF;    
            String hv = Integer.toHexString(v);    
            if (hv.length() < 2)    
            {    
                stringBuilder.append(0);    
            }    
            stringBuilder.append(hv);    
        }    
        return stringBuilder.toString();    
    }    
 
	
}
