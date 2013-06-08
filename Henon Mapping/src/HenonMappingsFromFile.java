import net.clonecomputers.lab.henonmap.*;

import java.io.*;
public class HenonMappingsFromFile {
	public static void main(String[] args) throws Exception{
		HenonMap2.display(750,750,true,new BufferedReader(new InputStreamReader(
			(new Pair()).getClass().getResourceAsStream("Good Inputs"))));
		/*String fp = new File(new net.clonecomputers.lab.henonmap.Pair()
			.getClass().getResource("Good Inputs").toURI()).getAbsolutePath();
		//File f = 
		//HenonMappings.main(new String[]{fp});
		System.out.println(fp);*/
	}
}
