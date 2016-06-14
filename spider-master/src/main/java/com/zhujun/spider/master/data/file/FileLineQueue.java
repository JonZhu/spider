package com.zhujun.spider.master.data.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 使用file储存queue数据
 * 
 * @author zhujun
 * @date 2016年6月14日
 *
 */
public class FileLineQueue implements Queue<String> {

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
		if (!dataFile.exists()) {
			File parent = dataFile.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
		}
		
		try {
			dataFileChannel = FileChannel.open(dataFile.toPath());
			indexFileChannel = FileChannel.open(indexFile.toPath());
		} catch (Exception e) {
			IOUtils.closeQuietly(dataFileChannel);
			IOUtils.closeQuietly(indexFileChannel);
			
			throw new RuntimeException("初始化文件失败", e);
		}
		
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
		
		dataFileChannel.position(index);
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		while (dataFileChannel.read(buffer) != -1) {
			
			buffer
			
		}
		
		
	}


	private String readIndexContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<String> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends String> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean add(String e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean offer(String e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String remove() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String poll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String element() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String peek() {
		// TODO Auto-generated method stub
		return null;
	}

}
