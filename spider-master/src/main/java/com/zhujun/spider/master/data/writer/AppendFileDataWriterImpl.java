package com.zhujun.spider.master.data.writer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.zhujun.spider.master.schedule.ScheduleConst;

/**
 * 写入Spider Data到文件
 * 
 * <p>数据写入到fileName指定的目录,文件按固定大小（如128M）切分,文件名加时间后缀</p>
 * 
 * @author zhujun
 * @date 2016年6月6日
 *
 */
public class AppendFileDataWriterImpl implements SpiderDataWriter {

	/**
	 * 文件大小128M
	 */
	private static final int FILE_SIZE = 128 * 1024 *1024;
	
	/**
	 * 数据文件中, spider haeder time的格式化, 如：2016-06-06T15:38:08
	 */
	private static final SimpleDateFormat HEADER_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	private String fileName;
	
	/**
	 * 用于写二进制数据
	 */
	private FileOutputStream fileOutputStream;
	
	/**
	 * 缓存写入
	 */
	private BufferedOutputStream bufferedOutputStream;
	
	/**
	 * 正在用于写入的文件
	 */
	private File writingFile;
	
	
	public AppendFileDataWriterImpl(String fileName) {
		this.fileName = fileName;
	}
	
	@Override
	synchronized public void write(String originUrl, Date fetchTime, byte[] contentData) {
		if (writingFile == null) {
			// 还未初始化
			
			File file = new File(fileName);
			File dir = file.getParentFile();
			if (dir.exists()) {
				// 目录存在,找最后一个未达到大小限制的文件写入
				File findFile = findWritingFileFromDir(dir);
				if (findFile != null) {
					writingFile = findFile;
				} else {
					writingFile = new File(fileName + generateSuffix());
				}
				
			} else {
				// 目录不存在,直接新创建写入文件
				dir.mkdirs();
				writingFile = new File(fileName + generateSuffix());
			}
			
			switchWritingFile();
		}
		
		
		if (writingFile.length() >= FILE_SIZE) {
			// 文件达到大小限制, 写入新文件
			writingFile = new File(fileName + generateSuffix());
			switchWritingFile();
		}
		
		// 写入数据
		try {
			writeString(ScheduleConst.CRNL + "SPIDER" + ScheduleConst.CRNL); //包开始标识
			writeString("Content-Length: " + contentData.length + ScheduleConst.CRNL);
			writeString("Url: " + originUrl + ScheduleConst.CRNL);
			writeString("Time: " + HEADER_TIME_FORMAT.format(fetchTime) + ScheduleConst.CRNL);
			writeString(ScheduleConst.CRNL); // header结束
			
			// 写入数据
			bufferedOutputStream.write(contentData);
			
			bufferedOutputStream.flush();
		} catch (Exception e) {
			throw new RuntimeException("写入数据失败", e);
		}
		
	}
	
	private void writeString(String str) throws UnsupportedEncodingException, IOException {
		bufferedOutputStream.write(str.getBytes("UTF-8"));
	}
	

	/**
	 * 从目录中找可写入的文件
	 * @author zhujun
	 * @date 2016年6月6日
	 *
	 * @param dir
	 * @return
	 */
	private File findWritingFileFromDir(File dir) {
		final String fileNamePrefix = new File(fileName).getName() + "-";
		File[] files = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.canWrite() && pathname.getName().startsWith(fileNamePrefix);
			}
		});
		
		if (files != null && files.length > 0) {
			File lastFile = files[files.length - 1]; // 按文件名升序排序
			if (lastFile.length() < FILE_SIZE) {
				return lastFile;
			}
			
		}
		
		return null;
	}


	/**
	 * 切换写入文件
	 * @author zhujun
	 * @date 2016年6月6日
	 *
	 */
	private void switchWritingFile() {
		IOUtils.closeQuietly(fileOutputStream);
		
		try {
			fileOutputStream = new FileOutputStream(writingFile, true);
			bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
		} catch (Exception e) {
			throw new RuntimeException("切换writing文件失败", e);
		}
	}


	private String generateSuffix() {
		// 文件后缀时间格式化, 如: 20160606153808
		return "-" + DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
	}

}
