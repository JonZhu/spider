package com.zhujun.export.appendfile;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class AppendFileReaderTest {

	@Test
	public void testReadMetaData() throws FileNotFoundException {
		AppendFileReader reader = new AppendFileReader("D:/spider/meituan_clone/data-20161104220641");
		try {
			MetaData metaData = null;
			while (true) {
				metaData = reader.readMetaData();
				if (metaData == null) {
					break;
				}
				
				System.out.println(metaData.getUrl() + " | " + metaData.getSize() + " | " 
						+ metaData.getContentType() + " | " + metaData.getFetchTime());
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
	
	
	@Test
	public void testReadMetaAndData() throws FileNotFoundException {
		AppendFileReader reader = new AppendFileReader("D:/spider/meituan_clone/data-20161104220641");
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
