package ru.binaryblitz.Chisto.Utils;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import ru.binaryblitz.Chisto.Server.ServerApi;

public class ServerErrorHandler {
    public static class APIError {

        private int statusCode;
        private String message;

        APIError() {
        }

        public int status() {
            return statusCode;
        }

        public String message() {
            return message;
        }
    }

    public static APIError parseError(Response<?> response) {
        Converter<ResponseBody, APIError> converter =
                ServerApi.retrofit().responseBodyConverter(APIError.class, new Annotation[0]);

        APIError error;

        try {
            error = converter.convert(response.errorBody());
        } catch (IOException e) {
            LogUtil.logException(e);
            return new APIError();
        }

        return error;
    }
}
