package org.geometerplus.android.fbreader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileZip{
	private static void zip(String targetPath, String sourcePath){
		File sourceFile = null;
		try{
			sourceFile = new File(sourcePath);
			if(!sourceFile.exists()){
				System.out.println("File could not be found");
				return;
			}
		}
		catch(Exception e){
			
		}
		ZipOutputStream zos = null;
		try{
			zos = new ZipOutputStream(new BufferedOutputStream(
                    new FileOutputStream(targetPath)));
            zos = doZip(zos, sourceFile, null);
		}
		catch(Exception e){
			
		}
		finally {
            if (zos != null) {
                try {
                    zos.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}
	private static ZipOutputStream doZip(ZipOutputStream zos, File sourceDir,
            String folder) throws FileNotFoundException, IOException {
        File[] sourceFiles = sourceDir.listFiles();
        BufferedInputStream bis;
        ZipEntry entry;
        String path;
        for (int i = 0; i < sourceFiles.length; i++) {
            path = (folder == null ? "" : folder + "/")
                    + sourceFiles[i].getName();
            if (sourceFiles[i].isDirectory()) {
                // 如果是目錄，就往下一層
                /*
            	Log.d(TAG,
                        "Open a directory: " + sourceFiles[i].getAbsolutePath());
                */
                doZip(zos, new File(sourceDir, sourceFiles[i].getName()),
                        path);
            }
            else {
                //Log.d(TAG, "Zip a file: " + sourceFiles[i].getAbsolutePath());
                bis = new BufferedInputStream(new FileInputStream(
                        sourceFiles[i]));
                entry = new ZipEntry(path);
                zos.putNextEntry(entry);
                int data = 0;
                while ((data = bis.read()) != -1) {
                    zos.write(data);
                }
                bis.close();
            }
        }
        zos.flush();
        return zos;
    }

}
