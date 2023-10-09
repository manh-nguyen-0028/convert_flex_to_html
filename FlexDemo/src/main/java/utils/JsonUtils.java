package utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class JsonUtils<T> {

    public List<T> readJsonFile(String filePath, Class<T> zClass) {
        List<T> results = null;
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            Type listType = TypeToken.getParameterized(List.class, zClass).getType();
            results = gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }
}
