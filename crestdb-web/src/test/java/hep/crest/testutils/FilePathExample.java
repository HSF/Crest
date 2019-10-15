/**
 * 
 */
package hep.crest.testutils;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;

/**
 * @author formica
 *
 */
public class FilePathExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Path p = Paths.get("C:\\Hello\\AnotherFolder\\The File Name.PDF");
		String file = p.getFileName().toString();
		System.out.println(" - "+file);
		p = Paths.get("/tmp/test.out");
		file = p.getFileName().toString();
		System.out.println(" - "+file);		
		String name1 = FilenameUtils.getName("/ab/cd/xyz.txt");
		System.out.println(" - "+name1);		
		String name2 = FilenameUtils.getName("C:\\\\Hello\\\\AnotherFolder\\\\The File Name.PDF");
		System.out.println(" - "+name2);		
	}

}
