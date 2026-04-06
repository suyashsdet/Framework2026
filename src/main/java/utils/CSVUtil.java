package utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static constants.FileLocationConstants.*;

public class CSVUtil {

    private static final String CSV_PATH = CSV_FILE_PATH;
    private static List<String[]> rows;

    public static Object[][] csvData(String csvFileName)
    {
        String csvFile = CSV_PATH+csvFileName+".csv";
        CSVReader reader;

        try{
            reader = new CSVReader(new FileReader(csvFile));
            rows = reader.readAll();
            reader.close();

        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }

        Object[][] data = new Object[rows.size()][];
        for(int i =0; i<rows.size();i++) {
            data[i] = rows.get(i);
        }

        return data;


    }

}



