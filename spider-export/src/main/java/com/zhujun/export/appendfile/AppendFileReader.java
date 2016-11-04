package com.zhujun.export.appendfile;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * AppendFile读取器
 * 
 * @author zhujun
 * @date 2016年11月4日
 *
 */
public class AppendFileReader implements Closeable {
	
	private final static int STATUS_READ_SPIDER = 0;
	private final static int STATUS_READ_HEADER = 1;
	private final static int STATUS_READ_FILE = 2;
	
	private final static String START_LINE_FLAG = "SPIDER";
	private final static String EMPTY_LINE = "";

	private String filePath;
	private RandomAccessFile randomAccessFile;
	
	/**
	 * 目前读取状态
	 */
	private int status = STATUS_READ_SPIDER;
	
	public AppendFileReader(String filePath) throws FileNotFoundException {
		this.filePath = filePath;
		
		File file = new File(filePath);
		if (!file.isFile() || !file.exists()) {
			throw new FileNotFoundException("文件不存在");
		}
		
		this.randomAccessFile = new RandomAccessFile(file, "r");
	}
	
	/**
	 * 读取下一个文件元数据
	 * 
	 * @author zhujun
	 * @date 2016年11月4日
	 *
	 * @return 返回null，表示数据结束
	 * @throws IOException 
	 */
	public MetaData readMetaData() throws IOException {
		status = STATUS_READ_SPIDER; // 重置状态
		MetaData metaData = null;
		String line = null;
		while(true) {
			if (status == STATUS_READ_SPIDER) {
				// 读取spider标识
				line = randomAccessFile.readLine();
				if (line == null) {
					// 文件结束
					return null;
				}
				
				if (START_LINE_FLAG.equals(line)) {
					// 读到起始标识行
					status = STATUS_READ_HEADER;
					metaData = new MetaData();
				}
				
			} else if (status == STATUS_READ_HEADER) {
				// 读header
				line = randomAccessFile.readLine();
				if (line == null || EMPTY_LINE.equals(line)) {
					// header结束
					status = STATUS_READ_FILE;
					return metaData;
				}
				
				// 解析header
				String[] headerPair = line.split(";", 2);
				
				
			}
		}
		
		
	}
	
	/**
	 * 读取当前元数据的文件数据
	 * 
	 * @author zhujun
	 * @date 2016年11月4日
	 *
	 * @return
	 */
	public byte[] readFileData() {
		return null;
	}
	
	@Override
	public void close() throws IOException {
		if (randomAccessFile != null) {
			randomAccessFile.close();
		}
	}

	public String getFilePath() {
		return filePath;
	}

}
