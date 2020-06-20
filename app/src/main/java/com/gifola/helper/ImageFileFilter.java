package com.gifola.helper;

import java.io.File;

/**
 * Created by Keval on 10/7/2017.
 */

public class ImageFileFilter {
    File file;
    private static final String[] okFileExtensions =  new String[] {"jpg", "png", "gif","jpeg"};
    private static final String[] okDocumentFileExtensions =  new String[] {"pdf", "doc", "docx"};

    public static boolean accept(File file)
    {
        for (String extension : okFileExtensions)
        {
            if (file.getName().toLowerCase().endsWith(extension))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean acceptDocument(File file)
    {
        for (String extension : okDocumentFileExtensions)
        {
            if (file.getName().toLowerCase().endsWith(extension))
            {
                return true;
            }
        }
        return false;
    }

    public static int getFileSize(String path){
        int fileSize=0;

        File file = new File(path);
        fileSize= Integer.parseInt(String.valueOf(file.length()/1024));

        return fileSize;
    }
}
