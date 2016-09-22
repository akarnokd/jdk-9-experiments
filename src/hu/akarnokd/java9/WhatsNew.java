package hu.akarnokd.java9;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class WhatsNew {
	public static void main(String[] args) throws Exception {
		
		try (PrintWriter out = new PrintWriter(new FileWriter(new File("jd9.txt")))) {
			
			Files.walkFileTree(Paths.get("c:\\temp\\jdk9"), new SimpleFileVisitor<Path>() {
				
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes arg1) throws IOException {
					if (file.toString().endsWith(".java") && !file.toString().replace('\\', '/').contains("com/sun/")) {
						List<String> lines = Files.readAllLines(file);
						
						boolean once = false;
						
						for (int i = 0; i < lines.size(); i++) {
							String line = lines.get(i);
							
							if (line.contains("@since 9")) {
								if (!once) {
									once = true;
									out.println("-------------------------------------------------------------------------------------------------------------");
									out.println(file);
								}
								int k = i;
								for (int j = i - 1; j > 0; j--) {
									if (lines.get(j).contains("/**")) {
										k = j;
										break;
									}
								}
								
								int m = i;
								for (int j = i + 1; j < lines.size(); j++) {
									if (lines.get(j).trim().endsWith("{") || lines.get(j).trim().endsWith(";")) {
										m = j;
										break;
									}
								}
								
								for (int j = k; j <= m; j++) {
									out.println(lines.get(j));
								}
								
								i = m;
								out.println();
							}
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}

	}
}
