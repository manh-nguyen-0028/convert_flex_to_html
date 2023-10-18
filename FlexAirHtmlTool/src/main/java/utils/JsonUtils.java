package utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import service.XmlService;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class JsonUtils<T> {

    public List<T> readJsonFile(String filePath, Class<T> zClass) {
        List<T> results = null;
        InputStream inputStream = XmlService.class.getClassLoader().getResourceAsStream(filePath);
        if (inputStream != null) {
            Gson gson = new Gson();
            InputStreamReader reader = new InputStreamReader(inputStream);

            Type listType = TypeToken.getParameterized(List.class, zClass).getType();

            results = gson.fromJson(reader, listType);
        }
        return results;
    }
}
