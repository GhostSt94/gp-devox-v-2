package com.utils;

import org.apache.log4j.Logger;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StaticFunctions {

	static Logger logger = Logger.getLogger(StaticFunctions.class);

    public static String formatDate(long millis,String format){
        try {
            Date date = new Date(millis);
            DateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.format(date);
        }catch (Exception e){
            return "";
        }
    }

	public static boolean prepareDirectory(String directoryPath) {
		File file = new File(directoryPath);
		if (file.exists() && !file.isDirectory() || !file.exists()) {
			boolean result = file.mkdirs();
			if (result)
				logger.debug("directory '" + directoryPath + "' created with success");
			else
				logger.debug("failed creating directory '" + directoryPath + "'");
			return result;
		} else {
			logger.debug("directory '" + directoryPath + "' already exist");
			return true;
		}
	}

}
