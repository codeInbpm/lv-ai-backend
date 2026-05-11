package com.lvai.common;

public interface ResultCode {
    int SUCCESS = 200;
    int ERROR = 500;
    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    int BAD_REQUEST = 400;
    int TOO_MANY_REQUESTS = 429;
}
