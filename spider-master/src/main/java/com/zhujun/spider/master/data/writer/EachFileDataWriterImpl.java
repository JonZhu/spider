package com.zhujun.spider.master.data.writer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * 每个页面, 单独写文件
 * 
 * @author zhujun
 * @date 2016年7月19日
 *
 */
public class EachFileDataWriterImpl implements SpiderDataWriter {

	final private String dir;
	
	public EachFileDataWriterImpl(String dir) {
		this.dir = dir;
	}
	
	@Override
	public void write(String originUrl, String contentType, Date fetchTime, byte[] contentData) throws IOException {

		URL urlObj = new URL(originUrl);
		File hostDir = new File(dir, urlObj.getHost());
		
		String fileName = urlObj.getFile();
		if ("".equals(fileName) || "/".equals(fileName)) {
			fileName = "index.html";
		}
		
		// replace : * ? " < > | 
		fileName = fileName.replaceAll("[:*?\"<>|]", "_");
		
		File dataFile = new File(hostDir, fileName);
		if (dataFile.isDirectory()) {
			// 如果文件路径已经被生成目录, 则存入该目录下的 index.html文件中
			dataFile = new File(dataFile, "index.html");
		} else {
			mkdirs(dataFile.getParentFile());
		}
		
		FileUtils.writeByteArrayToFile(dataFile, contentData);
		
	}

	/**
	 * 创建目录
	 * 
	 * @author zhujun
	 * @date 2016年7月19日
	 *
	 * @param parentFile
	 * @throws IOException 
	 */
	private void mkdirs(File dir) throws IOException {
		if (dir.isDirectory()) {
			return;
		}
		
		boolean flag = dir.mkdirs();
		if (!flag) {
			// 检查是否有文件占用目录名称
			File temp = dir;
			List<File> movedFileList = new ArrayList<>();
			while (temp != null) {
				if (temp.isFile()) {
					File moveToFile = new File(temp.getAbsolutePath() + "__mkdir_move");
					FileUtils.moveFile(temp, moveToFile);
					movedFileList.add(moveToFile);
				}
				
				temp = temp.getParentFile();
			}
			
			flag = dir.mkdirs();
			if (!flag) {
				throw new IOException("无法创建目录:" + dir.getAbsolutePath());
			}
			
			// 创建目录成功, 处理之前移动过的文件
			for (File file : movedFileList) {
				FileUtils.moveFile(file, new File(file.getAbsolutePath().replace("__mkdir_move", "/index")));
			}
			
		}
		
	}
	
}
