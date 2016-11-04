package com.zhujun.export.appendfile;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
	
	private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private String filePath;
	private RandomAccessFile randomAccessFile;
	
	/**
	 * 目前读取状态
	 */
	private int status = STATUS_READ_SPIDER;
	
	/**
	 * 当前文件大小
	 */
	private long currentFileSize = 0;
	
	/**
	 * 是否已经读取文件数据
	 */
	private boolean isReadFile = false;
	
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
		if (status == STATUS_READ_FILE && !isReadFile && currentFileSize > 0) {
			// 有文件数据未读, 跳过文件数据
			randomAccessFile.skipBytes((int)currentFileSize);
		}
		
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
					
					// 设置后续文件数据
					isReadFile = false;
					currentFileSize = metaData.getSize();
					
					return metaData;
				}
				
				// 解析header
				String[] headerPair = line.split(":", 2);
				if (headerPair.length != 2) {
					continue;
				}
				
				// 填充metadata值
				fillMetaData(metaData, headerPair[0].trim(), headerPair[1].trim());
			}
		}
		
		
	}
	
	/**
	 * 使用appendfile中数据header填充metadata
	 * 
	 * @param metaData
	 * @param headerName
	 * @param headerValue
	 */
	private void fillMetaData(MetaData metaData, String headerName, String headerValue) {
		if ("Url".equals(headerName)) {
			metaData.setUrl(headerValue);
		} else if ("Content-Length".equals(headerName)) {
			metaData.setSize(Long.valueOf(headerValue));
		} else if ("ContentType".equals(headerName)) {
			metaData.setContentType(headerValue);
		} else if ("Time".equals(headerName)) {
			try {
				metaData.setFetchTime(TIME_FORMAT.parse(headerValue));
			} catch (ParseException e) {
				// do nothing
				e.printStackTrace();
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
	 * @throws IOException 
	 */
	public byte[] readFileData() throws IOException {
		if (status != STATUS_READ_FILE || currentFileSize == 0 || isReadFile) {
			return null;
		}
		
		byte data[] = new byte[(int)currentFileSize];
		randomAccessFile.read(data);
		
		status = STATUS_READ_SPIDER; // 设置下一阶段状态
		isReadFile = true;
		currentFileSize = 0;
		
		return data;
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
