package com.zhujun.export.appendfile;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class AppendFileReaderTest {

	private final static String APPEND_FILE = "E:\\tmp\\spider\\baike_clone\\data-20180518101708";

	@Test
	public void testReadMetaData() throws FileNotFoundException {
		AppendFileReader reader = new AppendFileReader(APPEND_FILE);
		try {
			MetaData metaData = null;
			int count = 0;
			while (true) {
				metaData = reader.readMetaData();
				if (metaData == null) {
					break;
				}

				count++;

				printMetaData(metaData, count);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void printMetaData(MetaData metaData, int index) {
		System.out.println("count:" + index + " | " +  metaData.getUrl() + " | " + metaData.getSize() + " | "
                + metaData.getContentType() + " | " + metaData.getFetchTime() + " | " + metaData.getOffset());
	}


	@Test
	public void testSetOffset() throws IOException {
		AppendFileReader reader = new AppendFileReader(APPEND_FILE);
		reader.setOffset(134023992);
		printMetaData(reader.readMetaData(), 1);
	}
	
	
	@Test
	public void testReadMetaAndData() throws FileNotFoundException {
		AppendFileReader reader = new AppendFileReader(APPEND_FILE);
		try {
			MetaData metaData = null;
			while (true) {
				metaData = reader.readMetaData();
				if (metaData == null) {
					break;
				}
				
				System.out.println("Meta: " + metaData.getUrl() + " | " + metaData.getSize() + " | " 
						+ metaData.getContentType() + " | " + metaData.getFetchTime());
				System.out.println(new String(reader.readFileData()));
				System.out.println();
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
