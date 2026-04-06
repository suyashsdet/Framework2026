package utils;

import constants.FileLocationConstants;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static constants.FileLocationConstants.EXCEL_FILE_PATH;

public class ExcelUtil {




    public static Object[][] getData(String sheetName) {

        Object[][] data;
        try {
            FileInputStream ip = new FileInputStream(EXCEL_FILE_PATH);
            Workbook workBook= WorkbookFactory.create(ip);
            Sheet sheet = workBook.getSheet(sheetName);
            data = new Object[sheet.getLastRowNum()][sheet.getRow(0).getLastCellNum()];

            for(int i=0;i<sheet.getLastRowNum();i++){
                for(int j=0;j<sheet.getRow(0).getLastCellNum();j++){
                    data[i][j] = sheet.getRow(i+1).getCell(j).toString();


                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }
}
