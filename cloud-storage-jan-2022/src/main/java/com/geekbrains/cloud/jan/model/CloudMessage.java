package com.geekbrains.cloud.jan.model;

import java.io.Serializable;

public interface CloudMessage extends Serializable {
    CommandType getType();
}
