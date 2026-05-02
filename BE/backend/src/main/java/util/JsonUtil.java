package util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Utility để đọc/ghi JSON từ request/response.
 */
public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }

    /** Đọc JSON body từ request và parse thành object */
    public static <T> T readBody(HttpServletRequest request, Class<T> clazz) throws IOException {
        return mapper.readValue(request.getInputStream(), clazz);
    }

    /** Ghi object thành JSON vào response */
    public static void writeJson(HttpServletResponse response, int status, Object data) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        mapper.writeValue(writer, data);
        writer.flush();
    }

    /** Ghi message thành JSON vào response */
    public static void writeMessage(HttpServletResponse response, int status, String message) throws IOException {
        writeJson(response, status, Map.of("message", message));
    }

    /** Ghi error thành JSON vào response */
    public static void writeError(HttpServletResponse response, int status, String error) throws IOException {
        writeJson(response, status, Map.of("error", error, "status", status));
    }

    /** Convert object thành JSON string */
    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
