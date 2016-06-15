package com.zhujun.spider.master.data.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.zhujun.spider.master.schedule.ScheduleConst;

/**
 * 使用file储存queue数据
 * 
 * @author zhujun
 * @date 2016年6月14日
 *
 */
public class FileLineQueue implements Queue<String> {

	/**
	 * 行分隔符
	 */
	private final static byte[] LINE_SEP = ScheduleConst.CRNL.getBytes();
	
	private File dataFile;
	private File indexFile;
	
	private FileChannel dataFileChannel;
	private FileChannel indexFileChannel;
	
	private Queue<String> dataQueue;
	
	private int index = 0;
	
	public FileLineQueue(String filePath) {
		
		dataFile = new File(filePath);
		indexFile = new File(filePath + ".index");
		
		dataQueue = new LinkedList<>();
		
		initFile();
		
		readQueueDataFromFile();
		
	}
	
	private void initFile() {
		try {
			if (!dataFile.exists()) {
				File parent = dataFile.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				
				dataFile.createNewFile();
			}
			
			if (!indexFile.exists()) {
				indexFile.createNewFile();
			}
		
			dataFileChannel = FileChannel.open(dataFile.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
			indexFileChannel = FileChannel.open(indexFile.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
		} catch (Exception e) {
			closeChannels();
			throw new RuntimeException("初始化文件失败", e);
		}
		
	}

	private void closeChannels() {
		IOUtils.closeQuietly(dataFileChannel);
		IOUtils.closeQuietly(indexFileChannel);
	}

	/**
	 * 读文件数据
	 * 
	 * @author zhujun
	 * @date 2016年6月14日
	 *
	 */
	private void readQueueDataFromFile() {
		String indexStr = readIndexContent();
		
		if (NumberUtils.isNumber(indexStr)) {
			index = Integer.valueOf(indexStr);
		}
		
		try {
			dataFileChannel.position(index); // 移动到index位置
			ByteBuffer buffer = ByteBuffer.allocate(1024); // 读数据缓冲
			ByteArrayOutputStream lineBytes = new ByteArrayOutputStream(); // 行数据
			while (dataFileChannel.read(buffer) != -1) { // 从文件读数据
				
				buffer.flip();
				if (buffer.remaining() > 0) {
					// 复制数据到byte[]
					byte[] readData = new byte[buffer.remaining()];
					buffer.get(readData);
					
					while (true) { // 用行分隔符切分数据
						int sepIndex = findLineSepIndex(readData); // 查询行分隔符位置
						
						if (sepIndex == -1) {
							lineBytes.write(readData);
							break;
						} else {
							// 找到分隔符
							lineBytes.write(readData, 0, sepIndex);
							
							dataQueue.add(new String(lineBytes.toByteArray(), "UTF-8")); // 数据入队
							lineBytes = new ByteArrayOutputStream();
							
							// 重置readData
							byte[] temp = new byte[readData.length - sepIndex - LINE_SEP.length];
							System.arraycopy(readData, sepIndex + LINE_SEP.length, temp, 0, temp.length);
							readData = temp;
							
							if (readData.length == 0) {
								break;
							}
						}
					}
				}
				
				buffer.clear();
			}
			
			if (lineBytes.size() > 0) {
				// 写入剩余的数据
				dataQueue.add(new String(lineBytes.toByteArray(), "UTF-8")); // 数据入队
			}
			
		} catch (Exception e) {
			closeChannels();
			throw new RuntimeException("读取数据失败", e);
		}
		
	}


	private int findLineSepIndex(byte[] readData) {
		for (int i = 0; i <= readData.length - LINE_SEP.length; i++) {
			int matchCount = 0;
			for (int j = 0; j < LINE_SEP.length; j++) {
				if (LINE_SEP[j] == readData[i + j]) {
					// 一位匹配
					matchCount++;
				} else {
					break;
				}
			}
			
			if (matchCount == LINE_SEP.length) {
				// 全部byte匹配
				return i;
			}
		}
		
		return -1;
	}

	private String readIndexContent() {
		try {
			ByteBuffer buffer = ByteBuffer.allocate(512);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			while (indexFileChannel.read(buffer) != -1) {
				buffer.flip();
				byte[] readData = new byte[buffer.remaining()];
				buffer.get(readData);
				os.write(readData);
				buffer.clear();
			}
			
			return new String(os.toByteArray(), "UTF-8");
		} catch (Exception e) {
			closeChannels();
			throw new RuntimeException("读取index出错", e);
		}
		
	}

	@Override
	public int size() {
		return dataQueue.size();
	}

	@Override
	public boolean isEmpty() {
		return dataQueue.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return dataQueue.contains(o);
	}

	@Override
	public Iterator<String> iterator() {
		return dataQueue.iterator();
	}

	@Override
	public Object[] toArray() {
		return dataQueue.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return dataQueue.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		throw new RuntimeException("不支持");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new RuntimeException("不支持");
	}

	@Override
	public boolean addAll(Collection<? extends String> c) {
		return dataQueue.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new RuntimeException("不支持");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new RuntimeException("不支持");
	}

	@Override
	synchronized public void clear() {
		try {
			dataQueue.clear();
			dataFileChannel.position(0);
			dataFileChannel.truncate(0);
			
			writeIndexContent(0);
		} catch (Exception e) {
			throw new RuntimeException("clear error", e);
		}
	}

	private void writeIndexContent(String string) {
		try {
			indexFileChannel.position(0);
			indexFileChannel.truncate(0);
			ByteBuffer buffer = ByteBuffer.wrap(string.getBytes("UTF-8"));
			indexFileChannel.write(buffer);
		} catch (Exception e) {
			throw new RuntimeException("写入index file内容出错", e);
		}
	}
	
	private void writeIndexContent(long lon) {
		writeIndexContent(String.valueOf(lon));
	}

	@Override
	public boolean add(String e) {
		addEle2file(e);
		return dataQueue.add(e);
	}

	private void addEle2file(String e) {
		try {
			byte[] eBytes = e.getBytes("UTF-8");
			ByteBuffer buffer = ByteBuffer.allocate(eBytes.length + LINE_SEP.length);
			buffer.put(eBytes);
			buffer.put(LINE_SEP);
			buffer.flip();
			
			dataFileChannel.write(buffer);
		} catch (Exception e2) {
			throw new RuntimeException("写入data文件出错", e2);
		}
		
	}

	@Override
	public boolean offer(String e) {
		addEle2file(e);
		return dataQueue.offer(e);
	}

	@Override
	public String remove() {
		return poll();
	}

	@Override
	public String poll() {
		String ele = dataQueue.poll();
		if (ele != null) {
			try {
				index = index + ele.getBytes("UTF-8").length + LINE_SEP.length; // 计算新位置
				writeIndexContent(index);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return ele;
	}

	@Override
	public String element() {
		return dataQueue.element();
	}

	@Override
	public String peek() {
		return dataQueue.peek();
	}

}
