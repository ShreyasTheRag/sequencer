package audio;
import java.io.File;

public class Search {
	public Search()
	{
		
	}
	
	
	public static void listFilesForFolder(final File folder) {
	    for (final File fileEntry : folder.listFiles()) {
	    	System.out.println(fileEntry.getName());
//	        if (fileEntry.isDirectory()) {
//	            listFilesForFolder(fileEntry);
//	        } else {
//	            System.out.println(fileEntry.getName());
//	        }
	    }
	}
	
	
	public static void main(String[] args)
	{
		final File folder = new File("./src/audio/sounds");
		listFilesForFolder(folder);
	}
	
}


